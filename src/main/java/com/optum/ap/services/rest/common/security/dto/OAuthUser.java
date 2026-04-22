package com.optum.ap.services.rest.common.security.dto;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class OAuthUser {

	private String name;

	private String secret;
	
	private String granttypes;

	private String scopes;
	
	private String authorities;
	
	private Integer duration;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getGranttypes() {
		return granttypes;
	}

	public void setGranttypes(String granttypes) {
		this.granttypes = granttypes;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getScopes() {
		return scopes;
	}

	public void setScopes(String scopes) {
		this.scopes = scopes;
	}

	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
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

	public String getAuthorities() {
		return authorities;
	}

	public void setAuthorities(String authorities) {
		this.authorities = authorities;
	}
	@JsonFilter("ignoreFields")
	private class FilterHelper {
	}
}
