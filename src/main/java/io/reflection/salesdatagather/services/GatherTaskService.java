package io.reflection.salesdatagather.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.ComparatorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import io.reflection.salesdatagather.AppConfig;
import io.reflection.salesdatagather.model.DataAccount;
import io.reflection.salesdatagather.model.SplitDataFetch;
import io.reflection.salesdatagather.model.enums.ITunesPlatform;
import io.reflection.salesdatagather.model.enums.ReportFileType;
import io.reflection.salesdatagather.model.enums.SplitDataFetchStatus;
import io.reflection.salesdatagather.model.nondb.CsvRevenueAndDownloadEntry;
import io.reflection.salesdatagather.model.nondb.GatherTask;
import io.reflection.salesdatagather.model.nondb.LeasedTask;
import io.reflection.salesdatagather.model.repositories.DataAccountRepository;
import io.reflection.salesdatagather.model.repositories.SaleItemRepository;
import io.reflection.salesdatagather.model.repositories.SaleSummaryRepo;
import io.reflection.salesdatagather.model.repositories.SplitDataFetchRepo;
import io.reflection.salesdatagather.selenium.ITunesConnectDownloader;
import io.reflection.salesdatagather.selenium.ITunesHelper;

@Service
public class GatherTaskService {
	private transient static final Logger LOG = LoggerFactory.getLogger(GatherTaskService.class.getName());

	@Autowired
	private AppConfig appConfig;

	@Autowired
	private TaskExecutor taskExecutorService;

	@Autowired
	private TaskService taskService;

	@Autowired
	private ITunesHelper itunesHelper;

	@Autowired
	private ITunesConnectDownloader downloader;

	@Autowired
	private SalesDataProcessor salesDataProcessor;

	@Autowired
	private SaleSummaryRepo saleSummaryRepo;

	@Autowired
	private SaleItemRepository itemRepository;

	@Autowired
	private SplitDataFetchRepo splitDataFetchRepo;

	@Autowired
	private CloudStorageService cloudStorageService;

	@Autowired
	private DataAccountRepository dataAccountRepo;

	public void scheduleTaskForExecution(LeasedTask task) {
		GatherTask gatherTask = createGatherTaskFromParams(task.getParamMap());
		if (gatherTask == null) return;

		gatherTask.setLeasedTask(task);
		scheduleTaskForExecution(gatherTask);
	}

	public void scheduleTaskForExecution(DataAccount dataAccount, Date dateToGatherFrom, Date dateToGatherTo, String itemIds, String mainItemId, String countryCodeToGatherFor) {
		GatherTask task = new GatherTask(dataAccount, dateToGatherFrom, dateToGatherTo, itemIds, mainItemId, countryCodeToGatherFor, this);
		scheduleTaskForExecution(task);
		// executeGather(task);
	}

	public void scheduleTaskForExecution(GatherTask task) {
		taskExecutorService.execute(task);
		// executeGather(task);
	}

