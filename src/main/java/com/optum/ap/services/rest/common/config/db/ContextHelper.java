package com.optum.ap.services.rest.common.config.db;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextHelper {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	// Singleton instance
	private static final ContextHelper INSTANCE = new ContextHelper();

	private Context ctx;

	// Singleton: private constructor prevents instantiation from other classes
	private ContextHelper() {
		try {
			ctx = new InitialContext();
			ctx.createSubcontext("jdbc");
		} catch (NamingException e) {
			logger.error("error in creating initial context or subcontext : ", e);
		}
	}

	public static ContextHelper getInstance() {
		return INSTANCE;
	}

	public Context getContext() {
		return ctx;
	}
}
