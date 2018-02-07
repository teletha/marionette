/*
 * Copyright (C) 2017 Jackpot Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package marionette;

import static java.util.concurrent.TimeUnit.*;
import static org.openqa.selenium.support.ui.ExpectedConditions.*;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import filer.Filer;
import kiss.Decoder;
import kiss.Disposable;
import kiss.Encoder;
import kiss.I;
import kiss.Signal;
import kiss.Variable;
import kiss.WiseConsumer;
import kiss.WiseRunnable;
import kiss.WiseTriFunction;

/**
 * @version 2018/02/08 5:05:35
 */
public class Browser<Self extends Browser<Self>> implements Disposable {

    static {
        I.load(Codec.class, true);
        System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
        System.setProperty("webdriver.gecko.driver", Filer.locate("geckodriver.exe").toAbsolutePath().toString());
    }

    /** The reusable retry flow controller. */
    private static final Retry retry = new Retry();

    /** The user defined default preference. */
    private final Preference defaults = new Preference();

    /** The driver. */
    private WebDriver driver;

    /** The waiter for automatic operation. */
    private WebDriverWait operation;

    /** The waiter for human operation. */
    private WebDriverWait operationForHuman;

    /** The waiter for element search. */
    private WebDriverWait searchElement;

    /** The element search mode. */
    private boolean searchElementExactly;

    /** The interval time for each actions. */
    private long operationInterval;

    /**
     * <p>
     * Lazy initialization
     * </p>
     * 
     * @return
     */
    private WebDriver driver() {
        if (driver == null) {
            if (defaults.headless) {
                defaults.options.add("--headless");
            }

            ChromeOptions options = new ChromeOptions();
            options.addArguments(defaults.options);
            driver = new ChromeDriver(options);
            // FirefoxOptions options = new FirefoxOptions();
            // options.addArguments(defaults.options);
            //
            // driver = new FirefoxDriver(options);

            driver.manage().timeouts().pageLoadTimeout(defaults.pageLoadTimeout, MILLISECONDS);
            operation = new WebDriverWait(driver, defaults.operationTimeout);
            operationForHuman = new WebDriverWait(driver, Integer.MAX_VALUE, 500);
            searchElement = new WebDriverWait(driver, 60, 500);
            searchElementExactly = defaults.searchElementExactly;
            operationInterval = defaults.operationInterval;
        }
        return driver;
    }

    /**
     * <p>
     * Accept alert.
     * </p>
     * 
     * @return
     */
    public final Self acceptAlert() {
        driver.switchTo().alert().accept();

        return chain();
    }

    /**
     * <p>
     * Move mouse pointer to the specified element.
     * </p>
     * 
     * @param e A target element.
     * @return Chainable API.
     */
    public final Self action(Consumer<Actions> e) {
        Actions action = new Actions(driver);
        e.accept(action);

        return chain();
    }

    /**
     * <p>
     * Unit the described action and perform it. Calling {@link #retry()} in this method, it suspend
     * the current operation immediately and retry the original operation from start.
     * </p>
     * 
     * @param operation
     * @return
     */
    public final Self action(WiseRunnable operation) {
        I.run(operation, I.retryWhen(Retry.class));
        return chain(0);
    }

    /**
     * <p>
     * Wait client explicitly.
     * </p>
     * 
     * @param millSeconds A time to sleep.
     * @return Chainable API.
     */
    public final Self await(int time, TimeUnit unit) {
        return chain(unit.toMillis(time));
    }

    /**
     * <p>
     * Wait target element.
     * </p>
     * 
     * @param selector A css selector.
     * @return Chainable API.
     */
    public final Self awaitElement(String selector) {
        return awaitElement(By.cssSelector(selector));
    }

    /**
     * <p>
     * Wait target element.
     * </p>
     * 
     * @param selector A element selector.
     * @return Chainable API.
     */
    public final Self awaitElement(By selector) {
        operation.until(ExpectedConditions.presenceOfElementLocated(selector));

        return chain(0);
    }

    /**
     * <p>
     * Wait until the target element is disappeared.
     * </p>
     * 
     * @param selector A css selector.
     * @return Chainable API.
     */
    public final Self awaitElementDisappear(String selector) {
        return awaitElementDisappear(By.cssSelector(selector));
    }

