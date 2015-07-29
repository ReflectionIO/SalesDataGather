package io.reflection.salesdatagather.tasks;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.api.client.auth.oauth2.Credential;

import io.reflection.salesdatagather.BaseSpringTestClass;
import io.reflection.salesdatagather.services.GoogleAuthService;
import io.reflection.salesdatagather.services.TaskService;

public class TaskServiceTests extends BaseSpringTestClass{
	@Autowired
	private GoogleAuthService authService;

	@Autowired
	private TaskService	taskService;

	@Test
	@Ignore
	public void googleTaskQueueAuthorisation() {
		Credential credentials = authService.authorise();
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
