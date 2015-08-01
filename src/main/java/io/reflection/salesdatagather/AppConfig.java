package io.reflection.salesdatagather;

import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

@Configuration
@PropertySource("classpath:/application.properties")
public class AppConfig {
	private transient static final Logger LOG = LoggerFactory.getLogger(AppConfig.class.getName());

	@Value("${app.version}")
	private String version;

	@Value("${app.build.timestamp}")
	private String buildTimestamp;

	@Value("${profile}")
	private String profile;

	@Value("${google.project.name}")
	private String googleProjectName;

	@Value("${google.auth.p12key.path}")
	private String googleAuthCertPath;

	@Value("${google.auth.email}")
	private String googleAuthEmail;

	@Value("${google.tasks.queue.name}")
	private String tasksQueueName;

	@Value("${temp.download.dir}")
	private String tempDownloadDirectory;

	@Value("${temp.download.prefix}")
	private String tempDownloadPrefix;

	@Value("${google.tasks.leaseTimeSeconds}")
	private Integer taskLeaseTimeSeconds;

	@Value("${google.tasks.batchSize}")
	private Integer taskBatchSize;

	@Value("${google.storage.filePrefix}")
	private String googleStorageFilePrefix;

	@Value("${google.storage.bucketName}")
	private String googleStorageBucketName;

	@Value("${executor.queue.capacity}")
	private Integer executorQueueCapacity;

	@Value("${executor.pool.coreSize}")
	private Integer executorCorePoolSize;

	@Value("${executor.pool.maxSize}")
	private Integer executorMaxPoolSize;

	@Value("${activeCountryCodes}")
	private String activeCountryCodes;

	public String getVersion() {
		return version;
	}

	public String getBuildTimestamp() {
		return buildTimestamp;
	}

	public String getProfile() {
		return profile;
	}

	@Bean
	public JsonFactory getJsonFactory() {
		return JacksonFactory.getDefaultInstance();
	}

	@Bean
	public ThreadPoolTaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();

		/*
		 * This make sure then when adding a task to be executed, once the queue is full, the current thread itself runs the task.
		 * This means that the task adding mechanism slows down and becomes the last thread of the pool. When it is free again, it
		 * can begin loading back into the queue
		 */
		pool.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

		/*
		 * At a time just allow 6 tasks in the queue. This means that it will take double the time of a gather to clear the queue.
		 * We don't want to keep too many tasks to be borrowed/leased. We will lease as we execute.
		 */
		LOG.info(String.format("Starting a pool of executors with capacity: %d, core size:%d, max size: %d", executorQueueCapacity, executorCorePoolSize, executorMaxPoolSize));
		pool.setQueueCapacity(executorQueueCapacity);
		pool.setCorePoolSize(executorCorePoolSize); // Keep just one thread ready for processing tasks. This will go up to maxPoolSize thread as more tasks are added
		pool.setMaxPoolSize(executorMaxPoolSize); // only 6 tasks can be run at a time (the schedulling thread becomes the 7th leaving 1 spare for the OS)
		pool.setWaitForTasksToCompleteOnShutdown(true);

		return pool;
	}

	public String getGoogleAuthCertificatePath() {
		return googleAuthCertPath;
	}

	public String getGoogleProjectName() {
		return googleProjectName;
	}

	public String getGoogleAuthEmail() {
		return googleAuthEmail;
	}

	public String getTasksQueueName() {
		return tasksQueueName;
	}

	public String getDownloadDirPrefix() {
		return tempDownloadPrefix;
	}

	public String getTempDownloadDir() {
		return tempDownloadDirectory;
	}

	public Integer getTaskLeaseTimeSeconds() {
		return taskLeaseTimeSeconds;
	}

	public String getGoogleStorageFilePrefix() {
		return googleStorageFilePrefix;
	}

	public String getGoogleStorageBucketName() {
		return googleStorageBucketName;
	}

	public String getActiveCountryCodes() {
		return activeCountryCodes;
	}

	public Integer getTaskBatchSize() {
		return taskBatchSize;
	}

	public void logConfig() {
		LOG.info(String.format("\n"
				+ "============================================\n"
				+ "Configuration we are running under:\n"
				+ "\tVersion:\t\t\t%s\n"
				+ "\tBuild Timestamp:\t\t%s\n"
				+ "\tProfile:\t\t\t%s\n"
				+ "\n"
				+ "\tgoogleProjectName:\t\t%s\n"
				+ "\tgoogleAuthCertPath:\t\t%s\n"
				+ "\tgoogleAuthEmail:\t\t%s\n"
				+ "\n"
				+ "\tTask processing batch size:\t%s\n"
				+ "\tTasks Queue Name:\t\t%s\n"
				+ "\tTasks lease time in seconds:\t%s\n"
				+ "\n"
				+ "\tDownload Directory:\t\t%s\n"
				+ "\tDownload Prefix:\t\t%s\n"
				+ "\n"
				+ "\tStorage File Prefix:\t\t%s\n"
				+ "\tStorage Bucket Name:\t\t%s\n"
				+ "\n"
				+ "\tExecutor Queue Capacity:\t%s\n"
				+ "\tExecutor Core Pool Size:\t%s\n"
				+ "\tExecutor Max Pool Size:\t\t%s\n"
				+ "\n"
				+ "\tActive Country Codes:\t\t%s\n"
				+ "============================================\n",
				version, profile, buildTimestamp,
				googleProjectName, googleAuthCertPath, googleAuthEmail,
				taskBatchSize, tasksQueueName, taskLeaseTimeSeconds,
				tempDownloadDirectory, tempDownloadPrefix,
				googleStorageFilePrefix, googleStorageBucketName,
				executorQueueCapacity, executorCorePoolSize, executorMaxPoolSize, activeCountryCodes));
	}
}
