package io.reflection.salesdatagather;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:/application.properties")
public class AppConfig {

	@Value("${app.version}")
	private String	version;

	@Value("${profile}")
	private String	profile;

	public String getVersion() {
		return version;
	}

	public String getProfile() {
		return profile;
	}
}
