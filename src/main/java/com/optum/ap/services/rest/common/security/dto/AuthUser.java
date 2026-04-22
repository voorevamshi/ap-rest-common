package com.optum.ap.services.rest.common.security.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class AuthUser {

	private String name;

	private String password;

	private List<String> roles;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
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
