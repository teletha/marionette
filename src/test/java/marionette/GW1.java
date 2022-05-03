/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package marionette;

public class GW1 extends AbstractMacro<GW1> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void declare() {
        require(window -> window.title().contains("Teletha"), () -> {
            whenPress(Key.MouseMiddle).to(() -> {
                input(Key.H).delay(250).input(Key.MouseLeft);
            });

            whenPress(Key.F2, MacroOption.IgnoreEvent).to(() -> {
                input(Key.T, Key.Y, Key.I);
                delay(1500);
                input(Key.T, Key.U, Key.O);
                delay(1500);
                input(Key.P);
                delay(1500);
                input(Key.AtMark);
            });
        });
    }
}
