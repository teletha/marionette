/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package marionette.macro;

import kiss.Signal;

/**
 * @version 2016/10/02 18:01:25
 */
public interface MacroDSL {

    /**
     * <p>
     * Consume the native event.
     * </p>
     * 
     * @return
     */
    MacroDSL consume();

    /**
     * <p>
     * Declare press event.
     * </p>
     * 
     * @return
     */
    Signal<KeyEvent> press();

    /**
     * <p>
     * Declare release event.
     * </p>
     * 
     * @return
     */
    Signal<KeyEvent> release();

    /**
     * <p>
     * Declare key modifier.
     * </p>
     * 
     * @return Chainable DSL.
     */
    MacroDSL withAlt();

    /**
     * <p>
     * Declare key modifier.
     * </p>
     * 
     * @return Chainable DSL.
     */
    MacroDSL withCtrl();

    /**
     * <p>
     * Declare key modifier.
     * </p>
     * 
     * @return Chainable DSL.
     */
    MacroDSL withShift();
}