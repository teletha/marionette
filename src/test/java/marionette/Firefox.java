/*
 * Copyright (C) 2019 Marionette Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package marionette;

import marionette.macro.AbstractMacro;
import marionette.macro.Key;
import marionette.macro.Window;

public class Firefox extends AbstractMacro<Firefox> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void declare() {
        whenPress(Key.MouseRight).to(() -> {
            System.out.println("OK");
            delay(200);
            input(Key.D);
            delay(200);
            input(Key.Tab);
            delay(200);
            input(Key.Return);
            delay(200);
        });
    }

    private void gesture(String type) {
        switch (type) {
        case "R":
            input(Key.Alt, () -> input(Key.Right));
            break;

        case "L":
            input(Key.Alt, () -> input(Key.Left));
            break;

        case "D":
            input(Key.Control, Key.Shift, () -> input(Key.T));
            break;

        case "DU":
            input(Key.Control, Key.Shift, () -> input(Key.A));
            break;

        case "DUD":
            Window.now().restart();
            break;

        default:
            break;
        }
    }
}
