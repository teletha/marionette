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

import static marionette.macro.MacroOption.*;

/**
 * @version 2018/11/19 9:21:28
 */
public class OwnOperation extends AbstractMacro {

    /** The macro instance. */
    private final Macro macro;

    /**
     * Appilcation operator macro.
     * 
     * @param macro
     */
    public OwnOperation(Macro macro) {
        this.macro = macro;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declare() {
        whenPress(Key.Pause).to(macro::pauseOrResume);
        whenPress(Key.Pause, WithCtrl).to(macro::restart);
    }
}
