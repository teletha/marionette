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

import kiss.Signal;
import marionette.macro.AbstractMacro;
import marionette.macro.Key;
import marionette.macro.Macro;
import marionette.macro.Mouse;

public class GW extends AbstractMacro<GW> {

    /** The current movemnt state. */
    private State state = State.None;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declare() {
        requireTitle("Guild Wars 2", () -> {
            // dodge
            whenPress(Key.MouseMiddle).to(() -> {
                input(state.key);
                input(state.key);
                if (state != State.None) press(state.key);
            });

            // movement
            whenFastGesture(Key.MouseRight).to(e -> {
                state = e;

                // reset move to all directions
                release(Key.Up);
                release(Key.Down);
                release(Key.Right);
                release(Key.Left);

                // move to the current direction
                if (e != State.None) press(e.key);
            });
        });
    }

    /**
     * <p>
     * Declare key related event.
     * </p>
     * 
     * @param key
     * @return
     */
    private final Signal<State> whenFastGesture(Key key) {
        int[] last = new int[2];
        State[] lastDirection = new State[1];

        return when(Mouse.Move).skipUntil(whenPress(key)).takeUntil(whenRelease(key)).effectOnce(e -> {
            // save start position
            last[0] = e.x();
            last[1] = e.y();
            lastDirection[0] = null;
        }).map(e -> {
            int x = e.x();
            int y = e.y();
            int distanceX = Math.abs(last[0] - x);
            int distanceY = Math.abs(last[1] - y);

            // minimal movement where the gesture is recognized
            int min = 7;
            if (distanceX < min && distanceY < min) {
                return lastDirection[0];
            }

            // determine current direction
            State direction;
            if (distanceX > distanceY) {
                direction = x < last[0] ? State.Left : State.Right;
            } else {
                direction = y < last[1] ? State.Up : State.Down;
            }

            // save current position
            last[0] = x;
            last[1] = y;
            lastDirection[0] = direction;

            return direction;
        }).diff().or(State.None).repeat();
    }

    /**
     * Entry point.
     */
    public static void main(String[] args) {
        Macro.launch().useTrayIcon().use(GW.class);
    }

    /**
     * 
     */
    private enum State {
        Up(Key.Up), Down(Key.Down), Left(Key.Left), Right(Key.Right), None(Key.Down);

        private Key key;

        /**
         * @param key
         */
        private State(Key key) {
            this.key = key;
        }
    }
}
