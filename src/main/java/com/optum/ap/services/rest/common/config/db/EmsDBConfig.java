package com.optum.ap.services.rest.common.config.db;

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
@EnableJpaRepositories(basePackages = {
		"com.optum.ap.services.rest.dao" }, entityManagerFactoryRef = "emsEntityManager", transactionManagerRef = "emsTransactionManager")
@DBPresentConditionController(db = "EMS")
public class EmsDBConfig {
	
	private static LocalContainerEntityManagerFactoryBean emsEntityManager = null;

	@Bean(name = "emsDataSource", destroyMethod = "")
	@DBPresentConditionController(db = "EMS")
	public DataSource emsDataSource() {
		return AaaOAPDBConfig.setDataSource("EMS");
	}

	@Bean(name = "emsEntityManager")
	@ConfigurationProperties(prefix = "spring.jpa")
	@DBPresentConditionController(db = "EMS")
	public LocalContainerEntityManagerFactoryBean emsEntityManagerFactory() {
		if (emsEntityManager == null) {
			emsEntityManager = AaaOAPDBConfig.entityManagerFactory(emsDataSource(), "EMS",
				new String[] { "com.tpa.ap.domain.ems" });
		}
		return emsEntityManager;
	}

	@Bean(name = "emsTransactionManager")
	@DBPresentConditionController(db = "EMS")
	public PlatformTransactionManager emsTransactionManager() {
		return AaaOAPDBConfig.transactionManager(emsEntityManagerFactory(), emsDataSource());
	}
}
