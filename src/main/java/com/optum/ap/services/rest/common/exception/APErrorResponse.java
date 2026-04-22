/**
 * 
 */
package com.optum.ap.services.rest.common.exception;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author bperla
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class APErrorResponse {

	private int errorCode;
	private String message;
	
	public int getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	
}
