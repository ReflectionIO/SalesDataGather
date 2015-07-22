package io.reflection.salesdatagather.services;

import io.reflection.salesdatagather.SalesDataGatherApplication;
import io.reflection.salesdatagather.model.DataAccount;
import io.reflection.salesdatagather.model.enums.ITunesPlatform;
import io.reflection.salesdatagather.model.nondb.CsvRevenueAndDownloadEntry;
import io.reflection.salesdatagather.model.repositories.SaleSummaryRepo;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.collections.ComparatorUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SalesDataGatherApplication.class)
@WebAppConfiguration
public class SalesDataProcessorTest {
	private transient static final Logger LOG = LoggerFactory.getLogger(SalesDataProcessorTest.class.getName());

	@Autowired
	private SaleSummaryRepo saleSummaryRepo;

	@Test
	//	@Ignore // to enable this test, the sample test directory needs to be present. We need to get a sample file and move it into the test resources dir
	public void convertSalesAndDownloadsCSVTest() {
		Path downloadDir = Paths.get("/tmp/ff/259-461402734-gb-2015-06-01-2015-06-30-2757180851312207524");
		Path downloadsFile = downloadDir.resolve("downloads.csv");
		Path salesFile = downloadDir.resolve("sales.csv");
		Path iapSalesFile = downloadDir.resolve("iap_sales.csv");

		SalesDataProcessor processor = new SalesDataProcessor();
		HashMap<Date, CsvRevenueAndDownloadEntry> salesAndDownloadsMap = processor.convertSalesAndDownloadsCSV(salesFile, downloadsFile, iapSalesFile);

		ArrayList<Date> dateList = new ArrayList<Date>(salesAndDownloadsMap.keySet());
		Collections.sort(dateList, ComparatorUtils.naturalComparator());
		for(Date date: dateList) {
			CsvRevenueAndDownloadEntry entry = salesAndDownloadsMap.get(date);
			LOG.debug(String.format("%s, phone rev: %d, phone downloads: %d, tablet rev: %d, tablet downloads: %s, desktop rev:%d, desktop downloads: %d",
					date.toString(),
					(entry.getRevenueValue(ITunesPlatform.IPHONE) + entry.getRevenueValue(ITunesPlatform.IPOD)),
					(entry.getDownloadValue(ITunesPlatform.IPHONE) + entry.getDownloadValue(ITunesPlatform.IPOD)),
					entry.getRevenueValue(ITunesPlatform.IPAD),
					entry.getDownloadValue(ITunesPlatform.IPAD),
					entry.getRevenueValue(ITunesPlatform.DESKTOP),
					entry.getDownloadValue(ITunesPlatform.DESKTOP)
					));

			DataAccount account = new DataAccount();
			account.setId(259);

			saleSummaryRepo.updateSalesSummaries(account, "gb", "461402734", "Diamond Dash", salesAndDownloadsMap);
		}
	}
}
