package io.reflection.salesdatagather.services;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.taskqueue.Taskqueue;
import com.google.api.services.taskqueue.Taskqueue.Taskqueues.Get;
import com.google.api.services.taskqueue.TaskqueueRequest;
import com.google.api.services.taskqueue.TaskqueueRequestInitializer;
import com.google.api.services.taskqueue.model.Task;
import com.google.api.services.taskqueue.model.TaskQueue;
import com.google.api.services.taskqueue.model.TaskQueue.Stats;
import com.google.api.services.taskqueue.model.Tasks;

import io.reflection.salesdatagather.AppConfig;
import io.reflection.salesdatagather.model.nondb.LeasedTask;

@Service
public class TaskService {
	private transient static final Logger LOG = LoggerFactory.getLogger(TaskService.class.getName());

	private boolean hasServiceBeenInitialised = false;

	@Autowired
	private GoogleAuthService googleAuthService;

	@Autowired
	private AppConfig appConfig;

	private Taskqueue taskQueueApi = null;

	public synchronized boolean initialiseService() {
		if (!hasServiceBeenInitialised) {
			Credential credential = googleAuthService.authorise();
			if (credential == null) return false;

			LOG.debug("Initialising task queue api");
			String projectName = appConfig.getGoogleProjectName();

			taskQueueApi = new Taskqueue.Builder(googleAuthService.getHttpTransport(), googleAuthService.getJsonFactory(), credential)
					.setApplicationName(projectName).setTaskqueueRequestInitializer(new TaskqueueRequestInitializer() {
						@Override
						public void initializeTaskqueueRequest(TaskqueueRequest<?> request) {
							request.setPrettyPrint(Boolean.TRUE);
						}
					}).build();

			try {
				Get request = taskQueueApi.taskqueues().get(projectName, appConfig.getTasksQueueName());
				request.setGetStats(Boolean.TRUE);
				TaskQueue taskQueue = request.execute();
				Stats s = taskQueue.getStats();
				LOG.debug(String.format("Stats for queue: %s", s));

				hasServiceBeenInitialised = true;
				return true;
			} catch (IOException e) {
				LOG.error("Exception while trying to access the queue", e);
				return false;
			}
		}

		return false;
	}

	public LeasedTask leaseTask() {
		if (!hasServiceBeenInitialised) {
			if (!initialiseService()) return null;
		}

		Tasks tasks;
		String tasksQueueName = appConfig.getTasksQueueName();
		try {
			tasks = taskQueueApi.tasks().lease(appConfig.getGoogleProjectName(), tasksQueueName, 1, appConfig.getTaskLeaseTimeSeconds()).execute();
		} catch (IOException e) {
			LOG.error(String.format("Could not get a list of tasks from the %s queue", tasksQueueName), e);
			return null;
		}

		if (tasks == null || tasks.getItems() == null) {
			LOG.debug("Tasks request returned nothing");
			return null;
		}

		LOG.debug(String.format("Got %d tasks in the queue", tasks.getItems().size()));

		if (tasks == null || tasks.getItems() == null || tasks.getItems().size() == 0) return null;

		Task googleTask = tasks.getItems().get(0);

		Map<String, String> paramMap = getParametersFromPayload(googleTask.getPayloadBase64());

		LeasedTask task = new LeasedTask(paramMap, googleTask);

		return task;
	}

	private Map<String, String> getParametersFromPayload(String payloadBase64) {
		final Map<String, String> mappedParams = new HashMap<String, String>();

		final String parameters = payloadBase64;
		final String decodedParameters = new String(Base64.decodeBase64(parameters.getBytes()));

		if (decodedParameters != null) {
			final String[] parts = decodedParameters.split("&");

			String[] subParts = null;
			for (final String part : parts) {
				subParts = part.split("=");

				if (subParts.length == 1) {
					continue;
				}

				mappedParams.put(subParts[0], subParts[1]);
			}
		}

		return mappedParams;
	}

	public void deleteTask(LeasedTask leasedTask) {
		try {
			taskQueueApi.tasks().delete(appConfig.getGoogleProjectName(), appConfig.getTasksQueueName(), leasedTask.getGoogleLeasedTask().getId()).execute();
		} catch (IOException e) {
			LOG.error("Could not delete a task from the task service with task ID: " + leasedTask.getGoogleLeasedTask().getId(), e);
		}
	}
}
