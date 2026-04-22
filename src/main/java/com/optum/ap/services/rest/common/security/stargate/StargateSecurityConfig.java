/**
 * 
 */
package com.optum.ap.services.rest.common.security.stargate;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.optum.ap.services.rest.common.security.basic.APSecurityConfig;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author dgoyal2
 *
 */
@Configuration
@Order(5)
public class StargateSecurityConfig {

	@Autowired
	private StargateRequestFilter stargateFilter;

	
	@Bean
	@Order(5)
	public SecurityFilterChain configureStargate(HttpSecurity http) throws Exception {
		//		logger.error("configure Stargate HttpSecurity");

		http.csrf((csrf)->csrf.ignoringRequestMatchers("/**"));
		http
				.authorizeHttpRequests(
						(authorize) -> {
							authorize.requestMatchers(getAntRequestMatchers(List.of(APSecurityConfig.AUTH_WHITELIST))).permitAll();
							authorize.requestMatchers("/**").authenticated();
						})
				.securityMatcher(new StargateRequestMatcher())
				.addFilterAfter(stargateFilter, BasicAuthenticationFilter.class)
				.httpBasic(Customizer.withDefaults());
		SecurityFilterChain chChChains = http.build();
		return chChChains;
	}
	
	private RequestMatcher getAntRequestMatchers(List<String> patterns) {
		if (patterns.isEmpty()) {
			return null;
		}
		List<RequestMatcher> matchers = patterns.stream()
				.map(PathPatternRequestMatcher::pathPattern)
				.collect(Collectors.toList());

		return new OrRequestMatcher(matchers);
	}

	public class StargateRequestMatcher implements RequestMatcher {
		@Override
		public boolean matches(HttpServletRequest request) {
			String auth = request.getHeader("Authorization");
			String jwtHeader = request.getHeader("JWT");
			//			logger.error("StargateMatcher: auth " + (auth == null ? "is null" : auth) + " jwtHeader: " + (jwtHeader == null ? "is null" : jwtHeader)
			//					+ " returning " + (auth != null && auth.startsWith("Bearer") && jwtHeader != null));
			boolean matcher = (auth != null && auth.startsWith("Bearer") && jwtHeader != null);
			return matcher;
		}
	}
}
