package io.reflection.salesdatagather.tasks;

import io.reflection.salesdatagather.AppConfig;
import io.reflection.salesdatagather.model.DataAccount;
import io.reflection.salesdatagather.model.repositories.DataAccountRepository;
import io.reflection.salesdatagather.services.GatherTaskService;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.taskqueue.Taskqueue;
import com.google.api.services.taskqueue.Taskqueue.Taskqueues.Get;
import com.google.api.services.taskqueue.TaskqueueRequest;
import com.google.api.services.taskqueue.TaskqueueRequestInitializer;
import com.google.api.services.taskqueue.TaskqueueScopes;
import com.google.api.services.taskqueue.model.Task;
import com.google.api.services.taskqueue.model.TaskQueue;
import com.google.api.services.taskqueue.model.TaskQueue.Stats;
import com.google.api.services.taskqueue.model.Tasks;

@Service
public class TaskService {
	private transient static final Logger	LOG													= LoggerFactory.getLogger(TaskService.class.getName());

	private HttpTransport									httpTransport								= null;

	private boolean												IS_TASK_SERVICE_INITIALISED	= false;

	@Autowired
	private JsonFactory										jsonFactory;

	@Autowired
	private AppConfig											appConfig;

	@Autowired
	private DataAccountRepository dataAccountRepo;

	@Autowired
	private GatherTaskService gatherTaskService;

	private Taskqueue											taskQueueApi								= null;

	public synchronized boolean initialiseService() {
		if (!IS_TASK_SERVICE_INITIALISED) {
			Credential credential = authorise();
			if (credential == null) return false;

			LOG.debug("Initialising task queue api");
			String projectName = appConfig.getGoogleProjectName();

			taskQueueApi = new Taskqueue.Builder(httpTransport, jsonFactory, credential)
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

				return true;
			} catch (IOException e) {
				LOG.error("Exception while trying to access the queue", e);
				return false;
			}
		}

		return false;
	}

	public Credential authorise() {
		try {
			if (httpTransport == null) {
				httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			}

			GoogleCredential cred = new GoogleCredential.Builder()
			.setTransport(httpTransport)
			.setJsonFactory(jsonFactory)
			.setServiceAccountId(appConfig.getGoogleAuthEmail())
			.setServiceAccountPrivateKeyFromP12File(new File(appConfig.getGoogleAuthCertificatePath()))
			.setServiceAccountScopes(Collections.singleton(TaskqueueScopes.TASKQUEUE))
			.build();
			return cred;
		} catch (GeneralSecurityException | IOException e) {
			if (LOG.isErrorEnabled()) {
				LOG.error("Could not authorise the app with Google TaskQueue service.", e);
			}
		}
		return null;
	}

	public GatherTask leaseTask() {
		if (!IS_TASK_SERVICE_INITIALISED) {
			if (!initialiseService()) return null;
		}

		Tasks tasks;
		String tasksQueueName = appConfig.getTasksQueueName();
		try {
			tasks = taskQueueApi.tasks().lease(appConfig.getGoogleProjectName(), tasksQueueName, 1, appConfig.getTaskLeaseTime()).execute();
		} catch (IOException e) {
			LOG.error(String.format("Could not get a list of tasks from the %s queue", tasksQueueName), e);
			return null;
		}

		LOG.debug(String.format("Got %d tasks in the queue", tasks.getItems().size()));

		if(tasks==null || tasks.getItems()==null || tasks.getItems().size()==0) return null;

		Task leasedTask = tasks.getItems().get(0);

		Map<String, String> paramMap = getParametersFromPayload(leasedTask.getPayloadBase64());

		GatherTask gatherTask = createGatherTaskFromParams(paramMap);
		gatherTask.setLeasedTask(leasedTask);
		return gatherTask;
	}

	private GatherTask createGatherTaskFromParams(Map<String, String> paramMap) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

			DataAccount dataAccount = dataAccountRepo.getDataAccountById(Integer.valueOf(paramMap.get("dataAccountId")));

			Date gatherFrom = sdf.parse(paramMap.get("gatherFrom"));
			Date gatherTo = sdf.parse(paramMap.get("gatherTo"));

			String mainItemId = paramMap.get("mainItemId");
			String iapItemIds = paramMap.get("iapItemIds");

			String countryCode = paramMap.get("countryCode");

			GatherTask task = new GatherTask(dataAccount, gatherFrom, gatherTo, iapItemIds, mainItemId, countryCode, gatherTaskService);

			return task;
		}catch(NumberFormatException e) {
			LOG.error("Number format exception when trying to create GatherTask from params: "+paramMap.toString(), e);
		} catch (ParseException e) {
			LOG.error("Date parse exception when trying to create GatherTask from params: "+paramMap.toString(), e);
		}
		return null;
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
				mappedParams.put(subParts[0], subParts[1]);
			}
		}

		return mappedParams;
	}
}
