/*
 * Copyright (C) 2019 Marionette Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package marionette.macro;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import kiss.I;
import kiss.Signaling;

class MacroDefinition {

    /** The event listeners. */
    static final List<MacroDefinition> presses = new ArrayList();

    /** The event listeners. */
    static final List<MacroDefinition> releases = new ArrayList();

    /** The event listeners. */
    static final List<MacroDefinition> mouseMove = new ArrayList();

    /** The event listeners. */
    static final List<MacroDefinition> mouseWheel = new ArrayList();

    /** The window condition. */
    final Predicate<Window> windowConditon;

    /** The acceptable event type. */
    final Predicate condition;

    /** The event should be consumed or not. */
    final boolean consumable;

    final Signaling<KeyEvent> events = new Signaling();

    /**
     * Macro definition for key.
     * 
     * @param key
     * @param press
     * @param windowConditon
     * @param options
     */
    MacroDefinition(Key key, boolean press, Predicate<Window> windowConditon, Set<MacroOption> options) {
        this(key.matcher(), windowConditon, options);

        if (press) {
            presses.add(this);
        } else {
            releases.add(this);
        }
    }

    /**
     * Macro definition for mouse.
     * 
     * @param mouse
     * @param windowConditon
     * @param options
     */
    MacroDefinition(Mouse mouse, Predicate<Window> windowConditon, Set<MacroOption> options) {
        this(I.accept(), windowConditon, options);

        switch (mouse) {
        case Move:
            mouseMove.add(this);
            break;

        case Wheel:
            mouseWheel.add(this);
            break;

        default:
            break;
        }
    }

    /**
     * Macro definition.
     * 
     * @param condition
     * @param windowCondition
     * @param options
     */
    private MacroDefinition(Predicate condition, Predicate<Window> windowCondition, Set<MacroOption> options) {
        this.condition = condition;
        this.windowConditon = windowCondition;
        this.consumable = options.contains(MacroOption.IgnoreEvent);
    }
}
