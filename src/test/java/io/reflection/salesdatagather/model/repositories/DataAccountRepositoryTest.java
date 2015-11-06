package io.reflection.salesdatagather.model.repositories;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.reflection.salesdatagather.BaseSpringTestClass;
import io.reflection.salesdatagather.model.DataAccount;

public class DataAccountRepositoryTest extends BaseSpringTestClass {
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
		assertTrue(dataAccounts.size() > 0);
	}
}
