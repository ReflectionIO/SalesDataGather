package io.reflection.salesdatagather.model.repositories;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Repository;

import io.reflection.salesdatagather.model.DataAccount;

@Repository
public class SaleSummaryRepo {
	private transient static final Logger LOG = LoggerFactory.getLogger(SaleSummaryRepo.class.getName());

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public int updateSalesSummaries(DataAccount dataAccount, String countryCode, String mainItemId, String itemTitle, Date date, double phoneRevenueRatio, double tabletRevenueRatio,
			double phoneIapRevenueRatio, double tabletIapRevenueRatio, int phoneDownloads, int tabletDownloads, int totalDownloads) {

		String update = "update sale_summary set " +
				"	iphone_app_revenue = ROUND(total_app_revenue*?), " +
				"    ipad_app_revenue = ROUND(total_app_revenue*?), " +
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

		int updatedRowCount = jdbcTemplate.update(update, new PreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				int paramCount = 1;
				java.sql.Date sqlDate = new java.sql.Date(date.getTime());

				LOG.debug(String.format("update %d, %s, %s, %s, %s, %f, %f, %f, %f, %d, %d, %d", dataAccount.getId(), sqlDate, mainItemId, itemTitle, countryCode,
						phoneRevenueRatio, tabletRevenueRatio, phoneIapRevenueRatio, tabletIapRevenueRatio, phoneDownloads, tabletDownloads, totalDownloads));

				ps.setDouble(paramCount++, phoneRevenueRatio);
				ps.setDouble(paramCount++, tabletRevenueRatio);
				ps.setDouble(paramCount++, phoneIapRevenueRatio);
				ps.setDouble(paramCount++, tabletIapRevenueRatio);
				ps.setDouble(paramCount++, phoneDownloads);
				ps.setDouble(paramCount++, tabletDownloads);
				ps.setDouble(paramCount++, totalDownloads);

				ps.setInt(paramCount++, dataAccount.getId());
				ps.setDate(paramCount++, sqlDate);
				ps.setString(paramCount++, mainItemId);
				ps.setString(paramCount++, countryCode);
			}
		});

		if (updatedRowCount == 0) {
			LOG.info(String.format("Could not update sale summary record for: dataaccount: %d, itemid: %s, date: %s, country: %s", dataAccount.getId(), mainItemId, date, countryCode));
		}

		return updatedRowCount;
	}
}
