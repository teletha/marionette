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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

import kiss.Disposable;
import kiss.I;
import kiss.Signal;
import marionette.platform.Color;
import marionette.platform.Location;
import marionette.platform.Native;

/**
 * @version 2016/08/01 13:59:45
 */
public class Window {

    /** ID */
    private final Object windowID;

    /**
     * @param windowID
     */
    private Window(Object windowID) {
        this.windowID = windowID;
    }

    /**
     * <p>
     * ウインドウタイトルを返します。
     * </p>
     * 
     * @return
     */
    public String title() {
        return Native.API.getWindowTitle(windowID);
    }

    /**
     * <p>
     * プロセスを返します。
     * </p>
     * 
     * @return
     */
    public ProcessHandle.Info process() {
        return ProcessHandle.of(Native.API.getWindowProcessId(windowID)).map(process -> process.info()).orElseThrow();
    }

    /**
     * <p>
     * プロセス名を返します。
     * </p>
     * 
     * @return
     */
    public String processName() {
        String command = process().command().orElseThrow();
        int index = command.lastIndexOf(File.separatorChar);
        return index == -1 ? command : command.substring(index + 1);
    }

    /**
     * <p>
     * ウインドウを閉じます。（同期）
     * </p>
     */
    public void close() {
        Native.API.closeWindow(windowID);
    }

    /**
     * <p>
     * ウインドウを閉じます。（同期）
     * </p>
     */
    public void restart() {
        process().command().ifPresent(command -> {
            close();

            try {
                new ProcessBuilder(command).start();
            } catch (IOException e) {
                throw I.quiet(e);
            }
        });
    }

    /**
     * <p>
     * ウインドウにキー入力を送ります。（同期）
     * </p>
     */
    public void input(Key key) {
        Native.API.input(windowID, key);
    }

    /**
     */
    public Color color(int locationX, int locationY) {
        Location window = windowPosition();
        return color(window.slide(locationX, locationY));
    }

    /**
     */
    public Color color(Location location) {
        return Native.API.getColor(location);
    }

    /**
     * Locate the window related position.
     * 
     * @param locationX
     * @param locationY
     * @return
     */
    public Location locate(int locationX, int locationY) {
        return windowPosition().slide(locationX, locationY);
    }

    /**
     * ウインドウの位置及びサイズと取得。
     */
    public Location windowPosition() {
        return Native.API.getWindowPosition(windowID);
    }

    /**
     * Retrieve the current mouse relative position form this window.
     * 
     * @return
     */
    public Location mousePosition() {
        return windowPosition().relative(Native.API.getCursorPosition());
    }

    public boolean isMinified() {
        return Native.API.isMinified(windowID);
    }

    public void minimize() {
        Native.API.minimize(windowID);
    }

    public void maximize() {
        Native.API.maximize(windowID);
    }

    public void restore() {
        Native.API.restore(windowID);
    }

    /**
     * Check whetner the specified title window exists or not.
     */
    public static boolean existByTitle(String title) {
        return findByTitle(title).to().v != null;
    }

    /**
     * @param path
     */
    public static boolean existByTitle(Path path) {
        return existByTitle(path.getFileName().toString());
    }

    /**
     * @param path
     */
    public static void close(Path path) {
        Window.findByTitle(path).to().v.close();
    }

    /**
     * @param path
     */
    public static void open(Path path) {
    }

    /**
     * Get the current window.
     * 
     * @return
     */
    public static Window now() {
        return new Window(Native.API.activeWindow());
    }

    /**
     * <p>
     * List up all {@link Window}.
     * </p>
     * 
     * @return
     */
    public static Signal<Window> find() {
        return new Signal<Window>((observer, disposer) -> {
            Native.API.enumWindows(id -> {
                observer.accept(new Window(id));
            });
            observer.complete();
            return Disposable.empty();
        });
    }

    /**
     * List up all {@link Window} which contains the specified title.
     * 
     * @param titles A part of title.
     * @return
     */
    public static Signal<Window> findByTitle(String... titles) {
        return find().take(window -> Stream.of(titles).anyMatch(x -> window.title().contains(x)));
    }

    /**
     * List up all {@link Window} which contains the specified title.
     * 
     * @param title A part of title.
     * @return
     */
    public static Signal<Window> findByTitle(Path title) {
        return findByTitle(title.getFileName().toString());
    }

}