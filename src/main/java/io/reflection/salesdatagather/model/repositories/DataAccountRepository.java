package io.reflection.salesdatagather.model.repositories;

import io.reflection.salesdatagather.model.DataAccount;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DataAccountRepository {

	@Autowired
	private JdbcTemplate	jdbcTemplate;

	public DataAccount getDataAccountById(Integer id) {
		DataAccount dataAccount = jdbcTemplate.queryForObject(
				"select *, convert(aes_decrypt(`password`,UNHEX('F54E1A22395D42B8900261E14A750D98')), CHAR(1000)) AS `clearPassword` from dataaccount where id=?",
				new Object[] { id },
				new BeanPropertyRowMapper<DataAccount>(DataAccount.class));

		return dataAccount;
	}

	public List<DataAccount> getActiveDataAccounts() {
		List<DataAccount> dataAccounts = jdbcTemplate.query(
				"select *, convert(aes_decrypt(`password`,UNHEX('F54E1A22395D42B8900261E14A750D98')), CHAR(1000)) AS `clearPassword` from dataaccount where deleted='n' and active='y'",
				new BeanPropertyRowMapper<DataAccount>(DataAccount.class));

		return dataAccounts;
	}
}
