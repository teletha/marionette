/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package marionette;

import marionette.macro.AbstractMacro;
import marionette.macro.Key;
import marionette.macro.Macro;

/**
 * 
 */
public class GW extends AbstractMacro<GW> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declare() {
        // requireTitle("Guild Wars 2", () -> {
        // whenPress(Key.MouseRight, IgnoreEvent).to(() -> {
        // });
        //
        // whenRelease(Key.MouseRight, IgnoreEvent).to(() -> {
        // input(Key.MouseRight);
        // });
        //
        // whenPress(Key.Shift).to(e -> {
        // input(Key.AtMark);
        // System.out.println("AAA");
        // });
        //
        // whenRelease(Key.Shift).to(e -> {
        // input(Key.AtMark);
        // System.out.println("BBB");
        // });
        // });

        whenPress(Key.Shift).to(e -> {
            System.out.println("AAA");
            input(Key.AtMark);

        });

        //
        // whenGesture(Key.MouseRight).to(e -> {
        // switch (e) {
        // case "U":
        // press(Key.Up);
        // release(Key.Down);
        // release(Key.Left);
        // release(Key.Right);
        // break;
        //
        // case "D":
        // release(Key.Up);
        // press(Key.Down);
        // release(Key.Left);
        // release(Key.Right);
        // break;
        //
        // case "L":
        // release(Key.Up);
        // release(Key.Down);
        // press(Key.Left);
        // release(Key.Right);
        // break;
        //
        // case "R":
        // release(Key.Up);
        // release(Key.Down);
        // release(Key.Left);
        // press(Key.Right);
        // break;
        // }
        // });
    }

    /**
     * Entry point.
     */
    public static void main(String[] args) {
        Macro.launch().useTrayIcon().use(GW.class);
    }
}
