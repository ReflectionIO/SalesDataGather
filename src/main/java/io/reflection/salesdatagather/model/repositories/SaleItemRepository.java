package io.reflection.salesdatagather.model.repositories;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SaleItemRepository {

	@Autowired
	private JdbcTemplate	jdbcTemplate;

	public List<String> getMainItemsSoldForDataAccountInDateRange(Integer dataAccountId, Date from, Date to) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		String dateFromStr = sdf.format(from);
		String dateToStr = sdf.format(to);

		List<Map<String, Object>> itemList = jdbcTemplate.queryForList("select distinct itemid from sale where dataaccountid=? and parentidentifier = '' and begin between ? and ?",
				new Object[] {dataAccountId, dateFromStr, dateToStr});

		List<String> itemIds = new ArrayList<String>();
		if(itemList!=null && itemList.size()>0) {
			for(Map<String, Object> itemMap : itemList) {
				itemIds.add((String) itemMap.get("itemid"));
			}
		}

		itemList = jdbcTemplate.queryForList(
				"select distinct itemid from sale where dataaccountid=? and sku in (select distinct parentidentifier from sale where dataaccountid=? and typeidentifier = 'IA1' and begin between ? and ?);",
				new Object[] {dataAccountId, dataAccountId, dateFromStr, dateToStr});

		if(itemList!=null && itemList.size()>0) {
			for(Map<String, Object> itemMap : itemList) {
				String itemId = (String) itemMap.get("itemid");

				if(!itemIds.contains(itemId)) {
					itemIds.add(itemId);
				}
			}
		}

		return itemIds;
	}

	public List<String> getIAPIdsForItem(Integer dataAccountId, String itemId) {
		List<Map<String, Object>> itemList = jdbcTemplate.queryForList("select distinct itemid from sale where dataaccountid=? and typeidentifier='IA1' and parentidentifier = (select sku from sale where dataaccountid=? and itemid=? limit 1)",
				new Object[] {dataAccountId, dataAccountId, itemId});

		List<String> itemIds = new ArrayList<String>();
		if(itemList!=null && itemList.size()>0) {
			for(Map<String, Object> itemMap : itemList) {
				itemIds.add((String) itemMap.get("itemid"));
			}
		}

		return itemIds;
	}

	public String getItemTitleByAccountAndItemId(Integer dataAccountId, String mainItemId) {
		return jdbcTemplate.queryForObject("select title from sale where dataaccountid=? and itemid=? limit 1", new Object[] {dataAccountId, mainItemId}, String.class);
	}
}
