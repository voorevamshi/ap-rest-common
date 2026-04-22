package com.optum.ap.services.rest.common.security.dto;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@ConfigurationProperties("ap-stargate")
public class StargateConfigProperties {

	private String[] roles;
	private List<StargateAuthUser> users = new ArrayList<StargateAuthUser>();

	public String toString() {
		ObjectMapper json = new ObjectMapper();
		try {
			return json.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			return "{\"error\": \"" + e.getMessage() + "\"}";
		}
	}

	/**
	 * @return the roles
	 */
	public String[] getRoles() {
		return roles;
	}

	/**
	 * @param roles
	 *            the roles to set
	 */
	public void setRoles(String[] roles) {
		this.roles = roles;
	}

	public List<StargateAuthUser> getUsers() {
		return users;
	}

	public void setUsers(List<StargateAuthUser> users) {
		this.users = users;
	}

}
