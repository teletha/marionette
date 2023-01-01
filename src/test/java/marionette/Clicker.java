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

import java.util.concurrent.TimeUnit;

import kiss.Disposable;
import kiss.I;
import marionette.platform.Native;

public class Clicker extends Macro<Clicker> {

    private Disposable autoClick;

    /**
     * {@inheritDoc}
     */
    @Override
    public void declare() {
        debugByMouse();

        require(window -> window.title().contains("億万長者"), () -> {
            whenPress(Key.MouseRight, MacroOption.IgnoreEvent).to(() -> {
                if (autoClick == null) {
                    autoClick = I.schedule(0, 120, TimeUnit.MILLISECONDS, true).to(() -> input(Key.MouseLeft));
                } else {
                    autoClick.dispose();
                    autoClick = null;
                }
            });

            whenPress(Key.NumPad0, MacroOption.IgnoreEvent).to(() -> {
                chech(2590, 236);
                chech(2676, 236);
                chech(2762, 236);
                chech(2852, 236);
                chech(2937, 236);
                chech(3025, 236);
                chech(3113, 236);
            });
        });
    }

    private void chech(int x, int y) {
        int delay = 10;

        if (Native.API.getColor(x, y).green < 80) {
            System.out.println("左");
            mouseMoveTo(2707, 1138).delay(delay).input(Key.MouseLeft).delay(delay);
        } else if (Native.API.getColor(x + 50, y).green < 50) {
            System.out.println("右");
            mouseMoveTo(3046, 1138).delay(delay).input(Key.MouseLeft).delay(delay);
        } else if (Native.API.getColor(x + 30, y - 20).green < 50) {
            System.out.println("上");
            mouseMoveTo(2883, 965).delay(delay).input(Key.MouseLeft).delay(delay);
        } else if (Native.API.getColor(x + 30, y + 20).green < 50) {
            System.out.println("下");
            mouseMoveTo(2883, 1146).delay(delay).input(Key.MouseLeft).delay(delay);
        } else {
            System.out.println("無");
        }
    }
}