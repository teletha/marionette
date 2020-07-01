/*
 * Copyright (C) 2020 marionette Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package marionette;

import kiss.I;
import kiss.Signal;
import marionette.macro.AbstractMacro;
import marionette.macro.Key;
import marionette.macro.KeyEvent;
import marionette.macro.Mouse;

public abstract class GW extends AbstractMacro<GW> {

    /** The current movemnt state. */
    private State state = State.None;

    /**
     * {@inheritDoc}
     */
    @Override
    public void declare() {
        require(window -> window.title().equals("Guild Wars 2"), () -> {
            professionSpesific();

            // dodge
            whenPress(Key.MouseMiddle).to(() -> {
                if (Key.Shift.isPressed()) {
                    input(Key.J);
                } else if (Key.Control.isPressed()) {
                    input(Key.K);
                } else {
                    input(Key.H);
                }

                performDodge();

                // I.schedule(1500, MILLISECONDS, true, () -> {
                // if (canActivateProfessionSkill1()) {
                // input(Key.Z);
                // } else if (canActivateProfessionSkill2()) {
                // input(Key.X);
                // } else if (canActivateProfessionSkill3()) {
                // input(Key.C);
                // }
                // });

                if (state != State.None) press(state.key);
            });

            // movement
            whenFastGesture(Key.MouseRight).to(e -> {
                state = e;

                targetNearEnemy();
                root();

                // reset move to all directions
                input(Key.Up);
                release(Key.Down);
                release(Key.Right);
                release(Key.Left);

                // move to the current direction
                if (e != State.None) {
                    if (e == State.Up) {
                        input(Key.Delete);
                    } else {
                        press(e.key);
                    }
                }
            });

            // window management
            whenPress(Key.Escape).to(() -> {
                closeAllWindow();
            });
        });
    }

    /**
     * Whenever u use normal skill.
     * 
     * @return
     */
    protected final Signal<KeyEvent> whenUseSkill1() {
        return whenPress(Key.Q);
    }

    /**
     * Whenever u use normal skill.
     * 
     * @return
     */
    protected final Signal<KeyEvent> whenUseSkill2() {
        return whenPress(Key.W);
    }

    /**
     * Whenever u use normal skill.
     * 
     * @return
     */
    protected final Signal<KeyEvent> whenUseSkill3() {
        return whenPress(Key.E);
    }

    /**
     * Whenever u use normal skill.
     * 
     * @return
     */
    protected final Signal<KeyEvent> whenUseSkill4() {
        return whenPress(Key.R);
    }

    /**
     * Whenever u use normal skill.
     * 
     * @return
     */
    protected final Signal<KeyEvent> whenUseSkill5() {
        return whenPress(Key.R);
    }

    /**
     * Use skill.
     */
    protected final void useWeaponSkill1() {
        press(Key.Alt);
        input(Key.NumPad1);
        release(Key.Alt);
    }

    /**
     * Use skill.
     */
    protected final void useWeaponSkill2() {
        press(Key.Alt);
        input(Key.NumPad2);
        release(Key.Alt);
    }

    /**
     * Use skill.
     */
    protected final void useWeaponSkill3() {
        press(Key.Alt);
        input(Key.NumPad3);
        release(Key.Alt);
    }

    /**
     * Use skill.
     */
    protected final void useWeaponSkill4() {
        press(Key.Alt);
        input(Key.NumPad4);
        release(Key.Alt);
    }

    /**
     * Use skill.
     */
    protected final void useWeaponSkill5() {
        press(Key.Alt);
        input(Key.NumPad5);
        release(Key.Alt);
    }

    /**
     * Use skill.
     */
    protected final void useHealingSkill() {
        press(Key.Alt);
        input(Key.NumPad6);
        release(Key.Alt);
    }

    /**
     * Use skill.
     */
    protected final void useUtilitySkill1() {
        press(Key.Alt);
        input(Key.NumPad7);
        release(Key.Alt);
    }

    /**
     * Use skill.
     */
    protected final void useUtilitySkill2() {
        press(Key.Alt);
        input(Key.NumPad8);
        release(Key.Alt);
    }

    /**
     * Use skill.
     */
    protected final void useUtilitySkill3() {
        press(Key.Alt);
        input(Key.NumPad9);
        release(Key.Alt);
    }

    /**
     * Use skill.
     */
    protected final void useEliteSkill() {
        press(Key.Alt);
        input(Key.NumPad0);
        release(Key.Alt);
    }

    /**
     * Use skill.
     */
    protected final void useProfessionSkill1() {
        press(Key.Alt);
        input(Key.Insert);
        release(Key.Alt);
    }

    /**
     * Use skill.
     */
    protected final void useProfessionSkill2() {
        press(Key.Alt);
        input(Key.Home);
        release(Key.Alt);
    }

    /**
     * Use skill.
     */
    protected final void useProfessionSkill3() {
        press(Key.Alt);
        input(Key.PageUp);
        release(Key.Alt);
    }

    /**
     * Use skill.
     */
    protected final void useProfessionSkill4() {
        press(Key.Alt);
        input(Key.PageDown);
        release(Key.Alt);
    }

    /**
     * Use skill.
     */
    protected final void useProfessionSkill5() {
        press(Key.Alt);
        input(Key.End);
        release(Key.Alt);
    }

    /**
     * Perform Attack.
     */
    protected void performAttack() {
        useWeaponSkill1();
    }

    /**
     * Perform dodge.
     */
    protected void performDodge() {
        input(state.key);
        input(state.key);
    }

    /**
     * Wait by time.
     * 
     * @param ms
     */
    protected final void await() {
        await(550);
    }

    /**
     * Wait by time.
     * 
     * @param ms
     */
    protected final void await(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw I.quiet(e);
        }
    }

    protected abstract void professionSpesific();

    protected boolean canActivateProfessionSkill1() {
        return !window().color(684, 1085).is(0);
    }

    protected boolean canActivateProfessionSkill2() {
        return !window().color(729, 1085).is(0);
    }

    protected boolean canActivateProfessionSkill3() {
        return !window().color(775, 1085).is(0);
    }

    protected boolean canActivateProfessionSkill4() {
        return !window().color(819, 1085).is(0);
    }

    protected boolean canActivateWeaponSkill2() {
        return !window().color(716, 1131).is(0);
    }

    protected boolean canActivateWeaponSkill3() {
        return !window().color(769, 1131).is(0);
    }

    protected boolean canActivateWeaponSkill4() {
        return !window().color(825, 1131).is(0);
    }

    protected boolean canActivateWeaponSkill5() {
        return !window().color(881, 1131).is(0);
    }

    protected boolean canActivateUtilitySkill1() {
        return !window().color(1089, 1131).is(0);
    }

    protected boolean canActivateUtilitySkill2() {
        return !window().color(1141, 1131).is(0);
    }

    protected boolean canActivateUtilitySkill3() {
        return !window().color(1197, 1131).is(0);
    }

    protected boolean hasFullEnergy() {
        return window().color(1007, 1109).is(569343);
    }

    protected boolean canAvoid() {
        return window().color(957, 1084).is(527);
    }

    protected boolean isMountMode() {
        return window().color(625, 1147).is(5592661);
    }

    /**
     * Close all window.
     */
    protected final void closeAllWindow() {
        closeInventory();
        closeHero();
        closeBlackLionTrading();
        closeGuild();
        closeMail();
        closeContact();
        closeTalk();
    }

    /**
     * Check window state.
     * 
     * @return
     */
    protected final boolean isInventoryOpen() {
        return window().color(21, 49).is(12106429) && window().color(37, 72).is(12109006);
    }

    /**
     * Close window.
     */
    protected final void closeInventory() {
        if (isInventoryOpen()) {
            input(Key.F1);
        }
    }

    /**
     * Open window.
     */
    protected final void openInventory() {
        if (!isInventoryOpen()) {
            input(Key.F1);
        }
    }

    /**
     * Check window state.
     * 
     * @return
     */
    protected final boolean isHeroOpen() {
        return window().color(692, 87).is(15198183) && window().color(727, 85).is(10200492);
    }

    /**
     * Close window.
     */
    protected final void closeHero() {
        if (isHeroOpen()) {
            input(Key.F2);
        }
    }

    /**
     * Open window.
     */
    protected final void openHero() {
        if (!isHeroOpen()) {
            input(Key.F2);
        }
    }

    /**
     * Check window state.
     * 
     * @return
     */
    protected final boolean isBlackLionTradingOpen() {
        return window().color(642, 114).is(10857141) && window().color(667, 109).is(11187131);
    }

    /**
     * Close window.
     */
    protected final void closeBlackLionTrading() {
        if (isBlackLionTradingOpen()) {
            input(Key.F3);
        }
    }

    /**
     * Open window.
     */
    protected final void openBlackLionTrading() {
        if (!isBlackLionTradingOpen()) {
            input(Key.F3);
        }
    }

    /**
     * Check window state.
     * 
     * @return
     */
    protected final boolean isGuildOpen() {
        return window().color(611, 149).is(11053741) && window().color(628, 149).is(15331057);
    }

    /**
     * Close window.
     */
    protected final void closeGuild() {
        if (isGuildOpen()) {
            input(Key.F5);
        }
    }

    /**
     * Open window.
     */
    protected final void openGuild() {
        if (!isGuildOpen()) {
            input(Key.F5);
        }
    }

    /**
     * Check window state.
     * 
     * @return
     */
    protected final boolean isMailOpen() {
        return window().color(567, 188).is(13555670) && window().color(580, 193).is(527368);
    }

    /**
     * Close window.
     */
    protected final void closeMail() {
        if (isMailOpen()) {
            input(Key.F6);
        }
    }

    /**
     * Open window.
     */
    protected final void openMail() {
        if (!isMailOpen()) {
            input(Key.F6);
        }
    }

    /**
     * Check window state.
     * 
     * @return
     */
    protected final boolean isContactOpen() {
        return window().color(557, 244).is(13753569) && window().color(554, 221).is(14410727);
    }

    /**
     * Close window.
     */
    protected final void closeContact() {
        if (isContactOpen()) {
            input(Key.F7);
        }
    }

    /**
     * Open window.
     */
    protected final void openContact() {
        if (!isContactOpen()) {
            input(Key.F7);
        }
    }

    /**
     * Check window state.
     * 
     * @return
     */
    protected final boolean isTalkOpen() {
        System.out.println(window().color(557, 90) + "  " + window().color(579, 67));
        return window().color(557, 90).is(1587563) && window().color(579, 67).is(923944);
    }

    /**
     * Close window.
     */
    protected final void closeTalk() {
        if (isTalkOpen()) {
            input(Key.Escape);
        }
    }

    /**
     * Target helper.
     */
    protected final void targetNearEnemy() {
        press(Key.Alt);
        input(Key.F12);
        release(Key.Alt);
    }

    /**
     * Target helper.
     */
    protected final void root() {
        press(Key.Alt);
        input(Key.F11);
        release(Key.Alt);
    }

    /**
     * <p>
     * Declare key related event.
     * </p>
     * 
     * @param key
     * @return
     */
    private final Signal<State> whenFastGesture(Key key) {
        int[] last = new int[2];
        State[] lastDirection = new State[1];

        return when(Mouse.Move).skipUntil(whenPress(key)).takeUntil(whenRelease(key)).effectOnce(e -> {
            // save start position
            last[0] = e.x();
            last[1] = e.y();
            lastDirection[0] = null;
        }).map(e -> {
            int x = e.x();
            int y = e.y();
            int distanceX = Math.abs(last[0] - x);
            int distanceY = Math.abs(last[1] - y);

            // minimal movement where the gesture is recognized
            int min = 13;
            if (distanceX < min && distanceY < min) {
                return lastDirection[0];
            }

            // determine current direction
            State direction;
            if (distanceX > distanceY) {
                direction = x < last[0] ? State.Left : State.Right;
            } else {
                direction = y < last[1] ? State.Up : State.Down;
            }

            // save current position
            last[0] = x;
            last[1] = y;
            lastDirection[0] = direction;

            return direction;
        }).diff().or(State.None).repeat();
    }

    /**
     * 
     */
    private enum State {
        Up(Key.Up), Down(Key.Down), Left(Key.Left), Right(Key.Right), None(Key.Down);

        private Key key;

        /**
         * @param key
         */
        private State(Key key) {
            this.key = key;
        }
    }
}