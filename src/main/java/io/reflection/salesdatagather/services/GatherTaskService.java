package io.reflection.salesdatagather.services;

import io.reflection.salesdatagather.model.DataAccount;
import io.reflection.salesdatagather.model.nondb.CsvRevenueAndDownloadEntry;
import io.reflection.salesdatagather.model.repositories.SaleItemRepository;
import io.reflection.salesdatagather.model.repositories.SaleSummaryRepo;
import io.reflection.salesdatagather.selenium.ITunesConnectDownloader;
import io.reflection.salesdatagather.selenium.ITunesHelper;
import io.reflection.salesdatagather.selenium.SeleniumDriver;
import io.reflection.salesdatagather.tasks.GatherTask;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

@Service
public class GatherTaskService {
	private transient static final Logger LOG = LoggerFactory.getLogger(GatherTaskService.class.getName());

	@Autowired
	private TaskExecutor taskExecutorService;

	@Autowired
	private ITunesHelper itunesHelper;

	@Autowired
	private ITunesConnectDownloader downloader;

	@Autowired
	private SalesDataProcessor salesDataProcessor;

	@Autowired
	private SaleSummaryRepo	saleSummaryRepo;

	@Autowired
	private SaleItemRepository itemRepository;

	public void scheduleTaskForExecution(DataAccount dataAccount, Date dateToGatherFrom, Date dateToGatherTo, String itemIds, String mainItemId, String countryCodeToGatherFor) {
		GatherTask task = new GatherTask(dataAccount, dateToGatherFrom, dateToGatherTo, itemIds, mainItemId, countryCodeToGatherFor, this);
		taskExecutorService.execute(task);
	}

	public void executeGather(GatherTask task) {
		int currentPoolSize = ((ThreadPoolTaskExecutor)taskExecutorService).getThreadPoolExecutor().getQueue().size();

		String itemTitle = itemRepository.getItemTitleByAccountAndItemId(task.getDataAccount().getId(), task.getMainItemId());

		LOG.debug("Current queue size: "+currentPoolSize);

		Path downloadDir = null;
		try {
			downloadDir = itunesHelper.createTempDownloadsDir(task.getDataAccount().getId(), task.getMainItemId(), task.getCountryCodeToGatherFor(), task.getDateToGatherFrom(), task.getDateToGatherTo());
		} catch (IOException e) {
			LOG.error("Could not get a temporary directory from the ITunesHelper.", e);
			//TODO log as error in the DB to retry again later
			return;
		}

		FirefoxDriver driver = new SeleniumDriver(downloadDir).getDriver();
		try {
			downloader.downloadFromITunes(driver, downloadDir, task.getDataAccount().getUsername(), task.getDataAccount().getClearPassword(), task.getMainItemId(), task.getItemIds(), task.getCountryCodeToGatherFor(), task.getDateToGatherFrom(), task.getDateToGatherTo());
		}finally {
			driver.quit();
		}

		Path downloadsFile = downloadDir.resolve(ITunesConnectDownloader.DOWNLOADS_FILE_NAME);
		Path salesFile = downloadDir.resolve(ITunesConnectDownloader.SALES_FILE_NAME);
		Path iapSalesFile = downloadDir.resolve(ITunesConnectDownloader.IAP_SALES_FILE_NAME);

		//process downloads and sales files into a map of revenue and downloads per date
		HashMap<Date, CsvRevenueAndDownloadEntry> salesAndDownloadsMap = salesDataProcessor.convertSalesAndDownloadsCSV(salesFile, downloadsFile, iapSalesFile);

		saleSummaryRepo.updateSalesSummaries(task.getDataAccount(), task.getCountryCodeToGatherFor(), task.getMainItemId(), itemTitle, salesAndDownloadsMap);
	}
}
