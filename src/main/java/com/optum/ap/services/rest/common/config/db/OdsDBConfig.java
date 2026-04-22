package com.optum.ap.services.rest.common.config.db;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.optum.ap.services.rest.dao", entityManagerFactoryRef = "odsEntityManager", transactionManagerRef = "odsTransactionManager")
@DBPresentConditionController(db = "ODS")
public class OdsDBConfig {
	
	private static LocalContainerEntityManagerFactoryBean odsEntityManager = null;

	@Bean(name = "odsDataSource", destroyMethod = "")
	@DBPresentConditionController(db = "ODS")
	public DataSource odsDataSource() {
		return AaaOAPDBConfig.setDataSource("ODS");
	}

	@Bean(name = "odsEntityManager")
	@ConfigurationProperties(prefix = "spring.jpa")
	@DBPresentConditionController(db = "ODS")
	public LocalContainerEntityManagerFactoryBean odsEntityManagerFactory() {
		if (odsEntityManager == null) {
			odsEntityManager = AaaOAPDBConfig.entityManagerFactory(odsDataSource(), "ODS",
				new String[] { "com.tpa.ap.domain.ods" });
		}
		return odsEntityManager;
	}

	@Bean(name = "odsTransactionManager")
	@DBPresentConditionController(db = "ODS")
	public PlatformTransactionManager odsTransactionManager() {
		return AaaOAPDBConfig.transactionManager(odsEntityManagerFactory(), odsDataSource());
	}
}
