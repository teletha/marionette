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

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
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

import kiss.Decoder;
import kiss.Disposable;
import kiss.Encoder;
import kiss.I;
import kiss.Signal;
import kiss.Variable;
import kiss.WiseConsumer;
import kiss.WiseFunction;
import kiss.WiseRunnable;

/**
 * @version 2018/10/01 2:53:26
 */
public class Browser<Self extends Browser<Self>> implements Disposable {

    static {
        I.load(Codec.class);
        System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
        System.setProperty("webdriver.gecko.driver", Paths.get("geckodriver.exe").toAbsolutePath().toString());
    }

    /** The reusable retry flow controller. */
    private static final Retry retry = new Retry();

    /** The user defined preference. */
    BrowserInitialPreference prefs = new BrowserInitialPreference();

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
     * Lazy initialization
     * 
     * @return
     */
    private synchronized WebDriver driver() {
        if (driver == null) {
            if (prefs.headless) {
                prefs.options.add("--headless");
            }

            if (prefs.secret) {
                prefs.options.add("--incognito");
            }

            if (prefs.profileDirectory != null) {
                prefs.options.add("user-data-dir=" + prefs.profileDirectory.toAbsolutePath().toString());
            }

            ChromeOptions options = new ChromeOptions();
            options.addArguments(prefs.options);
            driver = new ChromeDriver(options);
            // FirefoxOptions options = new FirefoxOptions();
            // options.addArguments(defaults.options);
            //
            // driver = new FirefoxDriver(options);

            driver.manage().timeouts().pageLoadTimeout(prefs.pageLoadTimeout, TimeUnit.MILLISECONDS);
            operation = new WebDriverWait(driver, prefs.operationTimeout);
            operationForHuman = new WebDriverWait(driver, Integer.MAX_VALUE, 500);
            searchElement = new WebDriverWait(driver, 60, 500);
            searchElementExactly = prefs.searchElementExactly;
            operationInterval = prefs.operationInterval;
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
        I.signal("").effect(operation).retry(Retry.class).to();
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
        operation.until(ExpectedConditions.invisibilityOfElementLocated(selector));

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
     * @param retry A list of retry.
     * @return Chainable API.
     */
    public final Self click(String selector) {
        return click(By.cssSelector(selector));
    }

    /**
     * <p>
     * Click the target element.
     * </p>
     * 
     * @param selector A element selector.
     * @param retry A list of retry.
     * @return Chainable API.
     */
    public final Self click(By elementSelector) {
        return each(elementSelector, e -> {
            e.click();
        });
    }

    /**
     * <p>
     * Click the target element.
     * </p>
     * 
     * @param selector A element selector.
     * @param retry A list of retry.
     * @return Chainable API.
     */
    public final Self click(WebElement element) {
        return each(I.list(element), e -> {
            e.click();
        });
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

        operation.until(ExpectedConditions.visibilityOfElementLocated(cssSelector));
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
     * Reset preference.
     * 
     * @return
     */
    public final Self resetPreference() {
        prefs = new BrowserInitialPreference();
        preference(e -> {
        });
        return chain(0);
    }

    /**
     * Configure preference at runtime.
     * 
     * @param preference A preference operator.
     * @return
     */
    public final Self preference(Consumer<BrowserPreference> preference) {
        if (preference != null) {
            preference.accept(prefs);

            searchElementExactly = prefs.searchElementExactly;
            operationInterval = prefs.operationInterval;
            operation.withTimeout(Duration.ofSeconds(prefs.operationTimeout));
            driver.manage().timeouts().pageLoadTimeout(prefs.pageLoadTimeout, TimeUnit.MILLISECONDS);
        }
        return chain(0);
    }

    /**
     * <p>
     * Get cookie value by name.
     * </p>
     * 
     * @param name
     * @return
     */
    public final String cookie(String name) {
        return driver().manage().getCookieNamed(name).getValue();
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
    public final Self deselect(String selector) {
        return deselect(By.cssSelector(selector));
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
    public final Self deselect(By selector) {
        return each(selector, e -> {
            if (e.isSelected() == true) {
                operation.until(ExpectedConditions.elementToBeClickable(e)).click();
            }
        });
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
    public final Self each(String selector, WiseConsumer<WebElement> processor) {
        return each(By.cssSelector(selector), processor);
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
    public final Self each(By elementSelector, WiseConsumer<WebElement> processor) {
        checkExactly(elementSelector);
        return each(driver().findElements(elementSelector), processor);
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
    public final Self each(Iterable<WebElement> elements, WiseConsumer<WebElement> processor) {
        for (WebElement element : elements) {
            try {
                processor.accept(element);
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
    public final Signal<WebElement> find(String selector) {
        return find(By.cssSelector(selector));
    }

    /**
     * <p>
     * Find elements by selector and process them.
     * </p>
     * 
     * @param elementSelector A element selector.
     * @return {@link Signal} stream.
     */
    public final Signal<WebElement> find(By elementSelector) {
        return I.signal(driver()).flatIterable(d -> d.findElements(elementSelector)).retryWhen(retryError());
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
    public final Self input(String selector, Object input) {
        return input(By.cssSelector(selector), input);
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
    public final Self input(By elementSelector, Object input) {
        return each(elementSelector, e -> {
            try {
                e.click();
            } catch (WebDriverException clickWasFailed) {
                // ignore;
            }
            e.clear();
            e.sendKeys(String.valueOf(input));
        });
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
    public final Self inputSlowly(String selector, Object input) {
        return inputSlowly(By.cssSelector(selector), input);
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
    public final Self inputSlowly(By elementSelector, Object input) {
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
                await(500, TimeUnit.MILLISECONDS);
            }
        });
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
        I.signal(driver()).effect(d -> d.navigate().refresh()).retryWhen(recoverPageLoadTimeout()).to();

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
    public final Self select(String selector) {
        return select(By.cssSelector(selector));
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
    public final Self select(By selector) {
        return each(selector, e -> {
            if (e.isSelected() == false) {
                operation.until(ExpectedConditions.elementToBeClickable(e)).click();
            }
        });
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
    public final Self select(String selector, int index) {
        return select(By.cssSelector(selector), index);
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
    public final Self select(By selector, int index) {
        return each(selector, e -> {
            Select select = new Select(e);
            select.selectByIndex(index);
        });
    }

    /**
     * <p>
     * Retrive text.
     * </p>
     * 
     * @param selector A css selector.
     * @param retry A list of retry.
     * @return A text contents.
     */
    public final Variable<String> text(String selector) {
        return text(By.cssSelector(selector));
    }

    /**
     * <p>
     * Retrieve text.
     * </p>
     * 
     * @param selector A eleemnt selector.
     * @param retry A list of retry.
     * @return A text contents.
     */
    public final Variable<String> text(By selector) {
        return find(selector).take(1).map(WebElement::getText).to();
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
     * Helper method to describe the recovery for page load timeout.
     * </p>
     * 
     * @return
     */
    public final <E extends Throwable> WiseFunction<Signal<E>, Signal<?>> recoverPageLoadTimeout() {
        return fail -> fail.takeWhile(TimeoutException.class::isInstance).take(prefs.retryLimit);
    }

    private final <E extends Throwable> WiseFunction<Signal<E>, Signal<?>> retryError() {
        return fail -> fail.takeWhile(WebDriverException.class::isInstance);
    }

    /**
     * Builde the operatable {@link Browser}.
     * 
     * @return
     */
    public static final Browser build() {
        return build(Browser.class);
    }

    /**
     * Builde the operatable {@link Browser}.
     * 
     * @return
     */
    public static final Browser build(Consumer<BrowserInitialPreference> preference) {
        return build(Browser.class, preference);
    }

    /**
     * Builde the operatable your {@link Browser}.
     * 
     * @param browser Your browser.
     * @return
     */
    public static final <B extends Browser> B build(Class<B> browser) {
        return build(browser, (Consumer<BrowserInitialPreference>) e -> {
        });
    }

    /**
     * Builde the operatable your {@link Browser}.
     * 
     * @param browser Your browser.
     * @param preference A browser setting.
     * @return
     */
    public static final <B extends Browser> B build(Class<B> browser, Consumer<BrowserInitialPreference> preference) {
        B created = I.make(browser);

        if (preference != null) {
            preference.accept(created.prefs);
        }

        return created;
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
    @SuppressWarnings("serial")
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
