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

import marionette.platform.Location;

/**
 * @version 2016/10/16 10:26:38
 */
public interface KeyEvent {

    /**
     * <p>
     * Compute the mouse location when event was occured.
     * </p>
     * 
     * @return
     */
    int x();

    /**
     * <p>
     * Compute the mouse location when event was occured.
     * </p>
     * 
     * @return
     */
    int y();

    /**
     * <p>
     * Compute the time when event was occured.
     * </p>
     * 
     * @return
     */
    long time();

    /**
     * Compute the mouse wheel delta.
     * 
     * @return
     */
    double delta();

    default Location location() {
        return Location.of(x(), y());
    }

    /**
     * @param key
     * @return
     */
    static KeyEvent of(Key key) {
        long time = System.currentTimeMillis();

        return new KeyEvent() {

            /**
             * {@inheritDoc}
             */
            @Override
            public int y() {
                return 0;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public int x() {
                return 0;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public long time() {
                return time;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public double delta() {
                return 0;
            }
        };
    }
}