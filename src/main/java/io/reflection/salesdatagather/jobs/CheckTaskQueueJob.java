package io.reflection.salesdatagather.jobs;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.reflection.salesdatagather.AppConfig;
import io.reflection.salesdatagather.model.nondb.LeasedTask;
import io.reflection.salesdatagather.services.GatherTaskService;
import io.reflection.salesdatagather.services.TaskService;

@Component
@Profile({ "dev", "prod" })
public class CheckTaskQueueJob {
	private transient static final Logger LOG = LoggerFactory.getLogger(CheckTaskQueueJob.class.getName());

	@Autowired
	private TaskService taskService;

	@Autowired
	private GatherTaskService gatherTaskService;

	@Autowired
	private AppConfig appConfig;

	// Scheduled to run with a fixed delay (in milliseconds) between the end of a
	// run and the start of the next with initial delay in milliseconds
	@Scheduled(fixedDelay = (1000 * 60 * 1), initialDelay = 5000)
	public void checkTaskQueue() {
		LOG.info("Checking Task Queue for more tasks");

		List<LeasedTask> tasks = null;
		while ((tasks = taskService.leaseTask(appConfig.getTaskBatchSize())) != null) {
			for (LeasedTask task : tasks) {
				gatherTaskService.scheduleTaskForExecution(task);
			}
		}

		LOG.info("No more tasks to execute. Going ");
	}
}
