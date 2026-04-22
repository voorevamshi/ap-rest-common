/**
 * 
 */
package com.optum.ap.services.rest.common.exception;

import java.io.IOException;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.apache.log4j.Logger;

public class ApResponseErrorHandler implements ResponseErrorHandler {
	protected static Logger logger = Logger.getLogger(ApResponseErrorHandler.class);
	@Override
	public boolean hasError(ClientHttpResponse response) throws IOException {

		return RestUtil.isError(response.getStatusCode());
	}

//	@Override
	public void handleError(ClientHttpResponse response) throws IOException {
		logger.error("Response Error: " + response.getStatusCode() + " " + response.getStatusText());

	}
}
