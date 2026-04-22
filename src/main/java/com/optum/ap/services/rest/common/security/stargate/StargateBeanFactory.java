package com.optum.ap.services.rest.common.security.stargate;

import java.util.Collection;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.GrantedAuthority;

@Configuration
public class StargateBeanFactory {

	@Bean
	@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public StargateAuthenticationToken stargateAuthToken(String userName,Collection<? extends GrantedAuthority> authorities) {
		return new StargateAuthenticationToken(userName, authorities);
	}
}	
