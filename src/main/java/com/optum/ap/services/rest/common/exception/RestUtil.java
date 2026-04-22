/**
 * 
 */
package com.optum.ap.services.rest.common.exception;

import org.springframework.http.HttpStatusCode;

/**
 * @author bperla
 *
 */
public class RestUtil {
	
	public static boolean isError(HttpStatusCode httpStatusCode) {
		return httpStatusCode.isError();
	}
}
