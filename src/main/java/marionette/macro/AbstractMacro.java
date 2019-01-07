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

import java.util.function.Predicate;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.LONG;
import com.sun.jna.platform.win32.WinDef.WORD;
import com.sun.jna.platform.win32.WinUser.INPUT;
import com.sun.jna.platform.win32.WinUser.KEYBDINPUT;

import kiss.Extensible;
import kiss.I;
import kiss.Signal;
import marionette.macro.GlobalEvents.KeyMacro;

public abstract class AbstractMacro<Self extends AbstractMacro> implements Extensible {

    /** The display scale. */
    private static final long ScaleX = 65536 / User32.INSTANCE.GetSystemMetrics(User32.SM_CXSCREEN);

    /** The display scale. */
    private static final long ScaleY = 65536 / User32.INSTANCE.GetSystemMetrics(User32.SM_CYSCREEN);

    /** The window condition. */
    Predicate<Window> windowCondition = I.accept();

    /**
     * 
     */
    protected AbstractMacro() {
    }

    /**
     * Declare your macros.
     */
    protected abstract void declare();

    /**
     * <p>
     * Declare key related event.
     * </p>
     * 
     * @param key
     * @return
     */
    protected final MacroDSL when(Key key) {
        return new KeyMacro(key, windowCondition);
    }

    /**
     * <p>
     * Declare mouse related event.
     * </p>
     * 
     * @param mouse
     * @return
     */
    protected final Signal<KeyEvent> when(Mouse mouse) {
        return new KeyMacro(windowCondition).register(mouse);
    }

    /**
     * <p>
     * Emulate press event.
     * </p>
     * 
     * @param key
     * @return
     */
    protected final Self press(Key key) {
        return emulate(key, true, false);
    }

    /**
     * <p>
     * Emulate release event.
     * </p>
     * 
     * @param key
     * @return
     */
    protected final Self release(Key key) {
        return emulate(key, false, true);
    }

    /**
     * <p>
     * Emulate press and release event in series.
     * </p>
     * 
     * @param keys
     * @return
     */
    protected final Self input(Key... keys) {
        for (Key key : keys) {
            emulate(key, true, true);
        }
        return (Self) this;
    }

    /**
     * <p>
     * Emulate press and release event in parallel.
     * </p>
     * 
     * @param keys
     * @return
     */
    protected final Self inputParallel(Key... keys) {
        for (int i = 0; i < keys.length; i++) {
            emulate(keys[i], true, false);
        }

        for (int i = keys.length - 1; 0 <= i; i--) {
            emulate(keys[i], false, true);
        }
        return (Self) this;
    }

    /**
     * <p>
     * Retrieve the active window.
     * </p>
     * 
     * @return
     */
    protected final Window window() {
        return Window.now();
    }

    /**
     * <p>
     * Emulate input event.
     * </p>
     * 
     * @param key
     * @param press
     * @param release
     * @return
     */
    private final Self emulate(Key key, boolean press, boolean release) {
        if (key.mouse) {
            INPUT ip = new INPUT();
            ip.type = new DWORD(INPUT.INPUT_MOUSE);
            ip.input.setType("mi");

            if (press) {
                ip.input.mi.dwFlags = new DWORD(key.on | 0x8000); // MOUSEEVENTF_ABSOLUTE
                User32.INSTANCE.SendInput(new DWORD(1), new INPUT[] {ip}, ip.size());
            }

            if (release) {
                ip.input.mi.dwFlags = new DWORD(key.off | 0x8000); // MOUSEEVENTF_ABSOLUTE
                User32.INSTANCE.SendInput(new DWORD(1), new INPUT[] {ip}, ip.size());
            }
        } else {
            INPUT ip = new INPUT();
            ip.type = new DWORD(INPUT.INPUT_KEYBOARD);
            ip.input.setType("ki");
            ip.input.ki.wVk = new WORD(key.virtualCode);
            ip.input.ki.wScan = new WORD(key.scanCode);

            if (press) {
                ip.input.ki.dwFlags = new DWORD(KEYBDINPUT.KEYEVENTF_SCANCODE);
                User32.INSTANCE.SendInput(new DWORD(1), new INPUT[] {ip}, ip.size());
            }

            if (release) {
                ip.input.ki.dwFlags = new DWORD(KEYBDINPUT.KEYEVENTF_KEYUP | KEYBDINPUT.KEYEVENTF_SCANCODE);
                User32.INSTANCE.SendInput(new DWORD(1), new INPUT[] {ip}, ip.size());
            }
        }
        return (Self) this;
    }

    protected final Self mouseMoveTo(int x, int y) {
        INPUT ip = new INPUT();
        ip.type = new DWORD(INPUT.INPUT_MOUSE);
        ip.input.setType("mi");
        ip.input.mi.dx = new LONG(x * ScaleX);
        ip.input.mi.dy = new LONG(y * ScaleY);
        ip.input.mi.dwFlags = new DWORD(0x0001 | 0x8000); // MOUSEEVENTF_MOVE | MOUSEEVENTF_ABSOLUTE
        User32.INSTANCE.SendInput(new DWORD(1), new INPUT[] {ip}, ip.size());
        return (Self) this;
    }

    /**
     * <p>
     * Stop macro temporary.
     * </p>
     * 
     * @param ms A time to stop.
     * @return
     */
    protected final Self delay(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw I.quiet(e);
        }
        return (Self) this;
    }

    /**
     * <p>
     * Declare the condition of macro activation.
     * </p>
     * 
     * @param condition
     */
    protected final void require(Predicate<Window> condition, Runnable definitions) {
        Predicate<Window> stored = windowCondition;

        windowCondition = windowCondition.and(condition);
        definitions.run();

        windowCondition = stored;
    }

    /**
     * <p>
     * Declare the condition of macro activation.
     * </p>
     * 
     * @param condition
     */
    protected final void requireTitle(String title, Runnable definitions) {
        require(window -> window.title().contains(title), definitions);
    }
}
