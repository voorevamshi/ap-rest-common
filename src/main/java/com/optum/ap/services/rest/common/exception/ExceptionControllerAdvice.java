package com.optum.ap.services.rest.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionControllerAdvice {	
	
	@ExceptionHandler(APRestException.class)
	public ResponseEntity<APErrorResponse> exceptionHandler(APRestException re){
		APErrorResponse error = new APErrorResponse();
		error.setErrorCode(HttpStatus.BAD_REQUEST.value());
		error.setMessage(re.getErrorMessage());
		return new ResponseEntity<APErrorResponse>(error, HttpStatus.BAD_REQUEST);
	}
	
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<APErrorResponse> exceptionHandler(Exception ex){
		APErrorResponse error = new APErrorResponse();
		error.setErrorCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		error.setMessage(ex.getMessage()); // can the message be generic like "Please contact your administrator"
		return new ResponseEntity<APErrorResponse>(error, HttpStatus.INTERNAL_SERVER_ERROR);				
	}

}