	public void executeGather(GatherTask task) {
		int currentPoolSize = ((ThreadPoolTaskExecutor) taskExecutorService).getThreadPoolExecutor().getQueue().size();
		LOG.debug("Current queue size: " + currentPoolSize);

		if (!appConfig.getActiveCountryCodes().contains(task.getCountryCodeToGatherFor())) {
			taskService.deleteTask(task.getLeasedTask());
			return;
		}

		SplitDataFetch splitDataFetch = splitDataFetchRepo.findBy(task.getDataAccount().getId(), task.getDateToGatherFrom(), task.getDateToGatherTo(), task.getCountryCodeToGatherFor(),
				task.getMainItemId());

		if (splitDataFetch == null) {
			splitDataFetch = splitDataFetchRepo.createSplitDataFetch(
					task.getDataAccount().getId(),
					task.getMainItemId(),
					task.getDateToGatherFrom(),
					task.getDateToGatherTo(),
					task.getCountryCodeToGatherFor());
		} else if (splitDataFetch.getStatus().equalsIgnoreCase(SplitDataFetchStatus.INGESTED.toString())) {
			// if already ingested don't bother doing this again and delete the task
			// from the list
			taskService.deleteTask(task.getLeasedTask());
			return;
		}

		Path downloadDir = null;
		try {
			downloadDir = itunesHelper.createTempDownloadsDir(task.getDataAccount().getId(), task.getMainItemId(), task.getCountryCodeToGatherFor(), task.getDateToGatherFrom(), task.getDateToGatherTo());
		} catch (IOException e) {
			LOG.error("Could not get a temporary directory from the ITunesHelper.", e);
			return;
		}

		String downloadsUrl = null;
		String salesUrl = null;
		String iapSalesUrl = null;

		Path downloadsFile = null;
		Path salesFile = null;
		Path iapSalesFile = null;

		if (splitDataFetch.getStatus().equalsIgnoreCase(SplitDataFetchStatus.GATHERED.toString())) {
			downloadsUrl = splitDataFetch.getDownloadsReportUrl();
			salesUrl = splitDataFetch.getSalesReportUrl();
			iapSalesUrl = splitDataFetch.getIapReportUrl();

			downloadsFile = cloudStorageService.downloadFile(downloadDir, downloadsUrl);
			salesFile = cloudStorageService.downloadFile(downloadDir, salesUrl);
			iapSalesFile = cloudStorageService.downloadFile(downloadDir, iapSalesUrl);

			markForDeletionOnExit(downloadsFile, salesFile, iapSalesFile);
		} else {
			downloader.downloadFromITunes(downloadDir, task.getDataAccount().getUsername(), task.getDataAccount().getClearPassword(), task.getMainItemId(), task.getItemIds(),
					task.getCountryCodeToGatherFor(), task.getDateToGatherFrom(), task.getDateToGatherTo());

			downloadsFile = downloadDir.resolve(ITunesConnectDownloader.DOWNLOADS_FILE_NAME);
			salesFile = downloadDir.resolve(ITunesConnectDownloader.SALES_FILE_NAME);
			iapSalesFile = downloadDir.resolve(ITunesConnectDownloader.IAP_SALES_FILE_NAME);

			if (downloadsFile.toFile().exists()) {
				downloadsUrl = cloudStorageService.uploadFile(downloadsFile.toFile(), generateFileName(task, ReportFileType.DOWNLOADS));
				splitDataFetch.setDownloadsReportUrl(downloadsUrl);
			}

			if (salesFile.toFile().exists()) {
				salesUrl = cloudStorageService.uploadFile(salesFile.toFile(), generateFileName(task, ReportFileType.SALES));
				splitDataFetch.setSalesReportUrl(salesUrl);
			}

			if (iapSalesFile.toFile().exists()) {
				iapSalesUrl = cloudStorageService.uploadFile(iapSalesFile.toFile(), generateFileName(task, ReportFileType.IAPS_SALES));
				splitDataFetch.setIapReportUrl(iapSalesUrl);
			}

			splitDataFetch.setStatus(SplitDataFetchStatus.GATHERED.toString());
			splitDataFetchRepo.updateSplitDataFetch(splitDataFetch);
		}

		// process downloads and sales files into a map of revenue and downloads per
		// date
		HashMap<Date, CsvRevenueAndDownloadEntry> salesAndDownloadsMap = salesDataProcessor.convertSalesAndDownloadsCSV(salesFile, downloadsFile, iapSalesFile);

		processSalesAndDownloadData(task, salesAndDownloadsMap);

		splitDataFetch.setStatus(SplitDataFetchStatus.INGESTED.toString());
		splitDataFetchRepo.updateSplitDataFetch(splitDataFetch);

		tryAndDeleteFile(downloadsFile);
		tryAndDeleteFile(salesFile);
		tryAndDeleteFile(iapSalesFile);

		taskService.deleteTask(task.getLeasedTask());
	}

	private void markForDeletionOnExit(Path... paths) {
		for (Path path : paths) {
			File file = path.toFile();
			if (file.exists()) {
				file.deleteOnExit();
			}
		}
	}

