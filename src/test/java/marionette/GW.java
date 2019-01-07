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
            when(Key.AtMark).press().to(() -> {
                System.out.println("OK");
                input(Key.A);
            });
        });

        when(Key.AtMark).press().to(() -> {
            System.out.println("OK " + Key.Up.scanCode);
            input(Key.Up);
            input(Key.A);
            input(Key.B);
            input(Key.N2);
        });
    }

    /**
     * Entry point.
     */
    public static void main(String[] args) {
        Macro.launch().useTrayIcon().use(GW.class);
    }
}
