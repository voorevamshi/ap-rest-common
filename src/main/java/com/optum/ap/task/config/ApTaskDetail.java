package com.optum.ap.task.config;


import java.util.Map;

public class ApTaskDetail {
	
	private String cronExp;
	private boolean jobEnabled;
	private Map<String, String> taskProps;
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ApTaskDetail [cronExp=");
		builder.append(cronExp);
		builder.append(", jobEnabled=");
		builder.append(jobEnabled);
		builder.append(", taskProps=");
		builder.append(taskProps);
		builder.append("]");
		return builder.toString();
	}
	
	public String getCronExp() {
		return cronExp;
	}
	
	public void setCronExp(String cronExp) {
		this.cronExp = cronExp;
	}
	
	public boolean isJobEnabled() {
		return jobEnabled;
	}
	
	public void setJobEnabled(boolean jobEnabled) {
		this.jobEnabled = jobEnabled;
	}
	
	public Map<String, String> getTaskProps() {
		return taskProps;
	}
	
	public void setTaskProps(Map<String, String> taskProps) {
		this.taskProps = taskProps;
	}
	
}
