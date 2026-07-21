package com.automationexercise.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * TestCasesPage – Page Object for the Test Cases listing page (/test_cases).
 */
public class TestCasesPage extends AEBasePage {

    private static final By TEST_CASES_HEADING = By.xpath("//b[contains(text(),'Test Cases')]");

    public TestCasesPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Confirms the Test Cases page heading is visible.
     */
    public boolean isTestCasesPageVisible() {
        return isDisplayed(TEST_CASES_HEADING);
    }

    /** Waits until the Test Cases page heading is actually displayed. */
    public TestCasesPage waitUntilLoaded() {
        waitUntilVisible(TEST_CASES_HEADING);
        return this;
    }
}
