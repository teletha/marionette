/*
 * Copyright (C) 2025 The MARIONETTE Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package marionette;

import static java.lang.Boolean.TRUE;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.BaseTSD.ULONG_PTR;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.HHOOK;
import com.sun.jna.platform.win32.WinUser.HOOKPROC;
import com.sun.jna.platform.win32.WinUser.KBDLLHOOKSTRUCT;
import com.sun.jna.platform.win32.WinUser.LowLevelKeyboardProc;

class GlobalEvents {

    /** The keyboard hook. */
    private static NativeKeyboardHook keyboardHook = new NativeKeyboardHook();

    /** The keyboard hook. */
    private static NativeMouseHook mouseHook = new NativeMouseHook();

    /**
     * Start native hook.
     */
    static void initializeNativeHook() {
        keyboardHook.install();
        mouseHook.install();
    }

    /**
     * Stop native hook.
     */
    static void disposeNativeHook() {
        keyboardHook.uninstall();
        mouseHook.uninstall();
    }

    /**
     * 
     */
    protected static abstract class NativeHook<T> implements Runnable, HOOKPROC {

        /** The actual executor. */
        private final ExecutorService executor = new ThreadPoolExecutor(4, 256, 30, TimeUnit.SECONDS, new SynchronousQueue(), runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName(NativeHook.class.getSimpleName());
            thread.setDaemon(true);
            return thread;
        });

        /** The native hook. */
        protected HHOOK hook;

        private int threadId;

        /**
         * Install service.
         */
        void install() {
            executor.execute(this);
            Runtime.getRuntime().addShutdownHook(new Thread(this::uninstall));
        }

        /**
         * Uninstall service.
         */
        void uninstall() {
            User32.INSTANCE.PostThreadMessage(threadId, WinUser.WM_QUIT, new WinDef.WPARAM(), new WinDef.LPARAM());
            executor.shutdownNow();
        }

        /**
         * <p>
         * Configure hook type.
         * </p>
         * 
         * @return
         */
        protected abstract int hookType();

        /**
         * {@inheritDoc}
         */
        @Override
        public final void run() {
            threadId = Kernel32.INSTANCE.GetCurrentThreadId();
            hook = User32.INSTANCE.SetWindowsHookEx(hookType(), this, Kernel32.INSTANCE.GetModuleHandle(null), 0);

            User32.INSTANCE.GetMessage(new WinUser.MSG(), new WinDef.HWND(Pointer.NULL), 0, 0);
            User32.INSTANCE.UnhookWindowsHookEx(hook);
        }

        /**
         * Handle key event.
         * 
         * @param key
         */
        protected final boolean handle(T key, List<MacroDefinition> macros, KeyEvent event) {
            boolean consumed = false;
            Window now = Window.now();

            // built-in state management macro

            for (MacroDefinition macro : macros) {
                if (macro.enable.is(TRUE) && macro.windowConditon.test(now) && macro.condition.test(key)) {
                    executor.execute(() -> {
                        macro.events.accept(event);
                    });

                    if (macro.consumable) {
                        consumed = true;
                    }
                }
            }
            return consumed;
        }
    }

    /**
     * @version 2018/11/19 9:45:39
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
        private final int InjectedEvent = 1 << 4;

        /** The key mapper. */
        private final Key[] keys = new Key[256];

        /** The key mapper. */
        private final Key[] extendedKeys = new Key[256];

        {
            for (Key key : Key.values()) {
                if (key.extend) {
                    extendedKeys[key.virtualCode] = key;
                } else {
                    keys[key.virtualCode] = key;
                }
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
                Key key = (info.flags & 1) == 0 ? keys[info.vkCode] : extendedKeys[info.vkCode];

                switch (wParam.intValue()) {
                case WinUser.WM_KEYDOWN:
                case WinUser.WM_SYSKEYDOWN:
                    if (downLatest != key) {
                        downLatest = key;
                        consumed = handle(key, MacroDefinition.presses, KeyEvent.of(key));
                    }
                    break;

                case WinUser.WM_KEYUP:
                case WinUser.WM_SYSKEYUP:
                    downLatest = null;
                    consumed = handle(key, MacroDefinition.releases, KeyEvent.of(key));
                    break;
                }
            }
            return consumed ? new LRESULT(-1)
                    : User32.INSTANCE.CallNextHookEx(hook, nCode, wParam, new LPARAM(Pointer.nativeValue(info.getPointer())));
        }
    }

    /**
     * @version 2018/11/19 9:45:34
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
            boolean userInput = (info.flags & InjectedEvent) == 0 || wParam.intValue() == 519 || wParam.intValue() == 520;
            if (0 <= nCode && userInput) {
                info.pt.time = info.time;

                switch (wParam.intValue()) {
                case 512: // WM_MOUSEMOVE
                    consumed = handle(Key.MouseMiddle, MacroDefinition.mouseMove, info.pt);
                    break;

                case 513: // WM_LBUTTONDOWN
                    consumed = handle(Key.MouseLeft, MacroDefinition.presses, info.pt);
                    break;

                case 514: // WM_LBUTTONUP
                    consumed = handle(Key.MouseLeft, MacroDefinition.releases, info.pt);
                    break;

                case 516: // WM_RBUTTONDOWN
                    consumed = handle(Key.MouseRight, MacroDefinition.presses, info.pt);
                    break;

                case 517: // WM_RBUTTONUP
                    consumed = handle(Key.MouseRight, MacroDefinition.releases, info.pt);
                    break;

                case 519: // WM_MBUTTONDOWN
                    consumed = handle(Key.MouseMiddle, MacroDefinition.presses, info.pt);
                    break;

                case 520: // WM_MBUTTONUP
                    consumed = handle(Key.MouseMiddle, MacroDefinition.releases, info.pt);
                    break;

                case 522: // WM_MOUSEWHEEL
                    info.pt.delta = info.mouseData.getHigh().doubleValue();
                    consumed = handle(Key.MouseMiddle, MacroDefinition.mouseWheel, info.pt);
                    break;
                }
            }
            return consumed ? new LRESULT(-1)
                    : User32.INSTANCE.CallNextHookEx(hook, nCode, wParam, new LPARAM(Pointer.nativeValue(info.getPointer())));
        }
    }

    /**
     * @version 2018/11/19 9:45:45
     */
    private static interface LowLevelMouseProc extends HOOKPROC {

        LRESULT callback(int nCode, WPARAM wParam, MSLLHOOKSTRUCT lParam);
    }

    /**
     * @version 2018/11/19 9:45:47
     */
    public static class Point extends Structure implements KeyEvent {

        public NativeLong x;

        public NativeLong y;

        private long time;

        private double delta;

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
        public double delta() {
            return delta;
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
            return "Point [x=" + x + ", y=" + y + ", time=" + time + ", delta=" + delta + "]";
        }
    }

    /**
     * Contains information about a low-level mouse input event.
     */
    public static class MSLLHOOKSTRUCT extends Structure {

        /**
         * The x- and y-coordinates of the cursor, in per-monitor-aware screen coordinates.
         */
        public Point pt;

        /**
         * <p>
         * If the message is WM_MOUSEWHEEL, the high-order word of this member is the wheel delta.
         * The low-order word is reserved. A positive value indicates that the wheel was rotated
         * forward, away from the user; a negative value indicates that the wheel was rotated
         * backward, toward the user. One wheel click is defined as WHEEL_DELTA, which is 120.
         * </p>
         * <p>
         * If the message is WM_XBUTTONDOWN, WM_XBUTTONUP, WM_XBUTTONDBLCLK, WM_NCXBUTTONDOWN,
         * WM_NCXBUTTONUP, or WM_NCXBUTTONDBLCLK, the high-order word specifies which X button was
         * pressed or released, and the low-order word is reserved. This value can be one or more of
         * the following values. Otherwise, mouseData is not used.
         * </p>
         */
        public DWORD mouseData;

        /**
         * The event-injected flags. An application can use the following values to test the flags.
         * Testing LLMHF_INJECTED (bit 0) will tell you whether the event was injected. If it was,
         * then testing LLMHF_LOWER_IL_INJECTED (bit 1) will tell you whether or not the event was
         * injected from a process running at lower integrity level.
         */
        public int flags;

        /**
         * The time stamp for this message.
         */
        public long time;

        /**
         * Additional information associated with the message.
         */
        public ULONG_PTR dwExtraInfo;

        /**
         * {@inheritDoc}
         */
        @Override
        protected List getFieldOrder() {
            return Arrays.asList(new String[] {"pt", "mouseData", "flags", "time", "dwExtraInfo"});
        }
    }
}