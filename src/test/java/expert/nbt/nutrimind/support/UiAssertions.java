package expert.nbt.nutrimind.support;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Locator.WaitForOptions;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import static org.testng.Assert.*;

public final class UiAssertions {
    private UiAssertions() { }

    public static void visible(Locator locator, String expected) {
        visible(locator, expected, null);
    }

    public static void visible(Locator locator, String expected, Double timeout) {
        try {
            Locator.WaitForOptions options = new WaitForOptions();
            if (timeout != null) options.setTimeout(timeout);
            locator.waitFor(options);
            assertTrue(locator.isVisible(), "Expected: " + expected + ". Actual: the element is not visible.");
        } catch (PlaywrightException exception) {
            fail("Expected: " + expected + ". Actual: the element did not become visible.");
        }
    }

    public static void text(Locator locator, String expectedText, String expected) {
        String actual = normalize(locator.innerText());
        assertEquals(comparable(actual), comparable(expectedText), "Expected: " + expected + ". Actual text: " + actual + ".");
    }

    public static void texts(Locator locator, String[] expectedTexts, String expected) {
        List<String> actual = locator.allInnerTexts().stream().map(UiAssertions::normalize).toList();
        List<String> expectedValues = Arrays.stream(expectedTexts).map(UiAssertions::normalize).toList();
        List<String> comparableActual = actual.stream().map(UiAssertions::comparable).toList();
        List<String> comparableExpected = expectedValues.stream().map(UiAssertions::comparable).toList();
        assertEquals(comparableActual, comparableExpected, "Expected: " + expected + ". Actual text: " + actual + ".");
    }

    public static void containsText(Locator locator, String expectedText, String expected) {
        String actual = normalize(locator.innerText());
        assertTrue(comparable(actual).contains(comparable(expectedText)), "Expected: " + expected + ". Actual text: " + actual + ".");
    }

    public static void url(Page page, Pattern expectedUrl, String expected) {
        String actual = page.url();
        assertTrue(expectedUrl.matcher(actual).matches(), "Expected: " + expected + ". Actual URL: " + actual + ".");
    }

    public static void url(Page page, String expectedUrl, String expected) {
        String actual = page.url();
        assertEquals(actual, expectedUrl, "Expected: " + expected + ". Actual URL: " + actual + ".");
    }

    public static void count(Locator locator, int expectedCount, String expected) {
        int actual = locator.count();
        assertEquals(actual, expectedCount, "Expected: " + expected + ". Actual count: " + actual + ".");
    }

    public static void attribute(Locator locator, String name, String expectedValue, String expected) {
        String actual = locator.getAttribute(name);
        assertEquals(actual, expectedValue, "Expected: " + expected + ". Actual " + name + ": " + actual + ".");
    }

    public static void attributeContains(Locator locator, String name, String expectedValue, String expected) {
        String actual = locator.getAttribute(name);
        assertTrue(actual != null && actual.contains(expectedValue),
                "Expected: " + expected + ". Actual " + name + ": " + actual + ".");
    }

    public static void value(Locator locator, String expectedValue, String expected) {
        String actual = locator.inputValue();
        assertEquals(actual, expectedValue, "Expected: " + expected + ". Actual value: " + actual + ".");
    }

    public static void notBlank(Locator locator, String expected) {
        String actual = locator.inputValue();
        assertTrue(!actual.isBlank(), "Expected: " + expected + ". Actual: the field is empty.");
    }

    public static void enabled(Locator locator, String expected) {
        assertTrue(locator.isEnabled(), "Expected: " + expected + ". Actual: the control is disabled.");
    }

    public static void disabled(Locator locator, String expected) {
        assertFalse(locator.isEnabled(), "Expected: " + expected + ". Actual: the control is enabled.");
    }

    private static String normalize(String text) {
        return text.replaceAll("\\s+", " ").trim();
    }

    private static String comparable(String text) {
        return normalize(text).toLowerCase(Locale.ROOT);
    }
}
