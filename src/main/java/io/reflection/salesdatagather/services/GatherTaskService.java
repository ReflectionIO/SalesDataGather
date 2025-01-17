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
import io.reflection.salesdatagather.model.repositories.SplitDataRatioRepo;
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
	private SplitDataRatioRepo splitDataRatioRepo;

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
	}

	public void scheduleTaskForExecution(GatherTask task) {
		LOG.debug(String.format("Schedulling task: %s", task.getLeasedTask().getGoogleLeasedTask().getId()));
		taskExecutorService.execute(task);
	}

	public void executeGather(GatherTask task) {
		if (!appConfig.getActiveCountryCodes().contains(task.getCountryCodeToGatherFor())) {
			LOG.debug(String.format("Ignoring task %s as it's country %s is not in our list of countries to process.", task.getLeasedTask().getGoogleLeasedTask().getId(), task.getCountryCodeToGatherFor()));
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

		Path downloadsFilePath = null;
		Path salesFilePath = null;
		Path iapSalesFilePath = null;

		if (splitDataFetch.getStatus().equalsIgnoreCase(SplitDataFetchStatus.GATHERED.toString()) || splitDataFetch.getStatus().equalsIgnoreCase(SplitDataFetchStatus.INGESTED.toString())) {
			downloadsUrl = splitDataFetch.getDownloadsReportUrl();
			salesUrl = splitDataFetch.getSalesReportUrl();
			iapSalesUrl = splitDataFetch.getIapReportUrl();

			downloadsFilePath = cloudStorageService.downloadFile(downloadDir, downloadsUrl);
			salesFilePath = cloudStorageService.downloadFile(downloadDir, salesUrl);
			iapSalesFilePath = cloudStorageService.downloadFile(downloadDir, iapSalesUrl);

			markForDeletionOnExit(downloadsFilePath, salesFilePath, iapSalesFilePath);
		} else {
			downloader.downloadFromITunes(downloadDir, task.getDataAccount().getUsername(), task.getDataAccount().getClearPassword(), task.getMainItemId(), task.getItemIds(),
					task.getCountryCodeToGatherFor(), task.getDateToGatherFrom(), task.getDateToGatherTo());

			downloadsFilePath = downloadDir.resolve(ITunesConnectDownloader.DOWNLOADS_FILE_NAME);
			salesFilePath = downloadDir.resolve(ITunesConnectDownloader.SALES_FILE_NAME);
			iapSalesFilePath = downloadDir.resolve(ITunesConnectDownloader.IAP_SALES_FILE_NAME);

			File downloadsFile = downloadsFilePath.toFile();
			if (downloadsFile.exists() && downloadsFile.length() > 0) {
				downloadsUrl = cloudStorageService.uploadFile(downloadsFile, generateFileName(task, ReportFileType.DOWNLOADS));
				splitDataFetch.setDownloadsReportUrl(downloadsUrl);
			}

			File salesFile = salesFilePath.toFile();
			if (salesFile.exists() && salesFile.length() > 0) {
				salesUrl = cloudStorageService.uploadFile(salesFile, generateFileName(task, ReportFileType.SALES));
				splitDataFetch.setSalesReportUrl(salesUrl);
			}

			File iapSalesFile = iapSalesFilePath.toFile();
			if (iapSalesFile.exists() && iapSalesFile.length() > 0) {
				iapSalesUrl = cloudStorageService.uploadFile(iapSalesFile, generateFileName(task, ReportFileType.IAPS_SALES));
				splitDataFetch.setIapReportUrl(iapSalesUrl);
			}

			if (doesAtLeastOneFileExistWithData(downloadsFilePath, salesFilePath, iapSalesFilePath)) {
				splitDataFetch.setStatus(SplitDataFetchStatus.GATHERED.toString());
				splitDataFetchRepo.updateSplitDataFetch(splitDataFetch);
			}
		}

		if (doesAtLeastOneFileExistWithData(downloadsFilePath, salesFilePath, iapSalesFilePath)) {
			// process downloads and sales files into a map of revenue and downloads per date
			HashMap<Date, CsvRevenueAndDownloadEntry> salesAndDownloadsMap = salesDataProcessor.convertSalesAndDownloadsCSV(salesFilePath, downloadsFilePath, iapSalesFilePath);

			if (salesAndDownloadsMap != null) {
				processSalesAndDownloadData(task, salesAndDownloadsMap);
			}

			if (!splitDataFetch.getStatus().equalsIgnoreCase(SplitDataFetchStatus.INGESTED.toString())) {
				splitDataFetch.setStatus(SplitDataFetchStatus.INGESTED.toString());
				splitDataFetchRepo.updateSplitDataFetch(splitDataFetch);
			}
		}

		taskService.deleteTask(task.getLeasedTask());

		tryAndDeleteFile(downloadsFilePath, salesFilePath, iapSalesFilePath, downloadDir);
	}

	private boolean doesAtLeastOneFileExistWithData(Path... paths) {
		if (paths == null) return false;

		for (Path path : paths) {
			if (path != null && path.toFile().exists() && path.toFile().length() > 0) return true;
		}

		return false;
	}

	private void markForDeletionOnExit(Path... paths) {
		if (paths == null) return;

		for (Path path : paths) {
			if (path == null) {
				continue;
			}

			File file = path.toFile();
			if (file.exists()) {
				file.deleteOnExit();
			}
		}
	}

	private void tryAndDeleteFile(Path... paths) {
		for (Path path : paths) {
			if (path == null) {
				continue;
			}

			File file = path.toFile();
			if (file.exists()) {
				try {
					file.delete();
				} catch (Exception e) {
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void processSalesAndDownloadData(GatherTask task, HashMap<Date, CsvRevenueAndDownloadEntry> salesAndDownloadsMap) {
		ArrayList<Date> dateList = new ArrayList<Date>(salesAndDownloadsMap.keySet());
		Collections.sort(dateList, ComparatorUtils.naturalComparator());

		String itemTitle = null;
		try {
			itemTitle = itemRepository.getItemTitleByAccountAndItemId(task.getDataAccount().getId(), task.getMainItemId());
		} catch (Exception e) {
			LOG.error(String.format("Could not get the title of an item. Account ID: %s, ItemId: %s", task.getDataAccountId(), task.getMainItemId()));
			itemTitle = "";
		}

		int updatedCount = 0;

		for (Date date : dateList) {
			CsvRevenueAndDownloadEntry entry = salesAndDownloadsMap.get(date);

			int phoneRevenue = entry.getRevenueValue(ITunesPlatform.IPHONE) + entry.getRevenueValue(ITunesPlatform.IPOD);
			int tabletRevenue = entry.getRevenueValue(ITunesPlatform.IPAD);
			int totalRevenue = phoneRevenue + tabletRevenue + entry.getRevenueValue(ITunesPlatform.DESKTOP);

			int phoneIapRevenue = entry.getIapRevenueValue(ITunesPlatform.IPHONE) + entry.getIapRevenueValue(ITunesPlatform.IPOD);
			int tabletIapRevenue = entry.getIapRevenueValue(ITunesPlatform.IPAD);
			int totalIapRevenue = phoneIapRevenue + tabletIapRevenue + entry.getIapRevenueValue(ITunesPlatform.DESKTOP);

			double phoneRevenueRatio = (phoneRevenue == 0 || totalRevenue == 0) ? 0 : (double) phoneRevenue / (double) totalRevenue;
			double tabletRevenueRatio = (tabletRevenue == 0 || totalRevenue == 0) ? 0 : (double) tabletRevenue / (double) totalRevenue;

			double phoneIapRevenueRatio = (phoneIapRevenue == 0 || totalIapRevenue == 0) ? 0 : (double) phoneIapRevenue / (double) totalIapRevenue;
			double tabletIapRevenueRatio = (tabletIapRevenue == 0 || totalIapRevenue == 0) ? 0 : (double) tabletIapRevenue / (double) totalIapRevenue;

			int phoneDownloads = entry.getDownloadValue(ITunesPlatform.IPHONE) + entry.getDownloadValue(ITunesPlatform.IPOD);
			int tabletDownloads = entry.getDownloadValue(ITunesPlatform.IPAD);
			int totalDownloads = phoneDownloads + tabletDownloads;

			try {
				int updated = saleSummaryRepo.updateSalesSummaries(task.getDataAccount(), task.getCountryCodeToGatherFor(), task.getMainItemId(), itemTitle, date,
						phoneRevenueRatio, tabletRevenueRatio, phoneIapRevenueRatio, tabletIapRevenueRatio,
						phoneDownloads, tabletDownloads, totalDownloads);
				if (updated > 0) {
					updatedCount++;
				}
			} catch (Exception e) {
				LOG.error(String.format("Exception occured when trying to update sale summary. Account ID: %s, ItemId: %s, Date: %s", task.getDataAccountId(), task.getMainItemId(), date), e);
			}

			try {
				splitDataRatioRepo.createOrUpdateSplitDataRatio(
						task.getDataAccountId(), task.getMainItemId(), task.getCountryCodeToGatherFor(), date,
						phoneRevenueRatio, tabletRevenueRatio,
						phoneIapRevenueRatio, tabletIapRevenueRatio,
						phoneDownloads, tabletDownloads, totalDownloads);
			} catch (Exception e) {
				LOG.error(String.format("Exception occured when trying to insert/update split data ratio. Account ID: %s, ItemId: %s, Date: %s", task.getDataAccountId(), task.getMainItemId(), date), e);
			}
		}

		LOG.debug(
				String.format("Updated %d out of %d sales summary records for dataaccount: %s, itemid: %s - %s", updatedCount, dateList.size(), task.getDataAccountId(), task.getMainItemId(), itemTitle));
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

	public GatherTask createGatherTaskFromParams(Map<String, String> paramMap) {
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
