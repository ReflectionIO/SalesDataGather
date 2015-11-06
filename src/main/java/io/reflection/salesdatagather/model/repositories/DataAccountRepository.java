package io.reflection.salesdatagather.model.repositories;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import io.reflection.salesdatagather.model.DataAccount;

@Repository
public class DataAccountRepository {
	private transient static final Logger LOG = LoggerFactory.getLogger(DataAccountRepository.class.getName());

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public DataAccount getDataAccountById(Integer id) {
		try {
			DataAccount dataAccount = jdbcTemplate.queryForObject(
					"select *, convert(aes_decrypt(`password`,UNHEX('F54E1A22395D42B8900261E14A750D98')), CHAR(1000)) AS `clearPassword` from dataaccount where id=?",
					new Object[] { id },
					new BeanPropertyRowMapper<DataAccount>(DataAccount.class));

			return dataAccount;
		} catch (Exception e) {
			LOG.debug("Could not load data account by id: " + id + ". Cause: " + e.getMessage());
		}
		return null;
	}

	public List<DataAccount> getActiveDataAccounts() {
		try {
			List<DataAccount> dataAccounts = jdbcTemplate.query(
					"select *, convert(aes_decrypt(`password`,UNHEX('F54E1A22395D42B8900261E14A750D98')), CHAR(1000)) AS `clearPassword` from dataaccount where deleted='n' and active='y'",
					new BeanPropertyRowMapper<DataAccount>(DataAccount.class));

			return dataAccounts;
		} catch (Exception e) {
			LOG.debug("Could not load active data accounts. Cause: " + e.getMessage());
		}
		return null;
	}
}
