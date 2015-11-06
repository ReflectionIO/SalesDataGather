package io.reflection.salesdatagather.model.nondb;

import java.util.Map;

import com.google.api.services.taskqueue.model.Task;

public class LeasedTask {
	private Task				googleLeasedTask;
	private Map<String, String>	paramMap;

	public LeasedTask(Map<String, String> paramMap, Task googleLeasedTask) {
		this.paramMap = paramMap;
		this.googleLeasedTask = googleLeasedTask;
	}

	public Task getGoogleLeasedTask() {
		return googleLeasedTask;
	}

	public void setGoogleLeasedTask(Task googleLeasedTask) {
		this.googleLeasedTask = googleLeasedTask;
	}

	public Map<String, String> getParamMap() {
		return paramMap;
	}

	public void setParamMap(Map<String, String> paramMap) {
		this.paramMap = paramMap;
	}
}
