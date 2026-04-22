package com.optum.ap.services.rest.common.security.dto;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@ConfigurationProperties("ap-auth")
public class AuthConfigProperties {

	private List<String> roles = new ArrayList<String>();

	private List<AuthUser> users = new ArrayList<AuthUser>();
	
	private List<String> authBasicWithPrompt = new ArrayList<String>();
	
	
	public List<String> getAuthBasicWithPrompt() {
		return authBasicWithPrompt;
	}

	public void setAuthBasicWithPrompt(List<String> authBasicWithPrompt) {
		this.authBasicWithPrompt = authBasicWithPrompt;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	public List<AuthUser> getUsers() {
		return users;
	}

	public void setUsers(List<AuthUser> users) {
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
