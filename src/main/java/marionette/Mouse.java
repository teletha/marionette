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

/**
 * @version 2016/10/04 9:41:24
 */
public enum Mouse {

    Move(0x0001, 0x0001, null),

    Wheel(0x0800, 0x0800, null),

    WheelTilt(0x01000, 0x01000, null);

    /**
     * <p>
     * Define mouse action.
     * </p>
     * 
     * @param startAction
     * @param endAction
     */
    private Mouse(int startAction, int endAction, Key key) {
    }
}