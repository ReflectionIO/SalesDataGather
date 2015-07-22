package io.reflection.salesdatagather.model.repositories;

import io.reflection.salesdatagather.model.DataAccount;
import io.reflection.salesdatagather.model.enums.ITunesPlatform;
import io.reflection.salesdatagather.model.nondb.CsvRevenueAndDownloadEntry;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.collections.ComparatorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Repository;

@Repository
public class SaleSummaryRepo {
	private transient static final Logger LOG = LoggerFactory.getLogger(SaleSummaryRepo.class.getName());

	@Autowired
	private JdbcTemplate	jdbcTemplate;

	public void updateSalesSummaries(final DataAccount dataAccount, final String countryCode, final String mainItemId, final String itemTitle, final HashMap<Date, CsvRevenueAndDownloadEntry> salesAndDownloadsMap) {
		String update="update sale_summary set " +
				"	iphone_app_revenue = ROUND(universal_app_revenue*?), " +
				"    ipad_app_revenue = ROUND(universal_app_revenue*?), " +
				"    iphone_iap_revenue = ROUND(iap_revenue*?), " +
				"    ipad_iap_revenue = ROUND(iap_revenue*?), " +
				"    iphone_downloads=?," +
				"    ipad_downloads=?," +
				"    total_downloads=?" +
				"where" +
				"	dataaccountid=?" +
				"    and date=?" +
				"    and itemid=?" +
				"    and country=?";

		ArrayList<Date> dateList = new ArrayList<Date>(salesAndDownloadsMap.keySet());
		Collections.sort(dateList, ComparatorUtils.naturalComparator());

		for(Date date:dateList) {
			CsvRevenueAndDownloadEntry entry = salesAndDownloadsMap.get(date);
			try {
				int updatedRowCount = jdbcTemplate.update(update, new PreparedStatementSetter() {

					@Override
					public void setValues(PreparedStatement ps) throws SQLException {
						int paramCount=1;
						java.sql.Date sqlDate = new java.sql.Date(date.getTime());

						int phoneRevenue = entry.getRevenueValue(ITunesPlatform.IPHONE)+entry.getRevenueValue(ITunesPlatform.IPOD);
						int tabletRevenue = entry.getRevenueValue(ITunesPlatform.IPAD);
						int totalRevenue = phoneRevenue+tabletRevenue;

						int phoneIapRevenue = entry.getIapRevenueValue(ITunesPlatform.IPHONE)+entry.getIapRevenueValue(ITunesPlatform.IPOD);
						int tabletIapRevenue = entry.getIapRevenueValue(ITunesPlatform.IPAD);
						int totalIapRevenue = phoneIapRevenue+tabletIapRevenue;

						double phoneRevenueRation = (phoneRevenue==0 || totalRevenue==0)?0:(double)phoneRevenue/(double)totalRevenue;
						double tabletRevenueRation = (tabletRevenue==0 || totalRevenue==0)?0:(double)tabletRevenue/(double)totalRevenue;

						double phoneIapRevenueRation = (phoneIapRevenue==0 || totalIapRevenue==0)?0:(double)phoneIapRevenue/(double)totalIapRevenue;
						double tabletIapRevenueRation = (tabletIapRevenue==0 || totalIapRevenue==0)?0:(double)tabletIapRevenue/(double)totalIapRevenue;

						int phoneDownloads = entry.getDownloadValue(ITunesPlatform.IPHONE)+entry.getDownloadValue(ITunesPlatform.IPOD);
						int tabletDownloads = entry.getDownloadValue(ITunesPlatform.IPAD);
						int totalDownloads = phoneDownloads+tabletDownloads;

						LOG.debug(String.format("update %d, %s, %s, %s, %s, %f, %f, %f, %f, %d, %d, %d", dataAccount.getId(), sqlDate, mainItemId, itemTitle, countryCode,
								phoneRevenueRation, tabletRevenueRation, phoneIapRevenueRation, tabletIapRevenueRation, phoneDownloads, tabletDownloads, totalDownloads));

						ps.setDouble(paramCount++, phoneRevenueRation);
						ps.setDouble(paramCount++, tabletRevenueRation);
						ps.setDouble(paramCount++, phoneIapRevenueRation);
						ps.setDouble(paramCount++, tabletIapRevenueRation);
						ps.setDouble(paramCount++, phoneDownloads);
						ps.setDouble(paramCount++, tabletDownloads);
						ps.setDouble(paramCount++, totalDownloads);

						ps.setInt(paramCount++, dataAccount.getId());
						ps.setDate(paramCount++, sqlDate);
						ps.setString(paramCount++, mainItemId);
						ps.setString(paramCount++, countryCode);
					}
				});

				if(updatedRowCount==0) {
					LOG.info(String.format("Could not update sale summary record for: dataaccount: %d, itemid: %s, date: %s, country: %s", dataAccount.getId(), mainItemId, date, countryCode));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
