/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package marionette.macro;

import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

import javax.imageio.ImageIO;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.HOOKPROC;
import com.sun.jna.platform.win32.WinUser.KBDLLHOOKSTRUCT;
import com.sun.jna.platform.win32.WinUser.LowLevelKeyboardProc;

import kiss.I;
import kiss.Manageable;
import kiss.Observer;
import kiss.Signal;
import kiss.Singleton;
import marionette.macro.Macro.NativeHook;

/**
 * @version 2018/11/17 13:48:59
 */
@Manageable(lifestyle = Singleton.class)
public class Macros {

    /** Acceptable condition. */
    private static final Predicate ANY = new Predicate() {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean test(Object o) {
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Predicate and(Predicate other) {
            return other;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Predicate or(Predicate other) {
            return other;
        }
    };

    /** The window condition. */
    private Predicate<Window> windowCondition = ANY;

    /** The keyboard hook. */
    private NativeKeyboardHook keyboardHook = new NativeKeyboardHook();

    /** The keyboard hook. */
    private NativeMouseHook mouseHook = new NativeMouseHook();

    /** The tray icon. */
    private final TrayIcon tray;

    /**
     * 
     */
    private Macros() {
        keyboardHook.install();
        mouseHook.install();

        try {
            tray = new TrayIcon(ImageIO.read(Macro.class.getResource("icon.png")));
            tray.setImageAutoSize(true);
            tray.setToolTip("Marionette");
            tray.setPopupMenu(menu());
            SystemTray.getSystemTray().add(tray);
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * @version 2016/10/02 18:01:25
     */
    public interface MacroDSL {

        /**
         * <p>
         * Consume the native event.
         * </p>
         * 
         * @return
         */
        MacroDSL consume();

        /**
         * <p>
         * Declare press event.
         * </p>
         * 
         * @return
         */
        Signal<KeyEvent> press();

        /**
         * <p>
         * Declare release event.
         * </p>
         * 
         * @return
         */
        Signal<KeyEvent> release();

        /**
         * <p>
         * Declare key modifier.
         * </p>
         * 
         * @return Chainable DSL.
         */
        MacroDSL withAlt();

        /**
         * <p>
         * Declare key modifier.
         * </p>
         * 
         * @return Chainable DSL.
         */
        MacroDSL withCtrl();

        /**
         * <p>
         * Declare key modifier.
         * </p>
         * 
         * @return Chainable DSL.
         */
        MacroDSL withShift();
    }

    /**
     * @version 2016/10/04 15:57:53
     */
    private class KeyMacro implements MacroDSL {

        /** The window condition. */
        private Predicate<Window> window = windowCondition;

        /** The acceptable event type. */
        private Predicate condition = ANY;

        /** The event should be consumed or not. */
        private boolean consumable;

        /** The associated key. */
        private Key key;

        /** The modifier state. */
        private boolean alt;

        /** The modifier state. */
        private boolean ctrl;

        /** The modifier state. */
        private boolean shift;

        /** The observers. */
        private final List<Observer<? super KeyEvent>> observers = new CopyOnWriteArrayList();

        /**
         * <p>
         * Set key type.
         * </p>
         * 
         * @param key
         * @return
         */
        private KeyMacro key(Key key) {
            this.key = key;
            condition = condition.and(e -> e == this.key);

            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public MacroDSL consume() {
            consumable = true;
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public MacroDSL withAlt() {
            alt = true;
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public MacroDSL withCtrl() {
            ctrl = true;
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public MacroDSL withShift() {
            shift = true;
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<KeyEvent> press() {
            return register((key.mouse ? mouseHook : keyboardHook).presses);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<KeyEvent> release() {
            return register((key.mouse ? mouseHook : keyboardHook).releases);
        }

        /**
         * <p>
         * Register this macro.
         * </p>
         * 
         * @param macros
         * @return
         */
        private Signal<KeyEvent> register(List<KeyMacro> macros) {
            macros.add(this);

            return new Signal<KeyEvent>((observer, disposer) -> {
                observers.add(observer);
                return disposer.add(() -> observers.remove(observer));
            });
        }

        /**
         * <p>
         * Test modifier state.
         * </p>
         * 
         * @param alt The modifier state.
         * @param ctrl The modifier state.
         * @param shift The modifier state.
         * @return
         */
        private boolean modifier(boolean alt, boolean ctrl, boolean shift) {
            return this.alt == alt && this.ctrl == ctrl && this.shift == shift;
        }
    }

    /**
     * @version 2016/10/03 12:31:30
     */
    private static class NativeKeyboardHook extends NativeHook implements LowLevelKeyboardProc {

        /**
         * <p>
         * Specifies whether the event was injected. The value is 1 if that is the case; otherwise,
         * it is 0. Note that bit 1 is not necessarily set when bit 4 is set. <a href=
         * "https://msdn.microsoft.com/ja-jp/library/windows/desktop/ms644967(v=vs.85).aspx">REF
         * </a>
         * </p>
         */
        private static final int InjectedEvent = 1 << 4;

        /** The key mapper. */
        private static final Key[] keys = new Key[256];

        static {
            for (Key key : Key.values()) {
                keys[key.virtualCode] = key;
            }
        }

        /** The record for the last downed key to decimate a flood of key down Signal. */
        private Key downLatest;

        /**
         * {@inheritDoc}
         */
        @Override
        protected int hookType() {
            return WinUser.WH_KEYBOARD_LL;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public LRESULT callback(int nCode, WPARAM wParam, KBDLLHOOKSTRUCT info) {
            boolean consumed = false;
            boolean userInput = (info.flags & InjectedEvent) == 0;

            if (0 <= nCode && userInput) {
                Key key = keys[info.vkCode];

                switch (wParam.intValue()) {
                case WinUser.WM_KEYDOWN:
                case WinUser.WM_SYSKEYDOWN:
                    if (downLatest != key) {
                        downLatest = key;
                        consumed = handle(key, presses, KeyEvent.of(key));
                    }
                    break;

                case WinUser.WM_KEYUP:
                case WinUser.WM_SYSKEYUP:
                    downLatest = null;
                    consumed = handle(key, releases, KeyEvent.of(key));
                    break;
                }
            }
            return consumed ? new LRESULT(-1)
                    : User32.INSTANCE.CallNextHookEx(hook, nCode, wParam, new LPARAM(Pointer.nativeValue(info.getPointer())));
        }
    }

    /**
     * @version 2016/10/03 12:31:30
     */
    private static class NativeMouseHook extends NativeHook implements LowLevelMouseProc {

        /**
         * <p>
         * The event-injected flags. An application can use the following values to test the flags.
         * Testing LLMHF_INJECTED (bit 0) will tell you whether the event was injected. If it was,
         * then testing LLMHF_LOWER_IL_INJECTED (bit 1) will tell you whether or not the event was
         * injected from a process running at lower integrity level. <a href=
         * "https://msdn.microsoft.com/en-us/library/windows/desktop/ms644970(v=vs.85).aspx">REF
         * </a>
         * </p>
         */
        private static final int InjectedEvent = 1;

        /** The event listeners. */
        private final List<KeyMacro> moves = new ArrayList();

        /**
         * {@inheritDoc}
         */
        @Override
        protected int hookType() {
            return WinUser.WH_MOUSE_LL;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public LRESULT callback(int nCode, WPARAM wParam, MSLLHOOKSTRUCT info) {
            boolean consumed = false;
            boolean userInput = (info.flags & InjectedEvent) == 0;

            if (0 <= nCode && userInput) {
                info.pt.time = info.time;

                switch (wParam.intValue()) {
                case 513: // WM_LBUTTONDOWN
                    consumed = handle(Key.MouseLeft, presses, info.pt);
                    break;

                case 514: // WM_LBUTTONUP
                    consumed = handle(Key.MouseLeft, releases, info.pt);
                    break;

                case 516: // WM_RBUTTONDOWN
                    consumed = handle(Key.MouseRight, presses, info.pt);
                    break;

                case 517: // WM_RBUTTONUP
                    consumed = handle(Key.MouseRight, releases, info.pt);
                    break;

                case 519: // WM_MBUTTONDOWN
                    consumed = handle(Key.MouseMiddle, presses, info.pt);
                    break;

                case 520: // WM_MBUTTONDOWN
                    consumed = handle(Key.MouseMiddle, releases, info.pt);
                    break;
                }
            }
            return consumed ? new LRESULT(-1)
                    : User32.INSTANCE.CallNextHookEx(hook, nCode, wParam, new LPARAM(Pointer.nativeValue(info.getPointer())));
        }
    }

    /**
     * @version 2016/10/04 3:49:48
     */
    private static interface LowLevelMouseProc extends HOOKPROC {

        LRESULT callback(int nCode, WPARAM wParam, MSLLHOOKSTRUCT lParam);
    }

    /**
     * @version 2016/10/16 10:34:57
     */
    public static class Point extends Structure implements KeyEvent {

        public NativeLong x;

        public NativeLong y;

        private long time;

        /**
         * {@inheritDoc}
         */
        @Override
        protected List getFieldOrder() {
            return Arrays.asList(new String[] {"x", "y"});
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int x() {
            return x.intValue();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int y() {
            return y.intValue();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long time() {
            return time;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + ((x == null) ? 0 : x.hashCode());
            result = prime * result + ((y == null) ? 0 : y.hashCode());
            return result;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Point) {
                Point other = (Point) obj;
                return x() == other.x() && y() == other.y();
            }
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "Point [x=" + x + ", y=" + y + ", time=" + time + "]";
        }
    }

    /**
     * @version 2016/10/04 14:06:51
     */
    public static class MSLLHOOKSTRUCT extends Structure {

        public Point pt;

        public int mouseData;

        public int flags;

        public int time;

        public int dwExtraInfo;

        /**
         * {@inheritDoc}
         */
        @Override
        protected List getFieldOrder() {
            return Arrays.asList(new String[] {"pt", "mouseData", "flags", "time", "dwExtraInfo"});
        }
    }
}