    /**
     * <p>
     * Wait until the target element is disappeared.
     * </p>
     * 
     * @param selector A element selector.
     * @return Chainable API.
     */
    public final Self awaitElementDisappear(By selector) {
        operation.until(invisibilityOfElementLocated(selector));

        return chain(0);
    }

    /**
     * <p>
     * Wait target element.
     * </p>
     * 
     * @param selector A css selector.
     * @return Chainable API.
     */
    public final Self awaitText(String selector, String text) {
        return awaitText(By.cssSelector(selector), text);
    }

    /**
     * <p>
     * Wait target element.
     * </p>
     * 
     * @param selector A element selector.
     * @return Chainable API.
     */
    public final Self awaitText(By selector, String text) {
        if (text == null || text.isEmpty()) {
            operation.until(ExpectedConditions.textToBe(selector, ""));
        } else {
            operation.until(ExpectedConditions.textMatches(selector, Pattern.compile(Pattern.quote(text))));
        }
        return chain(0);
    }

    /**
     * <p>
     * Wait target element.
     * </p>
     * 
     * @param selector A css selector.
     * @return Chainable API.
     */
    public final Self awaitUnlessText(String selector, String text) {
        return awaitUnlessText(By.cssSelector(selector), text);
    }

    /**
     * <p>
     * Wait target element.
     * </p>
     * 
     * @param selector A element selector.
     * @return Chainable API.
     */
    public final Self awaitUnlessText(By selector, String text) {
        if (text == null || text.isEmpty()) {
            operation.until(ExpectedConditions.not(ExpectedConditions.textToBe(selector, "")));
        } else {
            operation.until(ExpectedConditions.not(ExpectedConditions.textMatches(selector, Pattern.compile(Pattern.quote(text)))));
        }
        return chain(0);
    }

    /**
     * <p>
     * Resolve captcha.
     * </p>
     * 
     * @param imageSelector
     * @param inputSelector
     * @return
     */
    public final Self captcha(String imageSelector, String inputSelector) {
        return captcha(imageSelector, inputSelector, null, null);
    }

    /**
     * <p>
     * Resolve captcha.
     * </p>
     * 
     * @param imageSelector
     * @param inputSelector
     * @param characterOperator
     * @return
     */
    public final Self captcha(String imageSelector, String inputSelector, UnaryOperator<String> characterOperator) {
        return captcha(imageSelector, inputSelector, null, characterOperator);
    }

    /**
     * <p>
     * Resolve captcha.
     * </p>
     * 
     * @param imageSelector
     * @param inputSelector
     * @param imageOperator
     * @param textOperator
     * @return
     */
    public final Self captcha(String imageSelector, String inputSelector, UnaryOperator<Uncaptcha> imageOperator, UnaryOperator<String> textOperator) {
        Objects.requireNonNull(imageSelector);
        Objects.requireNonNull(inputSelector);

        UnaryOperator<Uncaptcha> imageOp = imageOperator == null ? UnaryOperator.identity() : imageOperator;
        UnaryOperator<String> textOp = textOperator == null ? UnaryOperator.identity() : textOperator;

        return each(imageSelector, e -> {
            String text = imageOp.apply(new Uncaptcha(new URL(e.getAttribute("src")))).read();

            input(inputSelector, textOp.apply(text));
        });
    }

    /**
     * <p>
     * Click the target element if you can.
     * </p>
     * 
     * @param selector A css selector.
     * @return Chainable API.
     */
    public final boolean canClick(String selector) {
        if (exist(selector)) {
            click(selector);
            return true;
        } else {
            return false;
        }
    }

    /**
     * <p>
     * Click the target element.
     * </p>
     * 
     * @param selector A css selector.
     * @param recoveries A list of recoveries.
     * @return Chainable API.
     */
    public final Self click(String selector, WiseTriFunction<Runnable, Throwable, Integer, Runnable>... recoveries) {
        return click(By.cssSelector(selector), recoveries);
    }

    /**
     * <p>
     * Click the target element.
     * </p>
     * 
     * @param selector A element selector.
     * @param recoveries A list of recoveries.
     * @return Chainable API.
     */
    public final Self click(By elementSelector, WiseTriFunction<Runnable, Throwable, Integer, Runnable>... recoveries) {
        return each(elementSelector, e -> {
            e.click();
        }, recoveries);
    }

