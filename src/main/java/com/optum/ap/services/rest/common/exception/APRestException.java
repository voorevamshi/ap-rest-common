/**
 * 
 */
package com.optum.ap.services.rest.common.exception;

/**
 * @author bperla
 *
 */
public class APRestException extends Exception {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -99591194213725863L;
	private String errorMessage;
	

	/**
	 * 
	 */
	public APRestException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public APRestException(String message) {
		super(message);
		setErrorMessage(message);
		
	}

	/**
	 * @param cause
	 */
	public APRestException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public APRestException(String message, Throwable cause) {
		super(message);
		setErrorMessage(message);
	}

	

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
