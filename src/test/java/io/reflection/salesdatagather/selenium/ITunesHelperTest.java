package io.reflection.salesdatagather.selenium;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.reflection.salesdatagather.BaseSpringTestClass;

public class ITunesHelperTest extends BaseSpringTestClass {
	private transient static final Logger LOG = LoggerFactory.getLogger(ITunesHelperTest.class.getName());

	@Autowired
	private ITunesHelper itunesHelper;

	@Test
	public void getDownloadUrl() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String gatherFrom = "2014-12-01";
		String gatherTo = "2014-12-31";
		String itemIds = "429903190";

		String generatedUrl = null;
		try {
			generatedUrl = itunesHelper.getDownloadUrl(sdf.parse(gatherFrom), sdf.parse(gatherTo), itemIds, "gb");
		} catch (ParseException e) {
			LOG.error("This unit test needs to be checked. The dates used as params are not parsing into dates.", e);
		}

		String expectedOutput = "https://reportingitc2.apple.com/?startDate=2014-12-01&endDate=2014-12-31&group=platform&filter_content=429903190&filter_piano_location=143444";

		assertNotNull("The generated ITunes Connect URL was null", generatedUrl);
		assertEquals(expectedOutput, generatedUrl);
	}
}
