package io.reflection.salesdatagather.model.repositories;

import static org.junit.Assert.*;
import io.reflection.salesdatagather.SalesDataGatherApplication;
import io.reflection.salesdatagather.model.DataAccount;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SalesDataGatherApplication.class)
@WebAppConfiguration
public class DataAccountRepositoryTest {
	@Autowired
	private DataAccountRepository repository;

	@Test
	public void getDataAccountByIdTest() {
		DataAccount dataAccount = repository.getDataAccountById(1);
		assertNotNull(dataAccount);
		assertEquals(new Integer(1), dataAccount.getId());
		assertEquals("SPACEHOPPERSTUDIOS", dataAccount.getUsername());
	}

	@Test
	public void getDataActiveAccountsTest() {
		List<DataAccount> dataAccounts = repository.getActiveDataAccounts();
		assertNotNull(dataAccounts);
		assertTrue(dataAccounts.size()>0);
	}
}
