package com.optum.ap.services.rest.common.config.db.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class DBDefinition {
	private Map<String, String> datasource;

	public Map<String, String> getDatasource() {
		return datasource;
	}
	public void setDatasource(Map<String, String> dataSource) {
		this.datasource = dataSource;
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
