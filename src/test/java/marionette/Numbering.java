/*
 * Copyright (C) 2022 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package marionette;

import kiss.I;

public class Numbering extends AbstractMacro<Numbering> {

    private static boolean on = true;

    private int counter;

    /**
     * {@inheritDoc}
     */
    @Override
    public void declare() {
        require(window -> window.title().contains("墓地"), () -> {
            whenPress(Key.MouseMiddle).to(v -> {
                System.out.println(counter++);
            });
        });

        whenPress(Key.F12).to(v -> {
            on = false;
        });
    }

    public static void main(String[] args) {
        GlobalEvents.initializeNativeHook();

        try {
            Thread.sleep(1000 * 3);
            System.out.println("OK");
            new Numbering().declare();
            while (on) {
                System.out.println("O");
                Thread.sleep(1000 * 3);
            }
        } catch (InterruptedException e) {
            throw I.quiet(e);
        }
        GlobalEvents.disposeNativeHook();
    }
}
