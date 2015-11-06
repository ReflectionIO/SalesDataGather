package io.reflection.salesdatagather;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import io.reflection.salesdatagather.model.nondb.GatherTask;
import io.reflection.salesdatagather.model.nondb.LeasedTask;
import io.reflection.salesdatagather.services.GatherTaskService;
import io.reflection.salesdatagather.services.TaskService;

@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test")
@SpringApplicationConfiguration(classes = SalesDataGatherApplication.class)
@WebAppConfiguration
public class SalesDataGatherApplicationTests {
	private transient static final Logger LOG = LoggerFactory.getLogger(SalesDataGatherApplicationTests.class.getName());

	// @Autowired
	// private JdbcTemplate jdbc;
	//
	// @Autowired
	// private JdbcTestUtils jdbcTestUtils;
	//
	// @Autowired
	// private ScriptUtils sqlUtils;

	@Autowired
	private GatherTaskService gatherTaskService;

	@Autowired
	private TaskService taskService;

	@Before
	public void setup() {

	}

	@Test
	public void fullSplitDataGatherAndProcessTest() {
		GatherTask gatherTaskToTest = null;
		while ((gatherTaskToTest = getGatherTaskToTest()) != null) {
			gatherTaskService.executeGather(gatherTaskToTest);
		}
	}

	private GatherTask getGatherTaskToTest() {
		List<LeasedTask> leasedTasks = taskService.leaseTask(1);
		if (leasedTasks != null && leasedTasks.size() > 0) {
			LeasedTask task = leasedTasks.get(0);
			GatherTask gatherTask = gatherTaskService.createGatherTaskFromParams(task.getParamMap());
			gatherTask.setLeasedTask(task);

			LOG.info("Testing gather task: " + gatherTask.toString());

			return gatherTask;
		}

		return null;
	}
}
