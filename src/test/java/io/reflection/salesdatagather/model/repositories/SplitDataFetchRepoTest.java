package io.reflection.salesdatagather.model.repositories;

import static org.mockito.Mockito.*;

import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

public class SplitDataFetchRepoTest {

	@Test
	public void testCreateSplitDataFetch() {
		//setup
		JdbcTemplate jdbc = mock(JdbcTemplate.class);

		SplitDataFetchRepo repo = new SplitDataFetchRepo(jdbc);
		//		SplitDataFetch record = repo.createSplitDataFetch(1, new Date(), "gb", "123", SplitDataFetchStatus.GATHERING);
		//
		//		verify(jdbc, times(1)).update(anyString(), (PreparedStatementSetter) any());
		//
		//		assertNotNull("No SplitDataFetch record returned on calling SplitDataFetchRepo.createSplitDataFetch", record);
	}
}
