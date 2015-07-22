package io.reflection.salesdatagather;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SalesDataGatherApplication {
	private transient static final Logger	LOG	= LoggerFactory.getLogger(SalesDataGatherApplication.class.getName());

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(SalesDataGatherApplication.class, args);

		AppConfig appConfig = context.getBean(AppConfig.class);
		if (LOG.isInfoEnabled()) {
			LOG.info(String.format("\n\nSales Data Gather App started. Verions: %s under profile: %s\n"
					+ "=================================================================================\n\n", appConfig.getVersion(), appConfig.getProfile()));
		}

		context.getBean(NicksHelper.class).run();
	}
}
