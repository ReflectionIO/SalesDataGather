package io.reflection.salesdatagather.services;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.storage.StorageScopes;
import com.google.api.services.taskqueue.TaskqueueScopes;

import io.reflection.salesdatagather.AppConfig;

@Service
public class GoogleAuthService {
	private transient static final Logger LOG = LoggerFactory.getLogger(TaskService.class.getName());

	private HttpTransport httpTransport = null;

	@Autowired
	private JsonFactory jsonFactory;

	@Autowired
	private AppConfig appConfig;

	private GoogleCredential cred;

	public Credential authorise() {
		try {
			if (httpTransport == null) {
				httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			}

			if (cred == null) {
				cred = new GoogleCredential.Builder()
						.setTransport(httpTransport)
						.setJsonFactory(jsonFactory)
						.setServiceAccountId(appConfig.getGoogleAuthEmail())
						.setServiceAccountPrivateKeyFromP12File(new File(appConfig.getGoogleAuthCertificatePath()))
						.setServiceAccountScopes(Arrays.asList(TaskqueueScopes.TASKQUEUE, StorageScopes.DEVSTORAGE_READ_WRITE))
						.build();
			}
		} catch (GeneralSecurityException | IOException e) {
			if (LOG.isErrorEnabled()) {
				LOG.error("Could not authorise the app with Google Cloud service.", e);
			}
		}
		return cred;
	}

	public HttpTransport getHttpTransport() {
		return httpTransport;
	}

	public JsonFactory getJsonFactory() {
		return jsonFactory;
	}
}
