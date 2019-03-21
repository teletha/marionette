/*
 * Copyright (C) 2019 Marionette Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package marionette;

import marionette.macro.Macro;

/**
 * 
 */
public class Mirage2 extends Mesmer {

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
        Macro.launch().useTrayIcon().use(Mirage2.class);
    }
}
