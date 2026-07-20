package com.automationexercise.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TestCasesPage – Page Object for the Test Cases listing page (/test_cases).
 *
 * Previously HeaderComponent.clickTestCases() incorrectly returned HomePage.
 * This class correctly represents the destination page.
 */
public class TestCasesPage extends AEBasePage {

    private static final Logger log = LoggerFactory.getLogger(TestCasesPage.class);

    private static final By TEST_CASES_HEADING = By.xpath("//b[contains(text(),'Test Cases')]");

    public TestCasesPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Confirms the Test Cases page heading is visible.
     */
    public boolean isTestCasesPageVisible() {
        return isDisplayed(TEST_CASES_HEADING, 10);
    }
}
