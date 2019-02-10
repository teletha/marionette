/*
 * Copyright (C) 2019 Marionette Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package marionette.macro;

import java.awt.Font;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import kiss.I;
import kiss.Manageable;
import kiss.Singleton;

@Manageable(lifestyle = Singleton.class)
public class Macro {

    /** The application font. */
    private static final Font font = new Font("MeiryoKe_UIGothic", Font.PLAIN, 12);

    /** The active state. */
    private boolean paused;

    /** The tray icon. */
    private TrayIcon tray;

    /**
     * Create new macro manager.
     */
    private Macro() {
        GlobalEvents.initializeNativeHook();
    }

    /**
     * Config application.
     * 
     * @return Chainable API.
     */
    public synchronized Macro useTrayIcon() {
        if (tray == null) {
            try {
                tray = new TrayIcon(ImageIO.read(Macro.class.getResource("icon.png")));
                tray.setImageAutoSize(true);
                tray.setPopupMenu(menu());
                SystemTray.getSystemTray().add(tray);
            } catch (Exception e) {
                throw I.quiet(e);
            }
        }
        return this;
    }

    /**
     * Use the specified macro.
     */
    public <M extends AbstractMacro> Macro use(Class<M> clazz) {
        return use(I.make(clazz));
    }

    /**
     * Use the specified macro.
     */
    public Macro use(AbstractMacro macro) {
        if (macro != null) {
            macro.declare();
        }
        return this;
    }

    /**
     * Create popup menu.
     */
    private PopupMenu menu() {
        PopupMenu popup = new PopupMenu();
        popup.add(item("Reload", this::restart));
        popup.add(item("Quit", this::suspend));
        popup.setFont(font);

        return popup;
    }

    /**
     * <p>
     * Create menu item.
     * </p>
     * 
     * @param name
     * @param action
     * @return
     */
    private MenuItem item(String name, Runnable action) {
        MenuItem item = new MenuItem(name);
        item.addActionListener(e -> action.run());
        item.setFont(font);

        return item;
    }

    /**
     * Pause all macro temporary or resume now.
     * 
     * @return
     */
    public Macro pauseOrResume() {
        if (paused) {
            paused = false;
        } else {
            paused = true;
        }
        return this;
    }

    /**
     * Retrieve the active state.
     * 
     * @return
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * <p>
     * Restart this macro with JVM.
     * </p>
     */
    protected final void restart() {
        ArrayList<String> commands = new ArrayList();

        // Java
        commands.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
        commands.addAll(ManagementFactory.getRuntimeMXBean().getInputArguments());

        // classpath
        commands.add("-cp");
        commands.add(ManagementFactory.getRuntimeMXBean().getClassPath());

        // Class to be executed
        commands.add(getClass().getName());

        try {
            new ProcessBuilder(commands).start();
            suspend();
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * Suspend this macro.
     */
    protected final void suspend() {
        GlobalEvents.disposeNativeHook();
        System.exit(0);
    }

    /**
     * Launch macro application.
     * 
     * @return
     */
    public static final Macro launch() {
        return I.make(Macro.class);
    }
}
