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

			googleCloudStorage = new Storage.Builder(googleAuthService.getHttpTransport(), googleAuthService.getJsonFactory(), credential).build();

			hasServiceBeenInitialised = true;
			return true;
		}
		return false;
	}

	public String uploadFile(File file, String generateFileName) {
		try {
			Insert request = googleCloudStorage.objects().insert(appConfig.getGoogleStorageBucketName(), null, new InputStreamContent("application/octet-stream", new FileInputStream(file))).setName(generateFileName);
			request.execute();
		} catch (IOException e) {
			LOG.error("Error uploading file to Google", e);
		}

		return null;
	}

	public Path downloadFile(Path downloadDir, String downloadUrl) {
		Path outputFile = downloadDir.resolve("temp_download_"+System.currentTimeMillis()+"_"+((int)Math.random()*1000));

		try (FileOutputStream outputStream = new FileOutputStream(outputFile.toFile())){
			Get getRequest = googleCloudStorage.objects().get(appConfig.getGoogleStorageBucketName(), downloadUrl);
			getRequest.getMediaHttpDownloader().setDirectDownloadEnabled(true);
			getRequest.executeAndDownloadTo(outputStream);
		} catch (IOException e) {
			LOG.error("Error downloading file from Google. URL: "+downloadUrl, e);
		}

		return outputFile;
	}
}
