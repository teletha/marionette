/*
 * Copyright (C) 2024 The MARIONETTE Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package marionette;

public class LoL extends Macro<LoL> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void declare() {
        require(window -> window.title().contains("League of Legends"), () -> {
            whenPress(Key.A).to(() -> {
                input(Key.P).delay(300).input(Key.O);
            });
        });
    }
}