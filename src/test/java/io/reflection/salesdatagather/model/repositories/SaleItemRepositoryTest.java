package io.reflection.salesdatagather.model.repositories;

import io.reflection.salesdatagather.SalesDataGatherApplication;
import io.reflection.salesdatagather.model.DataAccount;
import io.reflection.salesdatagather.model.nondb.GatherTask;
import io.reflection.salesdatagather.services.GatherTaskService;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SalesDataGatherApplication.class)
@WebAppConfiguration
public class SaleItemRepositoryTest {
	private transient static final Logger	LOG	= LoggerFactory.getLogger(SaleItemRepositoryTest.class.getName());

	@Autowired
	private DataAccountRepository					dataAccountRepo;

	@Autowired
	private SaleItemRepository						saleItemRepo;

	@Autowired
	private GatherTaskService							gatherTaskService;

	@Test
	public void test() {
		int[] accountIds = new int[] { 332, 346, 333, 369, 347, 264, 330, 338, 321, 256, 360, 259, 348, 340, 329, 331, 359, 334, 366, 368, 363, 355 };
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

			for (String mainItemId : mainItems) {
				List<String> iapIdsForItem = saleItemRepo.getIAPIdsForItem(activeAccount.getId(), mainItemId);

				StringBuffer itemIds = new StringBuffer(mainItemId);
				for (String iapId : iapIdsForItem) {
					itemIds.append(',').append(iapId);
				}

				try {
					GatherTask task = new GatherTask(activeAccount, from, to, itemIds.toString(), mainItemId, "gb", gatherTaskService);
					task.run();
				} catch (Exception e) {
					LOG.error("Error occured while trying to process sales for data account: "+accountId+" mainItemId: "+mainItemId+", itemIds: "+itemIds.toString());
				}
			}
		}
	}
}
