package io.reflection.salesdatagather.model.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import io.reflection.salesdatagather.model.SplitDataFetch;
import io.reflection.salesdatagather.model.enums.SplitDataFetchStatus;

@Repository
public class SplitDataFetchRepo {
	private transient static final Logger LOG = LoggerFactory.getLogger(SplitDataFetchRepo.class.getName());

	private final JdbcTemplate jdbcTemplate;

	@Autowired(required = true)
	public SplitDataFetchRepo(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public SplitDataFetch createSplitDataFetch(final Integer dataAccountId, final String itemId, final Date gatheredFrom, final Date gatheredTo, final String country) {
		LOG.trace(
				String.format("Inserting new SplitDateFetch. DataAcc:%d, Item:%s, from:%s, to:%s, country: %s",
						dataAccountId, itemId, gatheredFrom, gatheredTo, country));

		String updateSql = "INSERT INTO "
				+ " split_data_fetch (data_account_id, fetch_date, fetch_time, country, itemid, status, from_date, to_date)"
				+ " VALUES ( ?, ?, ?, ?,    ?, ?, ?, ? )";

		GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
		int rowsInserted = jdbcTemplate.update(new PreparedStatementCreator() {

			@Override
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				PreparedStatement ps = con.prepareStatement(updateSql, Statement.RETURN_GENERATED_KEYS);

				int paramCount = 1;
				ps.setInt(paramCount++, dataAccountId);
				ps.setDate(paramCount++, new java.sql.Date(System.currentTimeMillis()));
				ps.setTimestamp(paramCount++, new Timestamp(System.currentTimeMillis()));
				ps.setString(paramCount++, country);
				ps.setString(paramCount++, itemId);
				ps.setString(paramCount++, SplitDataFetchStatus.GATHERING.toString());
				ps.setDate(paramCount++, new java.sql.Date(gatheredFrom.getTime()));
				ps.setDate(paramCount++, new java.sql.Date(gatheredTo.getTime()));

				return ps;
			}
		}, keyHolder);

		if (rowsInserted == 0) return null;

		Integer key = keyHolder.getKey().intValue();

		return getSplitDataFetchById(key);
	}

	private SplitDataFetch getSplitDataFetchById(Integer key) {
		return jdbcTemplate.queryForObject("select * from split_data_fetch where split_data_fetch_id=?", new Object[] { key }, BeanPropertyRowMapper.newInstance(SplitDataFetch.class));
	}

	public SplitDataFetch findBy(Integer dataAccountId, Date dateToGatherFrom, Date dateToGatherTo, String countryCodeToGatherFor, String itemId) {
		String selectSql = "SELECT * from split_data_fetch where data_account_id=? and from_date<=? and to_date>=? and country=? and itemid=? LIMIT 1";

		SplitDataFetch splitDataFetch = null;

		try {
			splitDataFetch = jdbcTemplate.queryForObject(selectSql, new Object[] { dataAccountId, dateToGatherFrom, dateToGatherTo, countryCodeToGatherFor, itemId },
					BeanPropertyRowMapper.newInstance(SplitDataFetch.class));
		} catch (Exception e) {
			LOG.debug("Could not get split data fetch based on the query. Error Message: " + e.getMessage());
		}

		return splitDataFetch;
	}

	public void updateSplitDataFetch(SplitDataFetch splitDataFetch) {
		String updateSql = "UPDATE split_data_fetch "
				+ " (dataaccount_id, "
				+ " fetch_date, fetch_time, "
				+ " from_date, to_date, "
				+ " country, itemid, "
				+ " downloads_report_url, sales_report_url, iap_report_url, "
				+ " status, "
				+ " phone_revenue_ratio, tablet_revenue_ratio, phone_iap_revenue_ratio, tablet_iap_revenue_ratio, "
				+ " phone_downloads, tablet_downloads, total_downloads)"
				+ " VALUES ("
				+ "  ?, "
				+ "  ?, ?, "
				+ "  ?, ?, "
				+ "  ?, ?, "
				+ "  ?, ?, ?, "
				+ "  ?, "
				+ "  ?, ?, ?, ?, "
				+ "  ?, ?, ? "
				+ " ) where split_data_fetch_id = ?";

		jdbcTemplate.update(updateSql, new Object[] {
				splitDataFetch.getDataAccountId(),
				splitDataFetch.getFetchedOn(), new Timestamp(splitDataFetch.getFetchedOn().getTime()),
				splitDataFetch.getFromDate(), splitDataFetch.getToDate(),
				splitDataFetch.getCountry(), splitDataFetch.getItemId(),
				splitDataFetch.getDownloadsReportUrl(), splitDataFetch.getSalesReportUrl(), splitDataFetch.getIapReportUrl(),
				splitDataFetch.getStatus(),
				splitDataFetch.getPhoneRevenueRatio(), splitDataFetch.getTabletRevenueRatio(), splitDataFetch.getPhoneIapRevenueRatio(), splitDataFetch.getTabletIapRevenueRatio(),
				splitDataFetch.getPhoneDownloads(), splitDataFetch.getTabletDownloads(), splitDataFetch.getTotalDownloads(),
				splitDataFetch.getSplitDataFetchId(),
		});
	}
}
