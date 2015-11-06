package io.reflection.salesdatagather.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.Storage.Objects.Get;
import com.google.api.services.storage.Storage.Objects.Insert;
import com.google.api.services.storage.model.StorageObject;

import io.reflection.salesdatagather.AppConfig;

@Service
public class CloudStorageService {
	private transient static final Logger LOG = LoggerFactory.getLogger(TaskService.class.getName());

	private boolean hasServiceBeenInitialised = false;

	@Autowired
	private AppConfig appConfig;

	@Autowired
	private GoogleAuthService googleAuthService;

	private Storage googleCloudStorage;

	public synchronized boolean initialiseService() {
		if (!hasServiceBeenInitialised) {
			Credential credential = googleAuthService.authorise();
			if (credential == null) return false;

			googleCloudStorage = new Storage.Builder(googleAuthService.getHttpTransport(), googleAuthService.getJsonFactory(), credential).setApplicationName(appConfig.getGoogleProjectName()).build();

			hasServiceBeenInitialised = true;
			return true;
		}
		return hasServiceBeenInitialised;
	}

	public String uploadFile(File file, String generateFileName) {
		try {
			if (!initialiseService()) {
				LOG.error("Could not upload files as the Google Cloud storage service has not been initialised.");
			}

			Insert request = googleCloudStorage.objects().insert(appConfig.getGoogleStorageBucketName(), null, new InputStreamContent("application/octet-stream", new FileInputStream(file)))
					.setName(generateFileName);
			StorageObject result = request.execute();
			String url = result.getBucket() + "/" + result.getName();
			return url;
		} catch (IOException e) {
			LOG.error("Error uploading file to Google", e);
		}

		return null;
	}

	public Path downloadFile(Path downloadDir, String downloadUrl) {
		if (downloadDir == null || downloadUrl == null || downloadUrl.trim().length() == 0) return null;

		Path outputFile = downloadDir.resolve("temp_download_" + System.currentTimeMillis() + "_" + ((int) Math.random() * 1000));

		if (!initialiseService()) {
			LOG.error("Could not download files as the Google Cloud storage service has not been initialised.");
		}
		try (FileOutputStream outputStream = new FileOutputStream(outputFile.toFile())) {
			int bucketNameEndIndex = downloadUrl.indexOf('/', 0);

			String bucketName = downloadUrl.substring(0, bucketNameEndIndex);
			String objectName = downloadUrl.substring(bucketNameEndIndex + 1);

			Get getRequest = googleCloudStorage.objects().get(bucketName, objectName);
			getRequest.getMediaHttpDownloader().setDirectDownloadEnabled(true);
			getRequest.executeMediaAndDownloadTo(outputStream);
		} catch (IOException e) {
			LOG.error("Error downloading file from Google. URL: " + downloadUrl, e);
		}

		return outputFile;
	}
}
