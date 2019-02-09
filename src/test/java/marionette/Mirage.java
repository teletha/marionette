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

import marionette.macro.Macro;

/**
 * 
 */
public class Mirage extends Mesmer {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void professionSpesific() {
        debugByMouse();

        whenUseSkill4().merge(whenUseSkill2()).to(e -> {
            if (hasClone2()) {
                useShtterSkill();
            }
        });
    }

    /**
     * Entry point.
     */
    public static void main(String[] args) {
        Macro.launch().useTrayIcon().use(Mirage.class);
    }
}
