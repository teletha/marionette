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

import static marionette.MacroOption.IgnoreEvent;

public class Eclipse extends Macro<Eclipse> {

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

            whenPress(Key.Return).to(e -> {
                System.out.println(e);
            });
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