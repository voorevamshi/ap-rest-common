/**
 * 
 */
package com.optum.ap.services.rest.service.controller;

import java.util.logging.Logger;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
/**
 * @author dgoyal2
 *
 */
@PreAuthorize("@stargateSecurity.hasAuthScope(authentication)")
@SecurityRequirement(name = "basicAuth")
public abstract class StargateBaseController {
	
	
	private Logger logger = Logger.getLogger(StargateBaseController.class.getName());


	
	private static final String[] DISALLOWED_FILEDS = new String[0];

	/**
	 * Subclasses that make use of the methods provided by this base class
	 * should override this method to return a Logger initialized
	 * 
	 * @return
	 */
	protected Logger getLogger() {
		return logger;
	}


	/*
	 * Send the response back as OK or error or any other status with wither the
	 * message or the actual response
	 */
	@SuppressWarnings("unchecked")
	protected <T> ResponseEntity<T> returnResponse(HttpStatus status, Object entity) {
		return (ResponseEntity<T>) ResponseEntity.status(status).body(entity);
	}

	@InitBinder
	public void allowedInitBinder(WebDataBinder binder) {
		binder.setDisallowedFields(DISALLOWED_FILEDS);
	}
}
