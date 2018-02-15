/*
 * Copyright (C) 2017 Marionette Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
public class Project extends bee.api.Project {

    String selenium = "3.9.1";

    {
        product("com.github.teletha", "Marionette", "0.1");

        require("com.github.teletha", "sinobu", "1.0");
        require("com.github.teletha", "filer", "0.5");
        require("net.java.dev.jna", "jna", "4.2.2");
        require("net.java.dev.jna", "jna-platform", "4.2.2");

        require("org.seleniumhq.selenium", "selenium-firefox-driver", selenium);
        require("org.seleniumhq.selenium", "selenium-chrome-driver", selenium);
        require("org.seleniumhq.selenium", "selenium-support", selenium);
    }
}
