package io.reflection.salesdatagather.selenium;

import java.nio.file.Path;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;


public class SeleniumDriver {

	private final FirefoxDriver driver;



	public SeleniumDriver(Path downloadDir) {
		final FirefoxProfile firefoxProfile = new FirefoxProfile();

		firefoxProfile.setPreference("webdriver.load.strategy", "unstable");
		firefoxProfile.setPreference("browser.download.folderList", 2);
		firefoxProfile.setPreference("browser.download.manager.showWhenStarting", false);
		firefoxProfile.setPreference("browser.download.dir", downloadDir.toString());
		firefoxProfile.setPreference("browser.helperApps.neverAsk.saveToDisk", "text/csv");

		driver = new FirefoxDriver(firefoxProfile);
	}

	public FirefoxDriver getDriver() {
		return driver;
	}
}
