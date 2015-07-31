package io.reflection.salesdatagather;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SalesDataGatherApplication {
	private transient static final Logger LOG = LoggerFactory.getLogger(SalesDataGatherApplication.class.getName());

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(SalesDataGatherApplication.class, args);

		AppConfig appConfig = context.getBean(AppConfig.class);
		if (LOG.isInfoEnabled()) {
			LOG.info(String.format("\n\nSales Data Gather App started. Verions: %s under profile: %s\n"
					+ "=================================================================================\n\n", appConfig.getVersion(), appConfig.getProfile()));
		}

		Path tempDownloadDir = Paths.get(appConfig.getTempDownloadDir());
		File tempDir = tempDownloadDir.toFile();
		if (!tempDir.exists()) {
			if (!tempDir.mkdirs()) {
				LOG.error("The temporary download directory does not exist and it could not be created. Exiting app.");
				tempDir.deleteOnExit();
				System.exit(0);
			}

			LOG.info("The temporary download directory did not exist. It has now been created: " + tempDownloadDir);
		}

		appConfig.logConfig();

		// context.getBean(NicksHelper.class).run();
	}
}
