/*
 * Copyright (C) 2023 The MARIONETTE Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
public class Project extends bee.api.Project {

    String selenium = "3.141.59";

    {
        product("com.github.teletha", "marionette", "1.0.0");

        require("com.github.teletha", "sinobu");
        require("com.github.teletha", "antibug").atTest();
        require("net.java.dev.jna", "jna");
        require("net.java.dev.jna", "jna-platform");

        versionControlSystem("https://github.com/teletha/marionette");
    }
}