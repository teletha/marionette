/*
 * Copyright (C) 2019 Marionette Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package marionette.browser;

import java.util.concurrent.TimeUnit;

/**
 * @version 2018/10/01 2:22:55
 */
public class BrowserPreference {

    /** The user defined default time (millseconds) for page load timeout. */
    public long pageLoadTimeout = 45 * 1000;

    /** The user defined default time (seconds) for operation timeout. */
    public long operationTimeout = 30;

    /** The user defined default action interval time (millseconds). */
    public long operationInterval = 50;

    /** The user defined default mode for element search. */
    public boolean searchElementExactly = false;

    /** The user defined default retry limit of timeout. */
    public int retryLimit = 1;

    /**
     * Configure browser action interval time.
     * 
     * @param time A time duration.
     * @param unit A time unit.
     * @return
     */
    public final BrowserPreference operationInterval(long time, TimeUnit unit) {
        if (0 < time && unit != null) {
            operationInterval = unit.toMillis(time);
        }
        return this;
    }

    /**
     * Configure timeout when browser loads page.
     * 
     * @param time A time duration.
     * @param unit A time unit.
     * @return
     */
    public final BrowserPreference operationTimeout(long time, TimeUnit unit) {
        if (0 < time && unit != null) {
            operationTimeout = unit.toSeconds(time);
        }
        return this;
    }

    /**
     * Configure timeout when browser loads page.
     * 
     * @param time A time duration.
     * @param unit A time unit.
     * @return
     */
    public final BrowserPreference pageLoadTimeout(long time, TimeUnit unit) {
        if (0 < time && unit != null) {
            pageLoadTimeout = unit.toMillis(time);
        }
        return this;
    }
}