	private void tryAndDeleteFile(Path... paths) {
		for (Path path : paths) {
			File file = path.toFile();
			if (file.exists()) {
				file.delete();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void processSalesAndDownloadData(GatherTask task, HashMap<Date, CsvRevenueAndDownloadEntry> salesAndDownloadsMap) {
		ArrayList<Date> dateList = new ArrayList<Date>(salesAndDownloadsMap.keySet());
		Collections.sort(dateList, ComparatorUtils.naturalComparator());

		String itemTitle = itemRepository.getItemTitleByAccountAndItemId(task.getDataAccount().getId(), task.getMainItemId());

		for (Date date : dateList) {
			CsvRevenueAndDownloadEntry entry = salesAndDownloadsMap.get(date);

			int phoneRevenue = entry.getRevenueValue(ITunesPlatform.IPHONE) + entry.getRevenueValue(ITunesPlatform.IPOD);
			int tabletRevenue = entry.getRevenueValue(ITunesPlatform.IPAD);
			int totalRevenue = phoneRevenue + tabletRevenue;

			int phoneIapRevenue = entry.getIapRevenueValue(ITunesPlatform.IPHONE) + entry.getIapRevenueValue(ITunesPlatform.IPOD);
			int tabletIapRevenue = entry.getIapRevenueValue(ITunesPlatform.IPAD);
			int totalIapRevenue = phoneIapRevenue + tabletIapRevenue;

			double phoneRevenueRatio = (phoneRevenue == 0 || totalRevenue == 0) ? 0 : (double) phoneRevenue / (double) totalRevenue;
			double tabletRevenueRatio = (tabletRevenue == 0 || totalRevenue == 0) ? 0 : (double) tabletRevenue / (double) totalRevenue;

			double phoneIapRevenueRatio = (phoneIapRevenue == 0 || totalIapRevenue == 0) ? 0 : (double) phoneIapRevenue / (double) totalIapRevenue;
			double tabletIapRevenueRatio = (tabletIapRevenue == 0 || totalIapRevenue == 0) ? 0 : (double) tabletIapRevenue / (double) totalIapRevenue;

			int phoneDownloads = entry.getDownloadValue(ITunesPlatform.IPHONE) + entry.getDownloadValue(ITunesPlatform.IPOD);
			int tabletDownloads = entry.getDownloadValue(ITunesPlatform.IPAD);
			int totalDownloads = phoneDownloads + tabletDownloads;

			saleSummaryRepo.updateSalesSummaries(task.getDataAccount(), task.getCountryCodeToGatherFor(), task.getMainItemId(), itemTitle, date,
					phoneRevenueRatio, tabletRevenueRatio, phoneIapRevenueRatio, tabletIapRevenueRatio,
					phoneDownloads, tabletDownloads, totalDownloads);
		}
	}

	private String generateFileName(GatherTask task, ReportFileType reportFileType) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		return new StringBuilder(appConfig.getGoogleStorageFilePrefix())
				.append('_')
				.append(task.getDataAccount().getId())
				.append('_')
				.append(task.getMainItemId())
				.append('_')
				.append(task.getCountryCodeToGatherFor())
				.append('_')
				.append(sdf.format(task.getDateToGatherFrom()))
				.append('_')
				.append(sdf.format(task.getDateToGatherTo()))
				.append('_')
				.append(reportFileType.toString().toLowerCase())
				.toString();
	}

	private GatherTask createGatherTaskFromParams(Map<String, String> paramMap) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

			DataAccount dataAccount = dataAccountRepo.getDataAccountById(Integer.valueOf(paramMap.get("dataAccountId")));

			if (dataAccount == null) return null;

			Date gatherFrom = sdf.parse(paramMap.get("gatherFrom"));
			Date gatherTo = sdf.parse(paramMap.get("gatherTo"));

			String mainItemId = paramMap.get("mainItemId");
			String iapItemIds = paramMap.get("iapItemIds");

			String countryCode = paramMap.get("countryCode");

			GatherTask task = new GatherTask(dataAccount, gatherFrom, gatherTo, iapItemIds, mainItemId, countryCode, this);

			return task;
		} catch (NumberFormatException e) {
			LOG.error("Number format exception when trying to create GatherTask from params: " + paramMap.toString(), e);
		} catch (ParseException e) {
			LOG.error("Date parse exception when trying to create GatherTask from params: " + paramMap.toString(), e);
		}
		return null;
	}
}
