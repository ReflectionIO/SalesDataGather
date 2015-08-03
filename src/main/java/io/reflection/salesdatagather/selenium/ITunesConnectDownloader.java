package io.reflection.salesdatagather.selenium;

import java.io.File;
import java.nio.file.Path;
import java.util.Date;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ITunesConnectDownloader {
	public static final String	SALES_FILE_NAME			= "sales.csv";
	public static final String	IAP_SALES_FILE_NAME	= "iap_sales.csv";
	public static final String	DOWNLOADS_FILE_NAME	= "downloads.csv";

	private transient static final Logger LOG = LoggerFactory.getLogger(ITunesConnectDownloader.class.getName());

	@Autowired
	private ITunesHelper helper;

	public void downloadFromITunes(Path downloadDir, String username, String password, String mainItemId, String IAPIds, String countryCodeToGatherFor, Date dateToGatherFrom, Date dateToGatherTo) {
		FirefoxDriver driver = new SeleniumDriver(downloadDir).getDriver();
		try {
			downloadFromITunes(driver, downloadDir, username, password, mainItemId, IAPIds, countryCodeToGatherFor, dateToGatherFrom, dateToGatherTo);
		} catch (Exception e) {
			LOG.error("Exception thrown while trying to download iTunes reports.", e);
		} finally {
			driver.quit();
			try {
				driver.kill();
			} catch (Exception e2) {
			}
		}
	}

	private void downloadFromITunes(FirefoxDriver driver, Path downloadDir, String username, String password, String mainItemId, String IAPIds, String countryCodeToGatherFor, Date dateToGatherFrom,
			Date dateToGatherTo) {
		LOG.debug("Logging into ITunes");
		SeleniumHelper.loginToItunes(driver, username, password);

		/*
		 * ********* DOWNLOADS FOR THE MAIN ITEM ONLY ********************
		 */
		try {

			String url = helper.getDownloadUrl(dateToGatherFrom, dateToGatherTo, mainItemId, countryCodeToGatherFor);
			if (url == null) return;

			File downloadFile = getDownloadsReport(driver, downloadDir, url);

			if (downloadFile == null) {
				SeleniumHelper.loadUrl(driver, url); // go back to the url if the download fails as we will be in an unknown state
			}

			File salesFile = getMainItemSalesReport(driver, downloadDir, downloadFile);

			url = helper.getDownloadUrl(dateToGatherFrom, dateToGatherTo, IAPIds, countryCodeToGatherFor);
			if (url == null) return;
			File iapSaleFile = getIapSalesReport(driver, downloadDir, url, downloadFile, salesFile);
		} catch (Exception e) {
			LOG.error("An unknown error occured while trying to download the reports");
		}
	}

	/**
	 * @param driver
	 * @param downloadDir
	 * @param url
	 * @param downloadFile
	 * @param salesFile
	 * @return
	 */
	private File getIapSalesReport(FirefoxDriver driver, Path downloadDir, String url, File downloadFile, File salesFile) {
		/*
		 * ********* SALES FOR THE IAPS ONLY ********************
		 */
		SeleniumHelper.loadUrl(driver, url);

		// ---- WAIT FOR THE TIMEZONE TO BE CLICKABLE AND THEN CHANGE IT TO LOCAL
		if (LOG.isDebugEnabled()) {
			LOG.debug("Clicking timezone icon when ready");
		}
		SeleniumHelper.clickElementByClassNameWhenReady(driver, ITunesHelper.TIMEZONE_TOGGLE_LINK);

		if (LOG.isDebugEnabled()) {
			LOG.debug("Timezone icon clicked. Waiting for local to be clickable and clicking");
		}

		SeleniumHelper.clickElementByClassNameWhenReady(driver, ITunesHelper.LOCAL_TIMEZONE_LINK);

		if (LOG.isDebugEnabled()) {
			LOG.debug("Waiting to click on sales");
		}

		try {
			SeleniumHelper.clickElementByClassNameWhenReady(driver, ITunesHelper.SALES_LINK);
		} catch (Exception e) {
			LOG.error("An error occured while trying to download the iaps sales file. Aborting during wait for sales link");
			return null;
		}

		// ---- WAIT FOR DOWNLOAD TO BE CLICKABLE AND THEN DOWNLOAD
		if (LOG.isDebugEnabled()) {
			LOG.debug("Waiting to click the share icon");
		}

		try {
			SeleniumHelper.clickElementByClassNameWhenReady(driver, ITunesHelper.SHARE_LINK);
		} catch (Exception e) {
			LOG.error("An error occured while trying to download the iaps sales file. Aborting during wait for share link");
			return null;
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("Downloading the sales report");
		}
		try {
			SeleniumHelper.clickElementByClassNameWhenReady(driver, ITunesHelper.DOWNLOAD_LINK);
		} catch (Exception e) {
			LOG.error("An error occured while trying to download the iaps sales file. Aborting during wait for download link");
			return null;
		}

		try {
			// A second's pause for safety.
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		File iapSaleFile = renameDownloadedFile(downloadDir, IAP_SALES_FILE_NAME, downloadFile == null ? null : downloadFile.toPath(), salesFile == null ? null : salesFile.toPath());
		return iapSaleFile;
	}

	/**
	 * @param driver
	 * @param downloadDir
	 * @param downloadFile
	 * @return
	 */
	private File getMainItemSalesReport(FirefoxDriver driver, Path downloadDir, File downloadFile) {
		/*
		 * ********* SALES FOR THE MAIN ITEM ONLY ********************
		 */
		try {
			SeleniumHelper.clickElementByClassNameWhenReady(driver, ITunesHelper.SALES_LINK);
		} catch (Exception e) {
			LOG.error("An error occured while trying to download the sales file. Aborting during wait for sales link");
			return null;
		}

		// ---- WAIT FOR DOWNLOAD TO BE CLICKABLE AND THEN DOWNLOAD
		if (LOG.isDebugEnabled()) {
			LOG.debug("Waiting to click the share icon");
		}

		try {
			SeleniumHelper.clickElementByClassNameWhenReady(driver, ITunesHelper.SHARE_LINK);
		} catch (Exception e) {
			LOG.error("An error occured while trying to download the sales file. Aborting during wait for share link");
			return null;
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("Downloading the sales report");
		}
		try {
			SeleniumHelper.clickElementByClassNameWhenReady(driver, ITunesHelper.DOWNLOAD_LINK);
		} catch (Exception e) {
			LOG.error("An error occured while trying to download the sales file. Aborting during wait for download link");
			return null;
		}

		try {
			// A second's pause for safety.
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		File salesFile = renameDownloadedFile(downloadDir, SALES_FILE_NAME, downloadFile == null ? null : downloadFile.toPath());
		return salesFile;
	}

	/**
	 * @param driver
	 * @param downloadDir
	 * @param url
	 * @return
	 */
	private File getDownloadsReport(FirefoxDriver driver, Path downloadDir, String url) {
		SeleniumHelper.loadUrl(driver, url);

		// ---- WAIT FOR THE TIMEZONE TO BE CLICKABLE AND THEN CHANGE IT TO LOCAL
		if (LOG.isDebugEnabled()) {
			LOG.debug("Clicking timezone icon when ready");
		}
		SeleniumHelper.clickElementByClassNameWhenReady(driver, ITunesHelper.TIMEZONE_TOGGLE_LINK);

		if (LOG.isDebugEnabled()) {
			LOG.debug("Timezone icon clicked. Waiting for local to be clickable and clicking");
		}

		SeleniumHelper.clickElementByClassNameWhenReady(driver, ITunesHelper.LOCAL_TIMEZONE_LINK);

		// ---- WAIT FOR DOWNLOAD TO BE CLICKABLE AND THEN DOWNLOAD
		if (LOG.isDebugEnabled()) {
			LOG.debug("Waiting to click the share icon");
		}

		try {
			SeleniumHelper.clickElementByClassNameWhenReady(driver, ITunesHelper.SHARE_LINK);
		} catch (Exception e) {
			LOG.error("An error occured while trying to download the downloads file. Aborting during wait for share link");
			return null;
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("Share icon clicked. Waiting for the download-csv to be clickable");
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("Downloading the downloads report");
		}
		try {
			SeleniumHelper.clickElementByClassNameWhenReady(driver, ITunesHelper.DOWNLOAD_LINK);
		} catch (Exception e) {
			LOG.error("An error occured while trying to download the downloads file. Aborting during wait for download link");
			return null;
		}

		try {
			// A second's pause for safety.
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		File downloadFile = renameDownloadedFile(downloadDir, DOWNLOADS_FILE_NAME);
		return downloadFile;
	}

	/**
	 * This method will rename the first file that it finds in the directory given that is not in the paths to ignore list.
	 *
	 * @param downloadDir
	 * @param newFileName
	 * @param pathsToIgnore
	 * @return java.io.File
	 */
	private File renameDownloadedFile(Path downloadDir, String newFileName, Path... pathsToIgnore) {
		for (File file : downloadDir.toFile().listFiles()) {
			if (!file.exists() || !file.isFile()) {
				continue;
			}

			File newFile = new File(file.getParentFile(), newFileName);
			// if there are no files to ignore, just rename the first file and return
			if (pathsToIgnore == null) {
				LOG.debug(String.format("Renaming file %s to %s", file.getName(), newFileName));
				file.renameTo(newFile);
				return newFile;
			}

			// is the current file in the ignore list
			boolean found = false;
			for (Path toIgnore : pathsToIgnore) {
				if (toIgnore == null) {
					continue;
				}

				if (file.getName().equalsIgnoreCase(toIgnore.getFileName().toString())) {
					found = true;
					break;
				}
			}

			// the current file is not in the ignore list and therefore is the first file to be renamed
			if (!found) {
				LOG.debug(String.format("Renaming file %s to %s", file.getName(), newFileName));
				file.renameTo(newFile);
				return newFile;
			}
		}

		return null;
	}
}
