package io.reflection.salesdatagather.tasks;

import static org.junit.Assert.*;
import io.reflection.salesdatagather.SalesDataGatherApplication;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.google.api.client.auth.oauth2.Credential;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SalesDataGatherApplication.class)
@WebAppConfiguration
public class TaskServiceTests {
	@Autowired
	private TaskService	taskService;

	@Test
	@Ignore
	public void googleTaskQueueAuthorisation() {
		Credential credentials = taskService.authorise();
		assertNotNull("Could not autorize the task queue service ", credentials);
	}

	@Test
	@Ignore
	public void googleTaskQueueInitialisation() {
		boolean serviceInitialised = taskService.initialiseService();
		assertTrue("Could not initialise the task queue service", serviceInitialised);
	}

	@Test
	@Ignore
	public void leaseTasksTest() {
		assertNotNull("Could not lease a task from the task service", taskService.leaseTask());
	}

	@Test
	public void getParametersFromPayloadTest() {
		String payload = "";
	}
}
