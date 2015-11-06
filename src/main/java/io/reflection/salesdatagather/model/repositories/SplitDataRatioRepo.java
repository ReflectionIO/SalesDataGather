package io.reflection.salesdatagather.model.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.stereotype.Repository;

import io.reflection.salesdatagather.model.SplitDataRatio;

@Repository
public class SplitDataRatioRepo {
	private transient static final Logger LOG = LoggerFactory.getLogger(SplitDataRatioRepo.class.getName());

	private final JdbcTemplate jdbcTemplate;

	@Autowired(required = true)
	public SplitDataRatioRepo(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public SplitDataRatio createOrUpdateSplitDataRatio(final Integer dataAccountId, final String itemId, final String country, final Date date,
			final Double phoneRevenueRatio, final Double tabletRevenueRatio,
			final Double phoneIapRevenueRatio, final Double tabletIapRevenueRatio,
			final Integer phoneDownloads, final Integer tabletDownloads, final Integer totalDownloads) {
		LOG.debug(
				String.format("Inserting new SplitDateRatio. DataAcc:%d, Item:%s, country: %s, date:%s, prr: %s, trr: %s, pirr: %s, tirr: %s, pd: %s, td: %s, ttd: %s",
						dataAccountId, itemId, country, date,
						phoneRevenueRatio, tabletRevenueRatio, phoneIapRevenueRatio, tabletIapRevenueRatio, phoneDownloads, tabletDownloads, totalDownloads));

		String updateSql = "INSERT INTO "
				+ " split_data_ratio ("
				+ "  data_account_id, country, itemid, date, "
				+ "  phone_revenue_ratio, tablet_revenue_ratio, "
				+ "  phone_iap_revenue_ratio, tablet_iap_revenue_ratio, "
				+ "  phone_downloads, tablet_downloads, total_downloads)"
				+ " VALUES ( ?, ?, ?, ?,  ?, ?,  ?, ?,  ?, ?, ? ) "
				+ " ON DUPLICATE KEY UPDATE "
				+ "  phone_revenue_ratio = ?, tablet_revenue_ratio = ?, "
				+ "  phone_iap_revenue_ratio = ?, tablet_iap_revenue_ratio = ?, "
				+ "  phone_downloads = ?, tablet_downloads = ?, total_downloads = ?";

		int rowsInserted = jdbcTemplate.update(new PreparedStatementCreator() {

			@Override
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				PreparedStatement ps = con.prepareStatement(updateSql);

				int paramCount = 1;

				ps.setInt(paramCount++, dataAccountId);
				ps.setString(paramCount++, country);
				ps.setString(paramCount++, itemId);
				ps.setDate(paramCount++, new java.sql.Date(date.getTime()));

				ps.setDouble(paramCount++, phoneRevenueRatio);
				ps.setDouble(paramCount++, tabletRevenueRatio);

				ps.setDouble(paramCount++, phoneIapRevenueRatio);
				ps.setDouble(paramCount++, tabletIapRevenueRatio);

				ps.setInt(paramCount++, phoneDownloads);
				ps.setInt(paramCount++, tabletDownloads);
				ps.setInt(paramCount++, totalDownloads);

				// repeat for update on duplicate key
				ps.setDouble(paramCount++, phoneRevenueRatio);
				ps.setDouble(paramCount++, tabletRevenueRatio);

				ps.setDouble(paramCount++, phoneIapRevenueRatio);
				ps.setDouble(paramCount++, tabletIapRevenueRatio);

				ps.setInt(paramCount++, phoneDownloads);
				ps.setInt(paramCount++, tabletDownloads);
				ps.setInt(paramCount++, totalDownloads);

				return ps;
			}
		});

		if (rowsInserted == 0) return null;

		return new SplitDataRatio(dataAccountId, itemId, country, date, phoneRevenueRatio, tabletRevenueRatio,
				phoneIapRevenueRatio, tabletIapRevenueRatio, phoneDownloads, tabletDownloads, totalDownloads);
	}

	@SuppressWarnings("unused")
	private SplitDataRatio getSplitDataRatioById(Integer dataAccountId, String itemId, String country, Date date) {
		if (dataAccountId == null || itemId == null || country == null || date == null) return null;

		return jdbcTemplate.queryForObject("select * from data_account_id=? and item_id=? and country=? and date=?", new Object[] { dataAccountId, itemId, country, date },
				BeanPropertyRowMapper.newInstance(SplitDataRatio.class));
	}

	public SplitDataRatio findBy(Integer dataAccountId, String countryCodeToGatherFor, String itemId, Date date) {
		String selectSql = "SELECT * from split_data_ratio where data_account_id=? and country=? and itemid=? and date<=? LIMIT 1";

		SplitDataRatio splitDataRatio = null;

		try {
			splitDataRatio = jdbcTemplate.queryForObject(selectSql, new Object[] { dataAccountId, countryCodeToGatherFor, itemId, date },
					BeanPropertyRowMapper.newInstance(SplitDataRatio.class));
		} catch (Exception e) {
			LOG.debug("Could not get split data ratio based on the query. Error Message: " + e.getMessage());
		}

		return splitDataRatio;
	}
}
