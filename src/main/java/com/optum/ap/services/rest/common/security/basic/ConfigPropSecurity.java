package com.optum.ap.services.rest.common.security.basic;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties
public class ConfigPropSecurity {

	private List<String> serviceRoleList = new ArrayList<String>();

	public List<String> getServiceRoleList() {
		return serviceRoleList;
	}

	public void setServiceRoleList(List<String> serviceRoleList) {
		this.serviceRoleList = serviceRoleList;
	}

}
