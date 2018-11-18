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

/**
 * @version 2018/11/17 12:04:18a
 */
public class Guildwars extends AbstractMacro {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declare() {
        when(Key.A).press().to(e -> {
            System.out.println(e);
        });
    }

    /**
     * Entry point.
     * 
     * @param args
     */
    public static void main(String[] args) {
        Macro macro = new Macro("Guildwars");
        macro.useTrayIcon();
        macro.use(Guildwars.class);
    }
}
