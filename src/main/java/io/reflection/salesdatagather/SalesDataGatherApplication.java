package io.reflection.salesdatagather;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class SalesDataGatherApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(SalesDataGatherApplication.class, args);
		AppConfig appConfig = context.getBean(AppConfig.class);
		System.out.println("=== Version: " + appConfig.getVersion() + ", Profile: " + appConfig.getProfile());
	}
}
