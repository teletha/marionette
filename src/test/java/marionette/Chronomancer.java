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

/**
 * 
 */
public class Chronomancer extends Mesmer {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void professionSpesific() {
        whenUseSkill1().merge(whenUseSkill2(), whenUseSkill3(), whenUseSkill4(), whenUseSkill5()).take(this::hasClone3).to(() -> {
            useShtterSkill();
        });

        whenUseSkill1().to(e -> {
            if (canActivateWeaponSkill3()) {
                useWeaponSkill3();
                await(2250);
            } else if (canActivateWeaponSkill5()) {
                useWeaponSkill5();
                await();
            }
            useWeaponSkill1();
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performDodge() {
        if (canActivateWeaponSkill2()) {
            useWeaponSkill2();
        } else if (canActivateWeaponSkill4()) {
            useWeaponSkill4();
        } else {
            super.performDodge();
        }
    }

    /**
     * Check clone existence.
     * 
     * @return
     */
    @Override
    protected boolean hasClone1() {
        return !window().color(719, 1059).is(2893860);
    }

    /**
     * Check clone existence.
     * 
     * @return
     */
    @Override
    protected boolean hasClone2() {
        return !window().color(754, 1061).is(2893860);
    }

    /**
     * Check clone existence.
     * 
     * @return
     */
    @Override
    protected boolean hasClone3() {
        return !window().color(786, 1061).is(2893859);
    }
}
