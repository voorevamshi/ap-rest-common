package com.optum.ap.services.rest.common.security.dto;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@ConfigurationProperties("ap-oauth")
public class OAuthConfigProp {

	private List<OAuthUser> users = new ArrayList<OAuthUser>();

	public List<OAuthUser> getUsers() {
		return users;
	}

	public void setUsers(List<OAuthUser> users) {
		this.users = users;
	}
	public String toString() {
		ObjectMapper json = new ObjectMapper();
		try {
			return json.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			return "{\"error\": \"" + e.getMessage() + "\"}";
		}
	}
}
