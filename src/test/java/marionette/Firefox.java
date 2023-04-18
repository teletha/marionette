/*
 * Copyright (C) 2023 The MARIONETTE Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package marionette;

public class Firefox extends Macro<Firefox> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void declare() {
        whenPress(Key.F2).to(() -> {
            Window.find().take(window -> window.title().contains("YouTube")).to(window -> {
                System.out.println(window);
                window.input(Key.F12);
            });
        });
    }
}