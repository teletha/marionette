/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package marionette;

import marionette.macro.AbstractMacro;
import marionette.macro.Key;
import marionette.macro.Macro;

/**
 * 
 */
public class GW extends AbstractMacro {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declare() {
        requireTitle("Guild Wars 2", () -> {
            when(Key.A).press().to(() -> {
                System.out.println("OK A in GW2");
            });
        });

        when(Key.K).press().to(() -> {
            System.out.println("OK K");
        });
    }

    /**
     * Entry point.
     */
    public static void main(String[] args) {
        Macro.launch().useTrayIcon().use(GW.class);
    }
}
