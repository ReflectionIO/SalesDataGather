package io.reflection.salesdatagather.jobs;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CheckTaskQueueJob {

	// Scheduled to run with a fixed delay (in milliseconds) between the end of a run and the start of the next with initial delay in milliseconds
	@Scheduled(fixedDelay = (1000 * 10), initialDelay = 5000)
	public void checkTaskQueue() {
		// TODO checkTaskQueue
	}
}
