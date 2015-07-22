package io.reflection.salesdatagather.services;

import io.reflection.salesdatagather.model.enums.ITunesPlatform;
import io.reflection.salesdatagather.model.nondb.CsvRevenueAndDownloadEntry;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.input.BOMInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import au.com.bytecode.opencsv.CSVReader;

@Service
public class SalesDataProcessor {
	private transient static final Logger LOG = LoggerFactory.getLogger(SalesDataProcessor.class.getName());

	public HashMap<Date, CsvRevenueAndDownloadEntry> convertSalesAndDownloadsCSV(Path salesFile, Path downloadsFile, Path iapSalesFile){

		LOG.debug(String.format("Sales file: %s, downloads: %s", salesFile, downloadsFile));



		HashMap<Date, CsvRevenueAndDownloadEntry> map = getMapOfDatesFromFirstAvailableFile(salesFile, downloadsFile, iapSalesFile);
		if(map==null) {
			LOG.error("Could not process the sales file.");
			return null;
		}
		convertSales(map, salesFile);
		convertDownloads(map, downloadsFile);
		convertIaps(map, iapSalesFile);

		return map;
	}

	private HashMap<Date, CsvRevenueAndDownloadEntry> getMapOfDatesFromFirstAvailableFile(Path salesFile, Path downloadsFile, Path iapSalesFile) {
		HashMap<Date, CsvRevenueAndDownloadEntry> map = new HashMap<Date, CsvRevenueAndDownloadEntry>(31);
		SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy");

		ArrayList<Date> dateList = new ArrayList<Date>(31);

		File fileToProcess = downloadsFile.toFile().exists()?downloadsFile.toFile():salesFile.toFile().exists()?salesFile.toFile():iapSalesFile.toFile().exists()?iapSalesFile.toFile():null;
		if(fileToProcess==null) return null;

		try (CSVReader csvReader = new CSVReader(new InputStreamReader(new BOMInputStream(new FileInputStream(fileToProcess), false)))){
			List<String[]> rows = csvReader.readAll();

			if(rows.size()<1) throw new RuntimeException("Not enough records to process in this csv file");

			List<String> dateRow = Arrays.asList(rows.get(0));

			// we expect the sales file to have platform in cell 0,0 and Measure in the second column of the first row 1,2.
			if(!"Platform".equals(dateRow.get(0)) || !"Measure".equals(dateRow.get(1))) throw new RuntimeException("File does not start with the data we expected.");

			//we loop through from the third column to the second last (the last one has the total values)
			Date date = null;
			for(int i=2; i < dateRow.size()-1; i++) {
				String dateValue = dateRow.get(i);
				try {
					date = sdf.parse(dateValue);
				} catch (ParseException e) {
					if(LOG.isDebugEnabled()) {
						LOG.debug("Could not parse the date on column "+(i+1)+" of the sales file. The value is: "+dateValue, e);
						return null;
					}
				}

				dateList.add(date);
				map.put(date,  new CsvRevenueAndDownloadEntry(date));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return map;
	}

	private void convertSales(HashMap<Date, CsvRevenueAndDownloadEntry> map, Path salesFile) {
		if(!salesFile.toFile().exists()) return;

		SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy");

		ArrayList<Date> dateList = new ArrayList<Date>(31);
		try (CSVReader csvReader = new CSVReader(new InputStreamReader(new BOMInputStream(new FileInputStream(salesFile.toFile()), false)))){

			List<String[]> rows = csvReader.readAll();

			if(rows.size()<1) throw new RuntimeException("Not enough records to process in this csv file");

			List<String> dateRow = Arrays.asList(rows.get(0));

			// we expect the sales file to have platform in cell 0,0 and Measure in the second column of the first row 1,2.
			if(!"Platform".equals(dateRow.get(0)) || !"Measure".equals(dateRow.get(1))) throw new RuntimeException("File does not start with the data we expected.");

			//we loop through from the third column to the second last (the last one has the total values)
			Date date = null;
			for(int i=2; i < dateRow.size()-1; i++) {
				String dateValue = dateRow.get(i);
				try {
					date = sdf.parse(dateValue);
				} catch (ParseException e) {
					if(LOG.isDebugEnabled()) {
						LOG.debug("Could not parse the date on column "+(i+1)+" of the sales file. The value is: "+dateValue, e);
						return;
					}
				}

				dateList.add(date);
				map.put(date,  new CsvRevenueAndDownloadEntry(date));
			}

			//loop through the platforms present
			for(int currentLine=1;currentLine<rows.size()-1;currentLine++) {
				List<String> row = Arrays.asList(rows.get(currentLine));

				ITunesPlatform platform = null;

				try {platform = ITunesPlatform.getPlatformByName(row.get(0).trim());}catch(Exception e) {LOG.error("***** Exception while trying to get platform for text: "+row.get(0)+e.getMessage());}

				LOG.debug(String.format("Got platform %s from string %s", platform, row.get(0)));

				//try and get the platform from the string. If there is a problem, proceed to the next row
				if(platform==null) {
					continue;
				}

				for(int i=2; i < row.size()-1; i++) {
					int value = 0;

					try {
						Double saleValue = Double.parseDouble(row.get(i));
						value = (int) (100 * saleValue);

						LOG.debug(String.format("Processing sale value for row %d. String: %s, Double: %f, Int: %d",i, row.get(i), saleValue, value));
					} catch (NumberFormatException e) {
						LOG.debug("Could not convert revenue string into a number. String was: "+row.get(i));
					}

					if(value!=0) {
						map.get(dateList.get(i-2)).setRevenueValue(platform, value);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void convertDownloads(HashMap<Date, CsvRevenueAndDownloadEntry> map, Path downloadsFile) {
		if(!downloadsFile.toFile().exists()) return;

		SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy");
		ArrayList<Date> dateList = new ArrayList<Date>(31);
		try (CSVReader csvReader = new CSVReader(new InputStreamReader(new BOMInputStream(new FileInputStream(downloadsFile.toFile()), false)))){

			List<String[]> rows = csvReader.readAll();

			if(rows.size()<1) throw new RuntimeException("Not enough records to process in this csv file");

			List<String> dateRow = Arrays.asList(rows.get(0));

			// we expect the sales file to have platform in cell 0,0 and Measure in the second column of the first row 1,2.
			if(!"Platform".equals(dateRow.get(0)) || !"Measure".equals(dateRow.get(1))) throw new RuntimeException("File does not start with the data we expected.");

			// we loop through from the third column to the second last (the last one has the total values)
			Date date = null;
			for(int i=2; i < dateRow.size()-1; i++) {
				try {
					date = sdf.parse(dateRow.get(i));
				} catch (ParseException e) {
					if(LOG.isDebugEnabled()) {
						LOG.debug("Could not parse the date on column "+(i+1)+" of the sales file. The value is: "+dateRow.get(i), e);
						return;
					}
				}

				dateList.add(date);
			}

			//loop through the platforms present
			for(int currentLine=1;currentLine<rows.size()-1;currentLine++) {
				List<String> row = Arrays.asList(rows.get(currentLine));

				ITunesPlatform platform = null;

				try {platform = ITunesPlatform.getPlatformByName(row.get(0).toUpperCase());}catch(Exception e) {}
				//try and get the platform from the string. If there is a problem, proceed to the next row
				if(platform==null) {
					continue;
				}

				for(int i=2; i < row.size()-1; i++) {
					int value = 0;

					try {
						value = Integer.parseInt(row.get(i));
						LOG.debug(String.format("Processing downloads value for row %d. String: %s, Int: %d",i, row.get(i), value));
					} catch (NumberFormatException e) {
						LOG.debug("Could not convert downloads string into a number. String was: "+row.get(i));
					}

					if(value!=0) {
						//TODO get sometimes get a NPE here. Investigate why
						map.get(dateList.get(i-2)).setDownloadValue(platform, value);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void convertIaps(HashMap<Date, CsvRevenueAndDownloadEntry> map, Path iapSalesFile) {
		if(!iapSalesFile.toFile().exists()) return;

		SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy");
		ArrayList<Date> dateList = new ArrayList<Date>(31);
		try (CSVReader csvReader = new CSVReader(new InputStreamReader(new BOMInputStream(new FileInputStream(iapSalesFile.toFile()), false)))){

			List<String[]> rows = csvReader.readAll();

			if(rows.size()<1) throw new RuntimeException("Not enough records to process in this csv file");

			List<String> dateRow = Arrays.asList(rows.get(0));

			// we expect the sales file to have platform in cell 0,0 and Measure in the second column of the first row 1,2.
			if(!"Platform".equals(dateRow.get(0)) || !"Measure".equals(dateRow.get(1))) throw new RuntimeException("File does not start with the data we expected.");

			// we loop through from the third column to the second last (the last one has the total values)
			Date date = null;
			for(int i=2; i < dateRow.size()-1; i++) {
				try {
					date = sdf.parse(dateRow.get(i));
				} catch (ParseException e) {
					if(LOG.isDebugEnabled()) {
						LOG.debug("Could not parse the date on column "+(i+1)+" of the sales file. The value is: "+dateRow.get(i), e);
						return;
					}
				}

				dateList.add(date);
			}


			//loop through the platforms present
			for(int currentLine=1;currentLine<rows.size()-1;currentLine++) {
				List<String> row = Arrays.asList(rows.get(currentLine));

				ITunesPlatform platform = null;

				try {platform = ITunesPlatform.getPlatformByName(row.get(0).trim());}catch(Exception e) {LOG.error("***** Exception while trying to get platform for text: "+row.get(0)+e.getMessage());}

				LOG.debug(String.format("Got platform %s from string %s", platform, row.get(0)));

				//try and get the platform from the string. If there is a problem, proceed to the next row
				if(platform==null) {
					continue;
				}

				for(int i=2; i < row.size()-1; i++) {
					int value = 0;

					try {
						Double saleValue = Double.parseDouble(row.get(i));
						value = (int) (100 * saleValue);

						LOG.debug(String.format("Processing IAP sale value for row %d. String: %s, Double: %f, Int: %d",i, row.get(i), saleValue, value));
					} catch (NumberFormatException e) {
						LOG.debug("Could not convert IAP revenue string into a number. String was: "+row.get(i));
					}

					if(value!=0) {
						map.get(dateList.get(i-2)).setIapRevenueValue(platform, value);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
