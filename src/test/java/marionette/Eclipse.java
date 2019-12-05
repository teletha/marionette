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

import static marionette.macro.MacroOption.IgnoreEvent;

import marionette.macro.AbstractMacro;
import marionette.macro.Key;

public class Eclipse extends AbstractMacro<Eclipse> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void declare() {
        require(window -> window.title().contains("Eclipse"), () -> {
            shortcut(Key.P, Key.Up);
            shortcut(Key.L, Key.Left);
            shortcut(Key.SemiColon, Key.Down);
            shortcut(Key.Colon, Key.Right);
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
}
