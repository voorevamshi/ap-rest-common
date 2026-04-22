package com.optum.ap.task.config;


import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "ap.tasks")
public class ApTaskConfig {
	
	private Map<String, ApTaskDetail> taskList;
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ApTaskConfig [taskList=");
		builder.append(taskList);
		builder.append("]");
		return builder.toString();
	}
	
	public Map<String, ApTaskDetail> getTaskList() {
		return taskList;
	}
	
	public void setTaskList(Map<String, ApTaskDetail> taskList) {
		this.taskList = taskList;
	}

}
