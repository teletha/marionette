/*
 * Copyright (C) 2025 The MARIONETTE Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package marionette;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

import kiss.I;
import kiss.Signaling;
import kiss.Variable;

class MacroDefinition {

    /** The event listeners. */
    static final List<MacroDefinition> presses = new CopyOnWriteArrayList();

    /** The event listeners. */
    static final List<MacroDefinition> releases = new CopyOnWriteArrayList();

    /** The event listeners. */
    static final List<MacroDefinition> mouseMove = new CopyOnWriteArrayList();

    /** The event listeners. */
    static final List<MacroDefinition> mouseWheel = new CopyOnWriteArrayList();

    /** The window condition. */
    final Predicate<Window> windowConditon;

    /** The acceptable event type. */
    final Predicate condition;

    /** The event should be consumed or not. */
    final boolean consumable;

    /** The acrivation state. */
    final Variable<Boolean> enable;

    final Signaling<KeyEvent> events = new Signaling();

    /**
     * Macro definition for key.
     * 
     * @param key
     * @param press
     * @param windowConditon
     * @param options
     */
    MacroDefinition(Key key, boolean press, Predicate<Window> windowConditon, Variable<Boolean> enable, Set<MacroOption> options) {
        this(key.matcher(), windowConditon, enable, options);

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
    MacroDefinition(Mouse mouse, Predicate<Window> windowConditon, Variable<Boolean> enable, Set<MacroOption> options) {
        this(I::accept, windowConditon, enable, options);

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
    private MacroDefinition(Predicate condition, Predicate<Window> windowCondition, Variable<Boolean> enable, Set<MacroOption> options) {
        this.condition = condition;
        this.windowConditon = windowCondition;
        this.enable = enable;
        this.consumable = options.contains(MacroOption.IgnoreEvent);
    }
}