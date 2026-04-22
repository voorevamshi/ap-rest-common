/**
 * 
 */
package com.optum.ap.services.rest.common.security.basic;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

import com.optum.ap.services.rest.common.security.APSecurityBasicAuthEntryPoint;
import com.optum.ap.services.rest.common.security.dto.AuthConfigProperties;
import com.optum.ap.services.rest.common.security.dto.AuthUser;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author bperla
 *
 */
@Configuration
@Component
@EnableWebSecurity
@Order(10)
public class APSecurityConfig {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	//	public static final String[] AUTH_WHITELIST = { "/api-docs/**", "/actuator/**","/configuration/**", "/configuration/security",
	//			"/configuration/ui", "/health", "/noise", "/monitor", "/swagger-resources/**", "/swagger-resources/configuration/ui",
	//			"/swagger-ui.html", "/v2/api-docs", "/webjars/**" };
	//	public static final String[] AUTH_WHITELIST = { "/**" };
	public static final String[] AUTH_WHITELIST = { "/api-docs/**",
			"/configuration/**", "/configuration/security", "/configuration/ui",
			"/health", "/monitor",
			"/swagger-resources/**", "/swagger-resources/configuration/ui", "/swagger-ui.html", "/swagger-ui/**",
			"/ap-hello-world-service-rest/swagger-ui/**",
			"/v2/api-docs", "/v3/api-docs/**", "/webjars/**" };

	@Autowired
	private AuthConfigProperties authConfigProps;

	@Autowired
	private ConfigPropSecurity configProps;

	@Bean
	@Order(10)
	public SecurityFilterChain configureBasic(HttpSecurity http) throws Exception {
		logger.debug("APSecurityConfig configure");
		//http.securityMatcher(new BasicRequestMatcher());
		http.csrf((csrf)->csrf.ignoringRequestMatchers("/**"));
		http
				.authorizeHttpRequests((authorize) -> authorize
						.requestMatchers("/actuator/**").hasRole("metrics-monitor")
						.requestMatchers(getAntRequestMatchers(List.of(APSecurityConfig.AUTH_WHITELIST))).permitAll()
						.requestMatchers("/**")
						.hasAnyRole(configProps.getServiceRoleList()
								.toArray(new String[configProps.getServiceRoleList().size()])))
				.httpBasic(withDefaults());
		http.exceptionHandling(
				(exceptionHandling) -> exceptionHandling.authenticationEntryPoint(new APSecurityBasicAuthEntryPoint()));
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

	private class BasicRequestMatcher implements RequestMatcher {
		@Override
		public boolean matches(HttpServletRequest request) {

			/* condition check only for URLs which prompt for authorization header */
			if (authConfigProps.getAuthBasicWithPrompt().contains(request.getRequestURI())) {
				return true;
			}
			String auth = request.getHeader("Authorization");
			return (auth != null && auth.startsWith("Basic"));
		}
	}

	@Bean
	public UserDetailsService userDetailsService() {
		logger.error("Using no-op password encoder to overcome bcrypt2 slowness{}", "");
		logger.info("Using no-op password encoder to overcome bcrypt2 slowness{}", "");
		// Spring Boot 2.* added a lag to password authentication to thwart dictionary attacks. The lag causes problems with
		// high demand services like ap-ems-service-rest. Forcing the no-op password removes the lag. The long run solution
		// should be to abandon Auth Basic and move to a more secure form of authentication.
		//		References:
		//		https://stackoverflow.com/questions/52040679/how-to-speed-up-springboot-2-0-basic-authentication
		InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
		List<AuthUser> users = authConfigProps.getUsers();
		ArrayList<UserDetails> userList = new ArrayList<>();
		if (users != null && !users.isEmpty()) {
			for (AuthUser user : users) {
				logger.info("Users {}", user);
				manager.createUser(User.withUsername(user.getName().trim())
						.password(passwordEncoder().encode(user.getPassword().trim()))
						.roles(user.getRoles().toArray(new String[user.getRoles().size()])).build());
			}
		}
		return manager;
	}

	@Bean
	public static PasswordEncoder passwordEncoder() {
		return NoOpPasswordEncoder.getInstance();
	}
	//	@Override
	//	protected void configure(HttpSecurity http) throws Exception {
	//		http.requestMatcher(new BasicRequestMatcher());
	//		http.csrf().disable();
	//
	//		http.authorizeRequests().antMatchers(AUTH_WHITELIST).permitAll();
	//		http.authorizeRequests().anyRequest()
	//				.hasAnyRole(
	//						configProps.getServiceRoleList().toArray(new String[configProps.getServiceRoleList().size()]))
	//				.and().httpBasic();
	//		http.exceptionHandling().authenticationEntryPoint(new APSecurityBasicAuthEntryPoint());
	//	}
	//	@Bean
	//	public WebSecurityCustomizer webSecurityCustomizer() {
	//		return (web) -> web.ignoring()
	//				.requestMatchers(AUTH_WHITELIST);
	//	}
	//	@Override
	//	public void configure(WebSecurity web) throws Exception {
	//		web.ignoring().antMatchers(AUTH_WHITELIST);
	//	}
}
