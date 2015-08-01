package io.reflection.salesdatagather.selenium;

import java.nio.file.Path;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

public class SeleniumDriver {

	private final FirefoxProfile firefoxProfile;

	public SeleniumDriver(Path downloadDir) {
		firefoxProfile = new FirefoxProfile();

		// firefoxProfile.setPreference("webdriver.load.strategy", "unstable");
		firefoxProfile.setPreference("browser.download.folderList", 2);
		firefoxProfile.setPreference("browser.download.manager.showWhenStarting", false);
		firefoxProfile.setPreference("browser.download.dir", downloadDir.toString());
		firefoxProfile.setPreference("browser.helperApps.neverAsk.saveToDisk", "text/csv");
	}

	public FirefoxDriver getDriver() {
		return new FirefoxDriver(firefoxProfile);
	}
}
