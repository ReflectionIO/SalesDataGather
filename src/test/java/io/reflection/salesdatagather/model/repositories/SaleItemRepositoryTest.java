package io.reflection.salesdatagather.model.repositories;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.reflection.salesdatagather.BaseSpringTestClass;
import io.reflection.salesdatagather.model.DataAccount;

public class SaleItemRepositoryTest extends BaseSpringTestClass {
	private transient static final Logger LOG = LoggerFactory.getLogger(SaleItemRepositoryTest.class.getName());

	@Autowired
	private DataAccountRepository dataAccountRepo;

	@Autowired
	private SaleItemRepository saleItemRepo;

	// This test is just there to make sure we can load item iaps. However
	// it is slow and hence disabled. Run it manually once in a while
	@Ignore
	@Test
	public void test() {
		int[] accountIds = new int[] { 332 };
		for (int accountId : accountIds) {
			DataAccount activeAccount = dataAccountRepo.getDataAccountById(accountId);

			Calendar cal = Calendar.getInstance();
			cal.set(2015, 5, 1);
			Date from = cal.getTime();

			cal.add(Calendar.MONTH, 1);
			cal.add(Calendar.DAY_OF_MONTH, -1);
			Date to = cal.getTime();

			LOG.debug(String.format("Processing active account: %s from %s to %s", activeAccount.getUsername(), from, to));

			List<String> mainItems = saleItemRepo.getMainItemsSoldForDataAccountInDateRange(activeAccount.getId(), from, to);
			if (mainItems == null) {
				LOG.debug(String.format("Found no main items for data account id %d", accountId));
			} else {
				LOG.debug(String.format("Found %d main items for data account id %d", mainItems.size(), accountId));
			}

			for (String mainItemId : mainItems) {
				List<String> iapIdsForItem = saleItemRepo.getIAPIdsForItem(activeAccount.getId(), mainItemId);
				if (iapIdsForItem == null) {
					LOG.debug(String.format("\t Got no iapItems for mainItem with ID %s", mainItemId));
				} else {
					LOG.debug(String.format("\t Got %d iapItems for mainItem with ID %s", iapIdsForItem.size(), mainItemId));
				}
			}
		}
	}
}
