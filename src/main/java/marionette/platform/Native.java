/*
 * Copyright (C) 2024 The MARIONETTE Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package marionette.platform;

import static com.sun.jna.Platform.*;

import java.util.function.Consumer;

import kiss.Signal;
import marionette.Key;

/**
 * @version 2016/10/04 21:02:26
 */
public interface Native<ID> {

    /** The actual API for the current platform. */
    Native API = isWindows() ? new WindowsAPI() : new UnknownAPI();

    /**
     * <p>
     * Retrieve the absolute cursor position.
     * </p>
     * 
     * @return
     */
    Location getCursorPosition();

    /**
     * <p>
     * Retrieve color in the specified pixel.
     * </p>
     * 
     * @param x
     * @param y
     * @return
     */
    Color getColor(int x, int y);

    /**
     * <p>
     * Retrieve the color in the specified pixel.
     * </p>
     * 
     * @param location
     */
    Color getColor(Location location);

    /**
     * <p>
     * Retrieve the acttive client rect.
     * </p>
     * 
     * @return
     */
    int[] getActiveClientRect();

    /**
     * <p>
     * Retrieve the specified window rect.
     * </p>
     * 
     * @param windowID
     * @return
     */
    Location getWindowPosition(ID windowID);

    /**
     * <p>
     * Retrieve the specified window title.
     * </p>
     * 
     * @param windowID
     * @return
     */
    String getWindowTitle(ID windowID);

    /**
     * <p>
     * Retrieve the specified window's process id.
     * </p>
     * 
     * @param windowID
     */
    int getWindowProcessId(ID windowID);

    /**
     * <p>
     * Close the specified window.
     * </p>
     * 
     * @param windowID
     */
    void closeWindow(ID windowID);

    /**
     * <p>
     * Retrieve the active window id.
     * </p>
     * 
     * @return
     */
    ID activeWindow();

    /**
     * <p>
     * List up all windows.
     * </p>
     * 
     * @param process
     */
    void enumWindows(Consumer<ID> process);

    /**
     * Execute the command.
     * 
     * @param command
     */
    void execute(Object... command);

    /**
     * Execute OCR.
     * 
     * @param x
     * @param y
     * @param width
     * @param height
     * @return
     */
    String ocr(int x, int y, int width, int height);

    /**
     * Listen the change of clipboard.
     * 
     * @return
     */
    Signal<String> clipboard();

    /**
     * <p>
     * Input key sequence to the specified window.
     * </p>
     * 
     * @param windowID
     */
    void input(ID windowID, Key key);

    /**
     * Switch IME mode to Hiragana.
     */
    void imeHiragana();

    /**
     * Switch IME mode to Katakana.
     */
    void imeKatakana();

    /**
     * Make off IME mode.
     */
    void imeOff();

    /**
     * Test whether the window is minified or not.
     * 
     * @return
     */
    boolean isMinified(ID windowID);

    /**
     * Test whether the window is maxmized or not.
     * 
     * @return
     */
    boolean isMaximized(ID windowID);

    /**
     * Minimize the window.
     */
    void minimize(ID windowID);

    /**
     * Maximize the window
     */
    void maximize(ID windowID);

    /**
     * Restore the window
     */
    void restore(ID windowID);
}