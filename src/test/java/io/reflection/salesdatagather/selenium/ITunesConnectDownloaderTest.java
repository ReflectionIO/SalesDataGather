package io.reflection.salesdatagather.selenium;

import static org.junit.Assert.*;
import io.reflection.salesdatagather.SalesDataGatherApplication;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SalesDataGatherApplication.class)
@WebAppConfiguration
public class ITunesConnectDownloaderTest {
	private static final String	USERNAME	= "SPACEHOPPERSTUDIOS";
	private static final String	PASSWORD	= "sstudios191920";
	private static final String	URL	= "https://reportingitc2.apple.com/?startDate=2014-12-01&endDate=2014-12-31&group=platform&filter_content=429903190&filter_platform=iPad,iPhone,iPod,Windows%252C%2520Macintosh%252C%2520UNKNOWN";

	private transient static final Logger LOG = LoggerFactory.getLogger(ITunesConnectDownloaderTest.class.getName());

	private FirefoxDriver driver;

	@Autowired
	private ITunesConnectDownloader downloader;

	@Autowired
	private ITunesHelper itunesHelper;

	private Path	downloadDir;

	@Before
	public void setUp() throws Exception {
		Date date = new Date();
		downloadDir = itunesHelper.createTempDownloadsDir(1,"JUNIT-TEST", "gb", date, date);
		driver = new SeleniumDriver(downloadDir).getDriver();
	}

	@After
	public void tearDown() throws Exception {
		LOG.debug("Starting tear down for test. Deleting contents and temp dir: "+downloadDir);

		Files.list(downloadDir).forEach(path ->deletePath(path));
		deletePath(downloadDir);
	}

	private void deletePath(Path path) {
		LOG.debug("Attempting to delete file: "+path);
		try {
			Files.deleteIfExists(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	@Ignore // to re-enable this test we need to put real values into the downloader's downloadFromItunes method
	public void testItunesConnectDownload() {
		LOG.debug("Testing download of Sales file");
		//		downloader.downloadFromITunes(driver, downloadDir, USERNAME, PASSWORD, "");

		File[] downloadedFiles = downloadDir.toFile().listFiles();
		assertNotNull("Did not get a list of files from the download dir at"+downloadDir, downloadedFiles);
		assertTrue("There are no files in the download directory at: "+downloadDir, downloadedFiles.length>0);
		assertTrue("There are "+downloadedFiles.length+" files in the download dir at "+downloadDir+". Only 2 were expected", downloadedFiles.length==2);

		Path downloadsFile = downloadDir.resolve(ITunesConnectDownloader.DOWNLOADS_FILE_NAME);
		Path salesFile = downloadDir.resolve(ITunesConnectDownloader.SALES_FILE_NAME);

		assertNotNull("Downloads file could not be found", downloadsFile);
		assertNotNull("Sales file could not be found", salesFile);

		assertTrue("Downloads file does not exist", downloadsFile.toFile().exists());
		assertTrue("Sales file does not exist", salesFile.toFile().exists());

		driver.quit();
	}
}
