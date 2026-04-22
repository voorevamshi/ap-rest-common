package com.optum.ap.services.rest.common.config.db;

import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class DBPresentCondition implements Condition {

	protected final Logger logger = Logger.getLogger(this.getClass());

	@Override
	public boolean matches(ConditionContext conditionalContext, AnnotatedTypeMetadata metadata) {

		Map<String, Object> attributes = metadata.getAnnotationAttributes(DBPresentConditionController.class.getName());
		String db = ((String) attributes.get("db")).toLowerCase();

		if (conditionalContext.getEnvironment().containsProperty("databaseList")
				&& conditionalContext.getEnvironment().getProperty("databaseList") != null) {
			// Remove empty space and wrap with commas so end values are matched
			String databaseList = "," + conditionalContext.getEnvironment().getProperty("databaseList").replaceAll("\\s", "") + ",";
			if (databaseList.toLowerCase().contains("," + db + ",")) {
				if (conditionalContext.getEnvironment().containsProperty(db + ".datasource.name") ||
						(conditionalContext.getEnvironment().containsProperty("ap-dbsources.databases." + db + ".datasource.name"))) {
					return true;
				} else {
					logger.error(db + " database is in databaseList but dataSource config was not found");
				}
			}
		}
		return false;
	}

}
