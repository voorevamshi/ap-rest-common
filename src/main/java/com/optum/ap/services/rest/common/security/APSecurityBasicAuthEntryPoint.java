package com.optum.ap.services.rest.common.security;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

import com.tpa.ap.common.service.util.ESAPIUtil;
import com.tpa.ap.common.service.util.ValidatorTypes;

public class APSecurityBasicAuthEntryPoint extends BasicAuthenticationEntryPoint {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authEx)
			throws IOException {

		Enumeration<String> headerNames = request.getHeaderNames();
		String headerList = "";
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			String headerValue = request.getHeader(headerName);
			headerList += headerName + "=" + headerValue + "; ";
		}
		super.commence(request, response, authEx);
	}
}
