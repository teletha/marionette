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
public class Mirage3 extends Mesmer {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void professionSpesific() {
        debugByMouse();

        // whenUseSkill4().merge(whenUseSkill2()).to(e -> {
        // if (hasClone2()) {
        // useShtterSkill();
        // }
        // });

        whenUseSkill2().merge(whenUseSkill4()).to(() -> {
            if (hasClone2()) {
                useShtterSkill();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performDodge() {
        if (hasFullEnergy()) {
            super.performDodge();
            useShtterSkill();
        } else if (canActivateUtilitySkill1()) {
            useUtilitySkill1();
        } else if (canActivateUtilitySkill3()) {
            useUtilitySkill3();
        } else if (canActivateProfessionSkill4()) {
            useProfessionSkill4();
        } else {
            super.performDodge();
            useShtterSkill();
        }
    }

    /**
     * Entry point.
     */
    public static void main(String[] args) {
        Macro.launch().useTrayIcon().use(Mirage3.class);
    }
}
