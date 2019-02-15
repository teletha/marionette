/*
 * Copyright (C) 2019 Marionette Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
public class Project extends bee.api.Project {

    String selenium = "3.14.0";

    {
        product("com.github.teletha", "marionette", "0.2");

        require("com.github.teletha", "sinobu", "LATEST");
        require("com.github.teletha", "antibug", "LATEST").atTest();
        require("net.java.dev.jna", "jna", "5.2.0");
        require("net.java.dev.jna", "jna-platform", "5.2.0");

        require("org.seleniumhq.selenium", "selenium-firefox-driver", selenium);
        require("org.seleniumhq.selenium", "selenium-chrome-driver", selenium);
        require("org.seleniumhq.selenium", "selenium-support", selenium);

        versionControlSystem("https://github.com/teletha/marionette");
    }
}
