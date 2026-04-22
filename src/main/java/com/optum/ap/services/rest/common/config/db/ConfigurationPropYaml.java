package com.optum.ap.services.rest.common.config.db;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties
public class ConfigurationPropYaml {

	private List<String> databaseList = new ArrayList<String>();

	public List<String> getDatabaseList() {
		return databaseList;
	}

	public void setDatabaseList(List<String> databaseList) {
		this.databaseList = databaseList;
	}

}
