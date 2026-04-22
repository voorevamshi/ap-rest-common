package com.optum.ap.services.rest.common.config.db;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class ServerDBPresentCondition implements Condition {

	@Override
	public boolean matches(ConditionContext conditionalContext, AnnotatedTypeMetadata arg1) {

		String dbList = conditionalContext.getEnvironment().getProperty("databaseList");
		return dbList != null;
	}
}
