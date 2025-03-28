/*
 * Copyright (C) 2025 The MARIONETTE Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package marionette.platform;

import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Shell32;
import com.sun.jna.platform.win32.ShellAPI.SHELLEXECUTEINFO;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HDC;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.POINT;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinNT.HRESULT;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.MSG;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

import kiss.I;
import kiss.Observer;
import kiss.Signal;
import kiss.Variable;
import kiss.WiseTriFunction;
import marionette.Key;

class WindowsAPI implements marionette.platform.Native<HWND> {

    /** Instance of USER32.DLL for use in accessing native functions. */
    private static final GDI GDI = Native.load("gdi32", GDI.class, W32APIOptions.DEFAULT_OPTIONS);

    /** Instance of USER32.DLL for use in accessing native functions. */
    private static final Shell Shell = Native.load("shell32", Shell.class, W32APIOptions.DEFAULT_OPTIONS);

    /** Instance of USER32.DLL for use in accessing native functions. */
    private static final User User = Native.load("user32", User.class, W32APIOptions.DEFAULT_OPTIONS);

    /** Instance of USER32.DLL for use in accessing native functions. */
    private static final Kernel Kernel = Native.load("kernel32", Kernel.class, W32APIOptions.DEFAULT_OPTIONS);

    /** Instance of USER32.DLL for use in accessing native functions. */
    private static final Msctf MSCTF = Native.load("msctf.dll", Msctf.class);

    /** Instance of USER32.DLL for use in accessing native functions. */
    private static final Imm Imm = Native.load("imm32.dll", Imm.class);

    /** The native clipboard manager. */
    private static final Clipboard clipboard = new Clipboard();

    private final Variable<String> ocr = clipboard().startWith("0.8").to();

    /**
     * {@inheritDoc}
     */
    @Override
    public Color getColor(int x, int y) {
        return Color.of(GDI.GetPixel(User.GetDC(null), x, y));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Color getColor(Location location) {
        return getColor(location.x, location.y);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Location getCursorPosition() {
        POINT point = new POINT();
        User.GetCursorPos(point);
        return new Location(point.x, point.y);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] getActiveClientRect() {
        RECT rect = new RECT();
        User.GetClientRect(User.GetForegroundWindow(), rect);
        return new int[] {rect.top, rect.left, rect.bottom, rect.right};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Location getWindowPosition(HWND windowID) {
        RECT rect = new RECT();
        User.GetWindowRect(windowID, rect);
        return new Location(rect.left, rect.top, rect.left, rect.top, rect.right - rect.left, rect.bottom - rect.top);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getWindowTitle(HWND windowID) {
        return text(windowID, User::GetWindowText);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getWindowProcessId(HWND windowID) {
        IntByReference pid = new IntByReference();
        User.GetWindowThreadProcessId(windowID, pid);
        return pid.getValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void closeWindow(HWND windowID) {
        User.PostMessage(windowID, WinUser.WM_CLOSE, null, null);

        while (User.IsWindowVisible(windowID)) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw I.quiet(e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HWND activeWindow() {
        return User.GetForegroundWindow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enumWindows(Consumer<HWND> process) {
        User.EnumWindows((hwnd, pointer) -> {
            process.accept(hwnd);
            return true;
        }, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Object... command) {
        if (0 < command.length) {
            StringJoiner joiner = new StringJoiner(" ");

            for (int i = 1; i < command.length; i++) {
                joiner.add(String.valueOf(command[i]));
            }

            SHELLEXECUTEINFO info = new SHELLEXECUTEINFO();
            info.fMask = 0x00000040; // SEE_MASK_NOCLOSEPROCESS
            info.lpFile = String.valueOf(command[0]);
            info.lpParameters = joiner.toString();
            info.nShow = User32.SW_HIDE;
            Shell.ShellExecuteEx(info);
            Kernel32.INSTANCE.WaitForSingleObject(info.hProcess, 0);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String ocr(int x, int y, int width, int height) {
        try {
            execute("Capture2Text.exe", x, y, x + width, y + height);
            return ocr.v;
        } catch (Throwable e) {
            throw I.quiet(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<String> clipboard() {
        return clipboard.read().skip(e -> e == null).diff();
    }

    /**
     * <p>
     * Helper method to read text from the specified handle.
     * </p>
     * 
     * @param id
     * @param consumer
     * @return
     */
    private static <T> String text(T id, WiseTriFunction<T, char[], Integer, Integer> consumer) {
        char[] text = new char[512];
        int size = consumer.apply(id, text, text.length);
        return new String(text, 0, size);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void input(HWND windowID, Key key) {
        User.PostMessage(windowID, WinUser.WM_KEYDOWN, new WPARAM(key.virtualCode), new LPARAM(0));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void imeHiragana() {
        imeOn();
        MSCTF.SetInputScope(activeWindow(), 44);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void imeKatakana() {
        imeOn();
        MSCTF.SetInputScope(activeWindow(), 46);
    }

    private void imeOn() {
        HWND hwnd = activeWindow();
        Pointer context = Imm.ImmGetContext(hwnd);
        Imm.ImmSetOpenStatus(context, true);
        Imm.ImmReleaseContext(hwnd, context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void imeOff() {
        HWND hwnd = activeWindow();
        Pointer context = Imm.ImmGetContext(hwnd);
        Imm.ImmSetOpenStatus(context, false);
        Imm.ImmReleaseContext(hwnd, context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMinified(HWND windowID) {
        return (User.GetWindowLong(windowID, WinUser.GWL_STYLE) & WinUser.WS_MINIMIZE) != 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMaximized(HWND windowID) {
        return (User.GetWindowLong(windowID, WinUser.GWL_STYLE) & WinUser.WS_MAXIMIZE) != 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void minimize(HWND windowID) {
        User.SendMessage(windowID, WinUser.WM_SYSCOMMAND, new WPARAM(WinUser.SC_MINIMIZE), new LPARAM(0));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void maximize(HWND windowID) {
        User.SendMessage(windowID, WinUser.WM_SYSCOMMAND, new WPARAM(WinUser.SC_MAXIMIZE), new LPARAM(0));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void restore(HWND windowID) {
        User.ShowWindow(windowID, WinUser.SW_RESTORE);
        // User.SendMessage(windowID, WinUser.WM_SYSCOMMAND, new WPARAM(WinUser.SW_RESTORE), new
        // LPARAM(0));
    }

    /**
     * @version 2016/10/12 12:34:21
     */
    private static interface Shell extends StdCallLibrary, Shell32 {

        /**
         * @param lpExecInfo
         *            <p>
         *            Type: <strong>SHELLEXECUTEINFO*</strong>
         *            </p>
         *            <p>
         *            A pointer to a <a href=
         *            "https://msdn.microsoft.com/en-us/library/windows/desktop/bb759784(v=vs.85).aspx">
         *            <strong xmlns="http://www.w3.org/1999/xhtml">SHELLEXECUTEINFO </strong></a>
         *            structure that contains and receives information about the application being
         *            executed.
         *            </p>
         * @return
         *         <p>
         *         Returns <strong>TRUE</strong> if successful; otherwise, <strong>FALSE</strong>.
         *         Call <a href=
         *         "https://msdn.microsoft.com/en-us/library/windows/desktop/ms679360(v=vs.85).aspx">
         *         <strong xmlns="http://www.w3.org/1999/xhtml">GetLastError </strong></a> for
         *         extended error information.
         *         </p>
         */
        @Override
        boolean ShellExecuteEx(SHELLEXECUTEINFO lpExecInfo);
    }

    /**
     * @version 2016/10/04 21:28:46
     */
    private static interface Kernel extends StdCallLibrary, Kernel32 {

        /**
         * <p>
         * Locks a global memory object and returns a pointer to the first byte of the object's
         * memory block.
         * </p>
         * 
         * @param hMem A handle to the global memory object. This handle is returned by either the
         *            GlobalAlloc or GlobalReAlloc function.
         * @return If the function succeeds, the return value is a pointer to the first byte of the
         *         memory block. If the function fails, the return value is NULL. To get extended
         *         error information, call GetLastError.
         */
        Pointer GlobalLock(Pointer hMem);

        /**
         * <p>
         * Decrements the lock count associated with a memory object that was allocated with
         * GMEM_MOVEABLE. This function has no effect on memory objects allocated with GMEM_FIXED.
         * </p>
         * <p>
         * Note The global functions have greater overhead and provide fewer features than other
         * memory management functions. New applications should use the heap functions unless
         * documentation states that a global function should be used. For more information, see
         * Global and Local Functions.
         * </p>
         * 
         * @param hMem A handle to the global memory object. This handle is returned by either the
         *            GlobalAlloc or GlobalReAlloc function.
         * @return If the memory object is still locked after decrementing the lock count, the
         *         return value is a nonzero value. If the memory object is unlocked after
         *         decrementing the lock count, the function returns zero and GetLastError returns
         *         NO_ERROR. If the function fails, the return value is zero and GetLastError
         *         returns a value other than NO_ERROR.
         */
        boolean GlobalUnlock(Pointer hMem);
    }

    /**
     * @version 2016/10/04 21:28:46
     */
    private static interface User extends StdCallLibrary, User32 {

        /**
         * Retrieves the position of the mouse cursor, in screen coordinates.
         *
         * @param p lpPoint [out]<br>
         *            Type: LPPOINT<br>
         *            A pointer to a POINT structure that receives the screen coordinates of the
         *            cursor.
         * @return Type: BOOL.<br>
         *         Returns nonzero if successful or zero otherwise. To get extended error
         *         information, call GetLastError.
         */
        @Override
        boolean GetCursorPos(POINT p);

        /**
         * This function retrieves the coordinates of a window's client area. The client coordinates
         * specify the upper-left and lower-right corners of the client area. Because client
         * coordinates are relative to the upper-left corner of a window's client area, the
         * coordinates of the upper-left corner are (0,0).
         *
         * @param hWnd Handle to the window.
         * @param rect Long pointer to a RECT structure that structure that receives the client
         *            coordinates. The left and top members are zero. The right and bottom members
         *            contain the width and height of the window.
         * @return If the function succeeds, the return value is nonzero. If the function fails, the
         *         return value is zero.
         */
        @Override
        boolean GetClientRect(HWND hWnd, RECT rect);

        /**
         * <p>
         * Opens the clipboard for examination and prSignal other applications from modifying the
         * clipboard content.
         * </p>
         * 
         * @param hWnd A handle to the window to be associated with the open clipboard. If this
         *            parameter is NULL, the open clipboard is associated with the current task.
         * @return If the function succeeds, the return value is nonzero. If the function fails, the
         *         return value is zero. To get extended error information, call GetLastError.
         */
        boolean OpenClipboard(HWND hWnd);

        /**
         * <p>
         * Closes the clipboard.
         * </p>
         * 
         * @return If the function succeeds, the return value is nonzero. If the function fails, the
         *         return value is zero. To get extended error information, call GetLastError.
         */
        boolean CloseClipboard();

        /**
         * <p>
         * Retrieves data from the clipboard in a specified format. The clipboard must have been
         * opened previously.
         * </p>
         * 
         * @param format A clipboard format. For a description of the standard clipboard formats,
         *            see Standard Clipboard Formats.
         * @return If the function succeeds, the return value is the handle to a clipboard object in
         *         the specified format. If the function fails, the return value is NULL. To get
         *         extended error information, call GetLastError.
         */
        Pointer GetClipboardData(int format);

        /**
         * <p>
         * Places the given window in the system-maintained clipboard format listener list.
         * </p>
         * 
         * @param hWnd A handle to the window to be placed in the clipboard format listener list.
         * @return Returns TRUE if successful, FALSE otherwise. Call GetLastError for additional
         *         details.
         */
        boolean AddClipboardFormatListener(HWND hWnd);

        /**
         * <p>
         * Removes the given window from the system-maintained clipboard format listener list.
         * </p>
         * 
         * @param hWnd A handle to the window to remove from the clipboard format listener list.
         * @return Returns TRUE if successful, FALSE otherwise. Call GetLastError for additional
         *         details.
         */
        boolean RemoveClipboardFormatListener(HWND hWnd);

        /**
         * <p>
         * Determines whether the clipboard contains data in the specified format.
         * </p>
         * 
         * @param format A standard or registered clipboard format. For a description of the
         *            standard clipboard formats, see Standard Clipboard Formats .
         * @return If the clipboard format is available, the return value is nonzero. If the
         *         clipboard format is not available, the return value is zero. To get extended
         *         error information, call GetLastError.
         */
        boolean IsClipboardFormatAvailable(int format);
    }

    /**
     * @version 2016/10/03 9:28:46
     */
    private static interface GDI extends StdCallLibrary, GDI32 {

        /**
         * The GetPixel function retrieves the red, green, blue (RGB) color value of the pixel at
         * the specified coordinates.
         *
         * @return The return value is either a scan code, a virtual-key code, or a character value,
         *         depending on the value of uCode and uMapType. If there is no translation, the
         *         return value is zero.
         */
        int GetPixel(HDC hdc, int x, int y);
    }

    /**
     * 
     */
    private static interface Msctf extends StdCallLibrary {
        HRESULT SetInputScope(HWND hwnd, int inputScope);
    }

    /**
     * 
     */
    private static interface Imm extends StdCallLibrary {
        Pointer ImmGetContext(HWND hwnd);

        void ImmSetOpenStatus(Pointer context, boolean status);

        void ImmReleaseContext(HWND hwnd, Pointer context);
    }

    /**
     * 
     */
    private static class Clipboard implements Runnable {

        /** The actual executor. */
        private static final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName(Clipboard.class.getSimpleName());
            thread.setDaemon(true);
            return thread;
        });

        /** The clipboard event listener manager. */
        private static final List<Observer> observers = new CopyOnWriteArrayList();

        /** The thread manager. */
        private static Future switcher;

        /**
         * <p>
         * Listen the change of clipboard.
         * </p>
         * 
         * @return
         */
        private Signal<String> read() {
            return new Signal<String>((observer, disposer) -> {
                if (observers.add(observer) && observers.size() == 1) {
                    switcher = executor.submit(this);
                }

                return () -> {
                    if (observers.remove(observer) && observers.isEmpty()) {
                        switcher.cancel(false);
                    }
                };
            });
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            HWND handle = User.CreateWindowEx(0, "STATIC", "", 0, 0, 0, 0, 0, null, null, null, null);

            if (User.AddClipboardFormatListener(handle)) {
                MSG message = new MSG();

                // WM_CLIPBOARDUPDATE == 0x31D
                while (User.GetMessage(message, null, 0x31D, 0x31D) != 0) {
                    try {
                        // accept unicode text only (CF_UNICODETEXT == 13)
                        if (User.OpenClipboard(null) && User.IsClipboardFormatAvailable(13)) {
                            Pointer data = User.GetClipboardData(13);
                            Pointer locked = Kernel.GlobalLock(data);
                            String text = locked.getWideString(0);
                            Kernel.GlobalUnlock(data);

                            for (Observer observer : observers) {
                                observer.accept(text);
                            }
                        }
                    } finally {
                        User.CloseClipboard();
                    }
                }
            }
        }
    }
}