    /**
     * <p>
     * Click the target element.
     * </p>
     * 
     * @param selector A element selector.
     * @param recoveries A list of recoveries.
     * @return Chainable API.
     */
    public final Self click(WebElement element, WiseTriFunction<Runnable, Throwable, Integer, Runnable>... recoveries) {
        return each(I.list(element), e -> {
            e.click();
        }, recoveries);
    }

    /**
     * Close alert popup.
     */
    public Browser closeAlert() {
        driver().switchTo().alert().accept();

        return chain();
    }

    /**
     * <p>
     * Clear element by the specified selector.
     * </p>
     * 
     * @param string
     */
    public Browser clear(String selector) {
        By cssSelector = By.cssSelector(selector);

        operation.until(visibilityOfElementLocated(cssSelector));
        driver().findElement(cssSelector).clear();

        return chain();
    }

    /**
     * Clear all cookies, localStorage and sessionStorage.
     * 
     * @return
     */
    public Browser clearFingerPrint() {
        driver().manage().deleteAllCookies();

        return chain();
    }

    /**
     * <p>
     * Configure as default setting.
     * </p>
     * 
     * @return
     */
    public final Self configDefault() {
        configSearchElementExactly(defaults.searchElementExactly);
        configOperationInterval(defaults.operationInterval, MILLISECONDS);
        configOperationTimeou(defaults.operationTimeout, SECONDS);
        configPageLoadTimeout(defaults.pageLoadTimeout, MILLISECONDS);
        return chain(0);
    }

    /**
     * <p>
     * Configure headless mode.
     * </p>
     * 
     * @param on
     * @return
     */
    public final Self configHeadless(boolean on) {
        if (driver == null) {
            defaults.headless = on;
        }
        return chain(0);
    }

    /**
     * <p>
     * Configure adblock mode.
     * </p>
     * 
     * @param on
     * @return
     */
    public final Self configAdblock(boolean on) {
        if (driver == null) {
            defaults.adblock = on;
        }
        return chain(0);
    }

    /**
     * <p>
     * Configure tor mode.
     * </p>
     * 
     * @param on
     * @return
     */
    public final Self configTor(boolean on) {
        if (driver == null) {
            defaults.tor = on;
        }
        return chain(0);
    }

    /**
     * <p>
     * Configure secret mode.
     * </p>
     * 
     * @param on
     * @return
     */
    public final Self configSecret(boolean on) {
        if (driver == null) {
            defaults.options.add("--incognito");
        }
        return chain(0);
    }

    /**
     * <p>
     * Configure element selection mode.
     * </p>
     * 
     * @param on
     * @return
     */
    public final Self configSearchElementExactly(boolean on) {
        if (driver == null) {
            defaults.searchElementExactly = on;
        } else {
            searchElementExactly = on;
        }
        return chain(0);
    }

    /**
     * <p>
     * Configure browser action interval time.
     * </p>
     * 
     * @param time A time duration.
     * @param unit A time unit.
     * @return
     */
    public final Self configOperationInterval(long time, TimeUnit unit) {
        if (0 < time && unit != null) {
            if (driver == null) {
                defaults.operationInterval = unit.toMillis(time);
            } else {
                operationInterval = unit.toMillis(time);
            }
        }
        return chain(0);
    }

    /**
     * <p>
     * Configure timeout when browser loads page.
     * </p>
     * 
     * @param time A time duration.
     * @param unit A time unit.
     * @return
     */
    public final Self configOperationTimeou(long time, TimeUnit unit) {
        if (0 < time && unit != null) {
            if (driver == null) {
                defaults.operationTimeout = unit.toSeconds(time);
            } else {
                operation.withTimeout(time, unit);
            }
        }
        return chain(0);
    }

    /**
     * <p>
     * Configure profile directory.
     * </p>
     * 
     * @param directory
     * @return
     */
    public final Self configProfile(Path directory) {
        if (directory != null && Files.isDirectory(directory)) {
            defaults.options.add("user-data-dir=" + directory.toAbsolutePath().toString());
        }
        return chain(0);
    }

    /**
     * <p>
     * Configure timeout when browser loads page.
     * </p>
     * 
     * @param time A time duration.
     * @param unit A time unit.
     * @return
     */
    public final Self configPageLoadTimeout(long time, TimeUnit unit) {
        if (0 < time && unit != null) {
            if (driver == null) {
                defaults.pageLoadTimeout = unit.toMillis(time);
            } else {
                driver.manage().timeouts().pageLoadTimeout(time, unit);
            }
        }
        return chain(0);
    }

