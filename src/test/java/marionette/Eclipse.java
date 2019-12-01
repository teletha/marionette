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

import static marionette.macro.MacroOption.*;

import marionette.macro.AbstractMacro;
import marionette.macro.Key;
import marionette.macro.Macro;

public class Eclipse extends AbstractMacro<Eclipse> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void declare() {
        require(window -> window.title().contains("Eclipse"), () -> {
            shortcut(Key.O, Key.Up);
            shortcut(Key.K, Key.Left);
            shortcut(Key.L, Key.Down);
            shortcut(Key.SemiColon, Key.Right);
        });
    }

    private void shortcut(Key key, Key target) {
        whenPress(key, IgnoreEvent).to(e -> {
            if (Key.Control.isPressed()) {
                press(target);
            } else {
                press(key);
            }
        });
    }

    /**
     * Entry point.
     */
    public static void main(String[] args) {
        Macro.launch().useTrayIcon().use(Eclipse.class);
    }
}
