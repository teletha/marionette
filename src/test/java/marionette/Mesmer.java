/*
 * Copyright (C) 2023 The MARIONETTE Development Team
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
public abstract class Mesmer extends GW {

    /**
     * Check clone existence.
     * 
     * @return
     */
    protected boolean hasClone3() {
        return window().color(786, 1059).is(16744447);
    }

    /**
     * Check clone existence.
     * 
     * @return
     */
    protected boolean hasClone2() {
        return window().color(755, 1059).is(16744447);
    }

    /**
     * Check clone existence.
     * 
     * @return
     */
    protected boolean hasClone1() {
        return window().color(720, 1060).is(16744191);
    }

    /**
     * Check clone existence.
     * 
     * @return
     */
    protected final boolean hasFullClone() {
        return hasClone3();
    }

    /**
     * Check clone existence.
     * 
     * @return
     */
    protected final boolean hasNoClone() {
        return !hasClone1();
    }

    /**
     * Use shatter skill.
     */
    protected final void useShtterSkill() {
        if (canActivateProfessionSkill1()) {
            useProfessionSkill1();
        } else if (canActivateProfessionSkill2()) {
            useProfessionSkill2();
        } else if (canActivateProfessionSkill3()) {
            useProfessionSkill3();
        }
    }
}