    /**
     * <p>
     * Deselect the target option.
     * </p>
     * 
     * @param selector A css selector.
     * @param input A option to deselect.
     * @return Chainable API.
     */
    public final Self deselect(String selector, WiseTriFunction<Runnable, Throwable, Integer, Runnable>... recoveries) {
        return deselect(By.cssSelector(selector), recoveries);
    }

    /**
     * <p>
     * Deselect the target option.
     * </p>
     * 
     * @param selector A element selector.
     * @param input A option to deselect.
     * @return Chainable API.
     */
    public final Self deselect(By selector, WiseTriFunction<Runnable, Throwable, Integer, Runnable>... recoveries) {
        return each(selector, e -> {
            if (e.isSelected() == true) {
                operation.until(ExpectedConditions.elementToBeClickable(e)).click();
            }
        }, recoveries);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void vandalize() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    /**
     * <p>
     * Find elements by selector and process them.
     * </p>
     * 
     * @param selector A css selector.
     * @param processor A element processor.
     * @return Chainable API.
     */
    public final Self each(String selector, WiseConsumer<WebElement> processor, WiseTriFunction<Runnable, Throwable, Integer, Runnable>... recoveries) {
        return each(By.cssSelector(selector), processor, recoveries);
    }

    /**
     * <p>
     * Find elements by selector and process them.
     * </p>
     * 
     * @param elementSelector A element selector.
     * @param processor A element processor.
     * @return Chainable API.
     */
    public final Self each(By elementSelector, WiseConsumer<WebElement> processor, WiseTriFunction<Runnable, Throwable, Integer, Runnable>... recoveries) {
        checkExactly(elementSelector);
        return each(driver().findElements(elementSelector), processor, recoveries);
    }

    /**
     * <p>
     * Find elements by selector and process them.
     * </p>
     * 
     * @param elementSelector A element selector.
     * @param processor A element processor.
     * @return Chainable API.
     */
    public final Self each(Iterable<WebElement> elements, WiseConsumer<WebElement> processor, WiseTriFunction<Runnable, Throwable, Integer, Runnable>... recoveries) {
        for (WebElement element : elements) {
            try {
                I.run(() -> processor.accept(element), recoveries);
            } catch (StaleElementReferenceException e) {
                // ignore
            }
        }
        return chain();
    }

    /**
     * <p>
     * Test element by the specified selector.
     * </p>
     * 
     * @param selector
     * @return
     */
    public boolean exist(String selector) {
        return driver().findElements(By.cssSelector(selector)).size() != 0;
    }

    /**
     * <p>
     * Find elements by selector and process them.
     * </p>
     * 
     * @param selector A css selector.
     * @return {@link Signal} stream.
     */
    public final Signal<WebElement> find(String selector, WiseTriFunction<Runnable, Throwable, Integer, Runnable>... recoveries) {
        return find(By.cssSelector(selector), recoveries);
    }

    /**
     * <p>
     * Find elements by selector and process them.
     * </p>
     * 
     * @param elementSelector A element selector.
     * @return {@link Signal} stream.
     */
    public final Signal<WebElement> find(By elementSelector, WiseTriFunction<Runnable, Throwable, Integer, Runnable>... recoveries) {
        List<WebElement> list = new ArrayList();

        I.run(() -> {
            list.addAll(driver().findElements(elementSelector));
        }, recovery(recoveries, I.retryWhen(TimeoutException.class)));

        return I.signal(list);
    }

    /**
     * <p>
     * Find elements by selector and process them.
     * </p>
     * 
     * @param elementSelector A element selector.
     * @return {@link Signal} stream.
     */
    public final List<WebElement> finds(String elementSelector, WiseTriFunction<Runnable, Throwable, Integer, Runnable>... recoveries) {
        List<WebElement> list = new ArrayList();

        I.run(() -> {
            list.addAll(driver().findElements(By.cssSelector(elementSelector)));
        }, recovery(recoveries, I.retryWhen(TimeoutException.class)));

        return list;
    }

    /**
     * <p>
     * Input text to the target field.
     * </p>
     * 
     * @param selector A css selector.
     * @param input A text to input.
     * @return Chainable API.
     */
    public final Self input(String selector, Object input, WiseTriFunction<Runnable, Throwable, Integer, Runnable>... recoveries) {
        return input(By.cssSelector(selector), input, recoveries);
    }

    /**
     * <p>
     * Input text to the target field.
     * </p>
     * 
     * @param elementSelector A element selector.
     * @param input A text to input.
     * @return Chainable API.
     */
    public final Self input(By elementSelector, Object input, WiseTriFunction<Runnable, Throwable, Integer, Runnable>... recoveries) {
        return each(elementSelector, e -> {
            try {
                e.click();
            } catch (WebDriverException clickWasFailed) {
                // ignore;
            }
            e.clear();
            e.sendKeys(String.valueOf(input));
        }, recoveries);
    }

    /**
     * <p>
     * Input text to the target field.
     * </p>
     * 
     * @param selector A css selector.
     * @param input A text to input.
     * @return Chainable API.
     */
    public final Self inputSlowly(String selector, Object input, WiseTriFunction<Runnable, Throwable, Integer, Runnable>... recoveries) {
        return inputSlowly(By.cssSelector(selector), input, recoveries);
    }

    /**
     * <p>
     * Input text to the target field.
     * </p>
     * 
     * @param elementSelector A element selector.
     * @param input A text to input.
     * @return Chainable API.
     */
    public final Self inputSlowly(By elementSelector, Object input, WiseTriFunction<Runnable, Throwable, Integer, Runnable>... recoveries) {
        return each(elementSelector, e -> {
            try {
                e.click();
            } catch (WebDriverException clickWasFailed) {
                // ignore;
            }
            e.clear();

            String v = String.valueOf(input);

            for (int i = 0; i < v.length(); i++) {
                e.sendKeys(String.valueOf(v.charAt(i)));
                await(500, MILLISECONDS);
            }
        }, recoveries);
    }

    /**
     * <p>
     * Input text to the target field by human.
     * </p>
     * 
     * @param selector A css selector.
     * @return Chainable API.
     */
    public final Self inputByHuman(String selector) {
        return inputByHuman(By.cssSelector(selector));
    }

    /**
     * <p>
     * Input text to the target field by human.
     * </p>
     * 
     * @param selector A css selector.
     * @return Chainable API.
     */
    public final Self inputByHuman(By selector) {
        // focus element
        click(selector).action(act -> act.moveToElement(driver.findElement(selector)).perform()).scrollBy(0, 100);

        operationForHuman.until(driver -> {
            WebElement input = driver.findElement(selector);
            WebElement active = driver.switchTo().activeElement();

            return input.equals(active) ? null : input;
        });
        return chain();
    }

    /**
     * <p>
     * Access to the specified URI.
     * </p>
     * 
     * @param uri A target uri to access.
     * @return Chainable APi.
     */
    public final Self load(String uri) {
        return load(Variable.of(uri));
    }

    /**
     * <p>
     * Access to the specified URI.
     * </p>
     * 
     * @param uri A target uri to access.
     * @return Chainable API.
     */
    public final Self load(Optional<String> uri) {
        return load(Variable.of(uri));
    }

    /**
     * <p>
     * Access to the specified URI.
     * </p>
     * 
     * @param uri A target uri to access.
     * @return Chainable API.
     */
    public final Self load(Variable<String> uri) {
        if (!driver().getCurrentUrl().equals(uri.v)) {
            driver().get(uri.v);
        }
        return chain();
    }

    /**
     * <p>
     * Write sup operation.
     * </p>
     * 
     * @param operation
     * @return
     */
    public final Self operate(Consumer<Self> operation) {
        if (operation != null) {
            operation.accept((Self) this);
        }
        return chain();
    }

    /**
     * <p>
     * Emulate post the specified form by the css selector.
     * </p>
     * 
     * @param cssSelector
     * @return
     */
    public final Self post(String cssSelector) {
        find(By.cssSelector(cssSelector)).take(1).to(e -> {
            script("jQuery.post('%s', jQuery('%s').serialize());", e.getAttribute("action"), cssSelector);
        });
        return chain();
    }

    /**
     * <p>
     * Execute the specified script.
     * </p>
     * 
     * @param script
     * @param values
     * @return
     */
    public final Self script(String script, Object... values) {
        ((JavascriptExecutor) driver()).executeScript(String.format(script, values));
        return chain();
    }

    /**
     * <p>
     * Reload page
     * </p>
     */
    public final Self reload() {
        I.run(driver().navigate()::refresh, recoverPageLoadTimeout());

        return chain();
    }

    /**
     * <p>
     * Retry the current operation.
     * </p>
     * 
     * @return
     * @see #action(WiseRunnable)
     */
    public final RuntimeException retry() {
        throw retry;
    }

    /**
     * Save the current cookie.
     * 
     * @param file
     * @return
     */
    public final Self storeCookie(Path file) {
        try {
            Cookies cookies = I.signal(driver().manage().getCookies()).to(Cookies.class, (m, c) -> m.put(c.getName(), c));
            I.write(cookies, Files.newBufferedWriter(file));
        } catch (IOException e) {
            throw I.quiet(e);
        }
        return (Self) this;
    }

    /**
     * Save the current cookie.
     * 
     * @param file
     * @return
     */
    public final Self restoreCookie(Path file) {
        if (Files.exists(file)) {
            try {
                Cookies cookies = I.read(Files.newBufferedReader(file), new Cookies());

                for (Cookie cookie : cookies.values()) {
                    driver().manage().addCookie(cookie);
                }
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }
        return (Self) this;
    }

    /**
     * <p>
     * Scroll window rect.
     * </p>
     * 
     * @param x
     * @param y
     * @return
     */
    public final Self scrollBy(int x, int y) {
        ((JavascriptExecutor) driver()).executeScript("window.scrollBy(" + x + "," + y + ")");

        return chain();
    }

    /**
     * <p>
     * Scroll window rect.
     * </p>
     * 
     * @param x
     * @param y
     * @return
     */
    public final Self scrollTo(int x, int y) {
        ((JavascriptExecutor) driver()).executeScript("window.scrollTo(" + x + "," + y + ")");

        return chain();
    }

    /**
     * <p>
     * Select the target option.
     * </p>
     * 
     * @param selector A css selector.
     * @param input A option to select.
     * @return Chainable API.
     */
    public final Self select(String selector, WiseTriFunction<Runnable, Throwable, Integer, Runnable>... recoveries) {
        return select(By.cssSelector(selector), recoveries);
    }

    /**
     * <p>
     * Select the target option.
     * </p>
     * 
     * @param selector A element selector.
     * @param input A option to select.
     * @return Chainable API.
     */
    public final Self select(By selector, WiseTriFunction<Runnable, Throwable, Integer, Runnable>... recoveries) {
        return each(selector, e -> {
            if (e.isSelected() == false) {
                operation.until(ExpectedConditions.elementToBeClickable(e)).click();
            }
        }, recovery(recoveries, I.retryWhen(WebDriverException.class)));
    }

    /**
     * <p>
     * Select the target option.
     * </p>
     * 
     * @param selector A css selector.
     * @param input A option to select.
     * @return Chainable API.
     */
    public final Self select(String selector, int index, WiseTriFunction<Runnable, Throwable, Integer, Runnable>... recoveries) {
        return select(By.cssSelector(selector), index, recoveries);
    }

    /**
     * <p>
     * Select the target option.
     * </p>
     * 
     * @param selector A element selector.
     * @param input A option to select.
     * @return Chainable API.
     */
    public final Self select(By selector, int index, WiseTriFunction<Runnable, Throwable, Integer, Runnable>... recoveries) {
        return each(selector, e -> {
            Select select = new Select(e);
            select.selectByIndex(index);
        }, recovery(recoveries, I.retryWhen(WebDriverException.class)));
    }

    /**
     * <p>
     * Retrive text.
     * </p>
     * 
     * @param selector A css selector.
     * @param recoveries A list of recoveries.
     * @return A text contents.
     */
    public final Variable<String> text(String selector, WiseTriFunction<Runnable, Throwable, Integer, Runnable>... recoveries) {
        return text(By.cssSelector(selector), recoveries);
    }

    /**
     * <p>
     * Retrieve text.
     * </p>
     * 
     * @param selector A eleemnt selector.
     * @param recoveries A list of recoveries.
     * @return A text contents.
     */
    public final Variable<String> text(By selector, WiseTriFunction<Runnable, Throwable, Integer, Runnable>... recoveries) {
        return find(selector, recoveries).take(1).map(WebElement::getText).to();
    }

    /**
     * <p>
     * Retrieve the current URI.
     * </p>
     * 
     * @return A current URI.
     */
    public final String uri() {
        return driver().getCurrentUrl();
    }

    /**
     * <p>
     * Check the selected elements are visible or not.
     * </p>
     * 
     * @param selector
     */
    private final void checkExactly(By selector) {
        if (searchElementExactly) {
            searchElement.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(selector));
        }
    }

    /**
     * <p>
     * Helper method to create method chain.
     * </p>
     * 
     * @return
     */
    private final Self chain() {
        return chain(operationInterval);
    }

    /**
     * <p>
     * Helper method to create method chain.
     * </p>
     * 
     * @return
     */
    private final Self chain(long millSeconds) {
        try {
            if (0 < millSeconds) Thread.sleep(millSeconds);
        } catch (InterruptedException e) {
            // ignore
        }
        return (Self) this;
    }

    /**
     * <p>
     * Add {@link RecoveryOperation} at tail.
     * </p>
     * 
     * @param base
     * @param additions
     * @return
     */
    private WiseTriFunction<Runnable, Throwable, Integer, Runnable>[] recovery(WiseTriFunction<Runnable, Throwable, Integer, Runnable>[] base, WiseTriFunction<Runnable, Throwable, Integer, Runnable>... additions) {
        WiseTriFunction<Runnable, Throwable, Integer, Runnable>[] increase = new WiseTriFunction[base.length + additions.length];

        System.arraycopy(base, 0, increase, 0, base.length);
        System.arraycopy(additions, 0, increase, base.length, additions.length);

        return increase;
    }

    /**
     * <p>
     * Helper method to describe the recovery for page load timeout.
     * </p>
     * 
     * @return
     */
    public final WiseTriFunction<Runnable, Throwable, Integer, Runnable> recoverPageLoadTimeout() {
        return I.recoverWhen(TimeoutException.class, defaults.retryLimit, original -> driver().navigate()::refresh);
    }

    public static <B extends Browser> B operate(Class<B> browser) {
        return I.make(browser);
    }

    /**
     * @version 2017/06/07 14:18:18
     */
    private class Preference {

        /** The user defined default time (millseconds) for page load timeout. */
        private long pageLoadTimeout = 45 * 1000;

        /** The user defined default time (seconds) for operation timeout. */
        private long operationTimeout = 30;

        /** The user defined default action interval time (millseconds). */
        private long operationInterval = 50;

        /** The user defined default mode for element search. */
        private boolean searchElementExactly = false;

        /** The user defined default retry limit of timeout. */
        private int retryLimit = 1;

        /** The secret mode. */
        private boolean secret = false;

        /** The headless mode. */
        public boolean headless = false;

        /** The adblock mode. */
        public boolean adblock = true;

        /** The adblock mode. */
        public boolean tor = false;

        /** The browser options. */
        private final List<String> options = I
                .list("--disable-remote-fonts", "--disable-content-prefetch", "--dns-prefetch-disable", "--log-level=3", "--silent");
    }

    /**
     * @version 2017/03/24 13:13:57
     */
    @SuppressWarnings("serial")
    private static class Retry extends RuntimeException {
    }

    /**
     * @version 2018/02/08 5:00:21
     */
    private static class Cookies extends HashMap<String, Cookie> {
    }

    /**
     * @version 2018/02/08 4:50:00
     */
    private static class Codec implements Encoder<Cookie>, Decoder<Cookie> {

        /**
         * {@inheritDoc}
         */
        @Override
        public Cookie decode(String value) {
            String[] values = value.split(";");
            return new Cookie.Builder(values[0], values[1]) //
                    .path(values[2])
                    .domain(values[3])
                    .expiresOn(values[4].equals("null") ? null : I.transform(values[4], Date.class))
                    .isSecure(I.transform(values[5], boolean.class))
                    .isHttpOnly(I.transform(values[6], boolean.class))
                    .build();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String encode(Cookie value) {
            StringJoiner joiner = new StringJoiner(";");
            joiner.add(value.getName());
            joiner.add(value.getValue());
            joiner.add(value.getPath());
            joiner.add(value.getDomain());
            joiner.add(I.transform(value.getExpiry(), String.class));
            joiner.add(I.transform(value.isSecure(), String.class));
            joiner.add(I.transform(value.isHttpOnly(), String.class));
            return joiner.toString();
        }
    }
}
