package io.reflection.salesdatagather.jobs;

import io.reflection.salesdatagather.tasks.GatherTask;
import io.reflection.salesdatagather.tasks.TaskService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CheckTaskQueueJob {
	private transient static final Logger LOG = LoggerFactory.getLogger(CheckTaskQueueJob.class.getName());

	@Autowired
	private TaskService taskService;

	// Scheduled to run with a fixed delay (in milliseconds) between the end of a run and the start of the next with initial delay in milliseconds
	@Scheduled(fixedDelay = (5000 * 5), initialDelay = 5000)
	public void checkTaskQueue() {
		LOG.info("Checking Task Queue for more tasks");
		GatherTask task = taskService.leaseTask();

		if(task==null) {
			LOG.info("No more tasks to execute. Going ");
			return; //No tasks for us to process, we can go back to sleep
		}
	}
}
