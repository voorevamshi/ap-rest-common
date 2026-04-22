package com.optum.ap.services.rest.common.security.basic;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BasicSecurity {
	private static final Logger logger = Logger.getLogger(BasicSecurity.class);

	public boolean hasAuthScope(Authentication authentication) {
		if (authentication != null && authentication instanceof UsernamePasswordAuthenticationToken) {
			logger.error("hasScope: true");
			return true;
		} else {
			logger.error("hasScope: false");
			return false;
		}
	}

	//@Bean
	public PasswordEncoder passwordEncoder() {
		logger.error("passwordEncoder");
		PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
		((DelegatingPasswordEncoder) passwordEncoder)
				.setDefaultPasswordEncoderForMatches(NoOpPasswordEncoder.getInstance());
		return passwordEncoder;
	}
}
