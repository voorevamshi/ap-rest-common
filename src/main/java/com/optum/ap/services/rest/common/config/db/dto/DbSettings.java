package com.optum.ap.services.rest.common.config.db.dto;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

@Component
@ConfigurationProperties("ap-dbsources")
public class DbSettings {

	private String dataBaseAttrs;

	private Map<String, DBDefinition> databases = new HashMap<>();

	public Map<String, DBDefinition> getDatabases() {
		return databases;
	}
	public void setDatabases(Map<String, DBDefinition> databases) {
		this.databases = databases;
	}
	public String getDataBaseAttrs() {
		return dataBaseAttrs;
	}
	public void setDataBaseAttrs(String dataBaseAttrs) {
		this.dataBaseAttrs = dataBaseAttrs;
	}
	public String toString() {
		ObjectMapper json = new ObjectMapper();
		String[] ignoreFieldList = { "password", "pwd", "secret" };
		FilterProvider filterFields = new SimpleFilterProvider().addFilter("ignoreFields",
				SimpleBeanPropertyFilter.serializeAllExcept(ignoreFieldList));
		json.addMixIn(this.getClass(), FilterHelper.class);
		try {
			return json.writer(filterFields).writeValueAsString(this);
		} catch (JsonProcessingException e) {
			return "{\"error\": \"" + e.getMessage() + "\"}";
		}
	}
	@JsonFilter("ignoreFields")
	private class FilterHelper {
	}
}
