package io.reflection.salesdatagather.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;

public class SeleniumHelper {
	private transient static final Logger LOG = LoggerFactory.getLogger(SeleniumHelper.class.getName());

	private static final int DEFAULT_TIMEOUT_IN_SECONDS = 60;

	/**
	 * @param driver
	 */
	public static void clickElementByClassNameWhenReady(FirefoxDriver driver, String className) {
		new WebDriverWait(driver, DEFAULT_TIMEOUT_IN_SECONDS).until(ExpectedConditions.elementToBeClickable(By.className(className)));

		driver.findElement(By.className(className)).click();
	}

	/**
	 * @param driver
	 * @param url
	 */
	public static void loadUrl(FirefoxDriver driver, String url) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Opening url: " + url);
		}
		driver.get(url);

		new WebDriverWait(driver, DEFAULT_TIMEOUT_IN_SECONDS).until(new Predicate<WebDriver>() {

			@Override
			public boolean apply(WebDriver input) {
				return ((JavascriptExecutor) input).executeScript("return document.readyState").equals("complete");
			}
		});

		if (LOG.isDebugEnabled()) {
			LOG.debug("Page readyState = complete");
		}
	}

	/**
	 * @param driver
	 * @param username
	 * @param password
	 */
	public static void loginToItunes(FirefoxDriver driver, String username, String password) {
		driver.get("https://itunesconnect.apple.com/WebObjects/iTunesConnect.woa");
		final WebElement accountName = driver.findElement(By.id("accountname"));
		accountName.sendKeys(username);

		final WebElement passwordElement = driver.findElement(By.id("accountpassword"));
		passwordElement.sendKeys(password);

		passwordElement.submit();

		new WebDriverWait(driver, DEFAULT_TIMEOUT_IN_SECONDS).until(new Predicate<WebDriver>() {

			@Override
			public boolean apply(WebDriver input) {
				return ((JavascriptExecutor) input).executeScript("return document.readyState").equals("complete");
			}
		});
	}

}
