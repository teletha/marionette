/*
 * Copyright (C) 2020 marionette Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package marionette;

public class Picasa extends Macro<Picasa> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void declare() {
        require(window -> window.title().contains("Picasa"), () -> {
            whenPress(Key.N0, MacroOption.IgnoreEvent).to(e -> {
                input(Key.AltLeft);
                input(Key.O);
                input(Key.D);
                input(Key.Tab);
                input(Key.Return);
            });
        });
    }
}