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

import org.junit.jupiter.api.Test;

/**
 * @version 2018/11/17 9:16:01
 */
class MacroTest {

    @Test
    void testName() {
        Macro.use(M.class);
    }

    private static class M extends Macro {

    }
}