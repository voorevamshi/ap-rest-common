package com.optum.ap.services.rest.common.security.stargate;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import com.auth0.jwt.interfaces.DecodedJWT;

/**
 * @author dgoyal2
 *
 *         Custom Filter to filter the requests coming from api gateway This
 *         filter will help to validate the JWT token received embeded in the
 *         request from the gateway end.
 */
@Component
public class StargateRequestFilter extends GenericFilterBean {
	private static final Logger logger = Logger.getLogger(StargateRequestFilter.class);

	@Autowired
	private StargateBeanFactory stargateFactory;
	
	@Override
	public void destroy() {

	}

	/**
	 * This method helps to bypass the request object which should not go through
	 * JWT validation
	 * 
	 * @param request
	 * @return
	 */
	public boolean shouldNotFilter(HttpServletRequest request) {
		String auth = request.getHeader("Authorization");
		String jwtHeader = request.getHeader("JWT");
//		logger.error("shouldNotFilter: " + (auth != null && auth.startsWith("Basic") && jwtHeader == null));
		return (auth != null && auth.startsWith("Basic") && jwtHeader == null);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
			throws IOException, ServletException {
		MultiReadRequestWrapper wrappedReq = new MultiReadRequestWrapper((HttpServletRequest) request);

//		SecurityContext auth = SecurityContextHolder.getContext();
//		logger.error("Authentication object: " + auth.toString());
//		logger.error("URL: " + wrappedReq.getRequestURI());
//		Enumeration<String> attrs = wrappedReq.getHeaderNames();
//		while (attrs.hasMoreElements()) {
//			String attr = attrs.nextElement();
//			logger.error("Header " + attr + ": " + request.getAttribute(attr));
//		}
		String jwtToken = wrappedReq.getHeader("JWT");
		if (jwtToken == null) {
//			logger.error("doFilter: jwToken is null");
//			throw new AuthorizationServiceException("Request in not authorized");
		} else {

			try {
				StargateJWTValidator validator = new StargateJWTValidator();
				DecodedJWT decodedJWT = validator.validateJWTToken(jwtToken, wrappedReq.getInputStream(), null);

				if (decodedJWT == null) {
//					logger.error("doFilter: decodedJWT is null");
					throw new AuthorizationServiceException("Request in not authorized decoded jwt is null//invalid");
				} else {
					if (SecurityContextHolder.getContext() != null) {
						String userName = decodedJWT.getClaim("consumername").asString();
						StargateAuthenticationToken authToken=stargateFactory.stargateAuthToken(userName,Collections.singleton(new SimpleGrantedAuthority(userName)));
						SecurityContextHolder.getContext().setAuthentication(authToken);
					}
				}
			} catch (Exception e) {
				logger.error("Exception generated "+e.getMessage());
				throw new AuthorizationServiceException("Exception generated "+e.getMessage());
			}
		}
//		logger.error("doFilter: invoking chain");
		filterChain.doFilter(wrappedReq, response);
	}
}
