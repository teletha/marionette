/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package marionette.browser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import kiss.I;

/**
 * @version 2018/10/01 2:22:55
 */
public class BrowserInitialPreference extends BrowserPreference {

    /** The secret mode. */
    public boolean secret = false;

    /** The headless mode. */
    public boolean headless = false;

    /** The adblock mode. */
    public boolean adblock = true;

    /** The tor mode. */
    public boolean tor = false;

    /** The profile directory. */
    Path profileDirectory;

    /** The browser options. */
    final List<String> options = I
            .list("--disable-remote-fonts", "--disable-content-prefetch", "--dns-prefetch-disable", "--log-level=3", "--silent");

    /**
     * Configure profile directory.
     * 
     * @param directory
     * @return
     */
    public final BrowserInitialPreference profile(String directory) {
        return profile(Paths.get(directory));
    }

    /**
     * Configure profile directory.
     * 
     * @param directory
     * @return
     */
    public final BrowserInitialPreference profile(Path directory) {
        if (directory != null) {
            try {
                Files.createDirectories(directory);

                this.profileDirectory = directory;
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }
        return this;
    }
}