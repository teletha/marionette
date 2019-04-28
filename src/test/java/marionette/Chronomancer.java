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
public class Chronomancer extends Mesmer {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void professionSpesific() {
        debugByMouse();

        whenUseSkill1().merge(whenUseSkill2(), whenUseSkill3(), whenUseSkill4(), whenUseSkill5()).take(this::hasClone3).to(() -> {
            useShtterSkill();
        });
    }

    /**
     * Check clone existence.
     * 
     * @return
     */
    @Override
    protected boolean hasClone1() {
        return window().color(748, 1062).is(16547836);
    }

    /**
     * Check clone existence.
     * 
     * @return
     */
    @Override
    protected boolean hasClone2() {
        return window().color(781, 1059).is(16744447);
    }

    /**
     * Check clone existence.
     * 
     * @return
     */
    @Override
    protected boolean hasClone3() {
        return window().color(813, 1058).is(16744447);
    }

    /**
     * Entry point.
     */
    public static void main(String[] args) {
        Macro.launch().useTrayIcon().use(Chronomancer.class);
    }
}
