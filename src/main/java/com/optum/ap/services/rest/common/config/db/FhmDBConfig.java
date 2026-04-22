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
		"com.optum.ap.services.rest.dao" }, entityManagerFactoryRef = "fhmEntityManager", transactionManagerRef = "fhmTransactionManager")
@DBPresentConditionController(db = "FHM")
public class FhmDBConfig {
	
	private static LocalContainerEntityManagerFactoryBean fhmEntityManager = null;

	@Bean(name = "fhmDataSource", destroyMethod = "")
	@DBPresentConditionController(db = "FHM")
	public DataSource fhmDataSource() {
		return AaaOAPDBConfig.setDataSource("FHM");
	}

	@Bean(name = "fhmEntityManager")
	@ConfigurationProperties(prefix = "spring.jpa")
	@DBPresentConditionController(db = "FHM")
	public LocalContainerEntityManagerFactoryBean fhmEntityManagerFactory() {
		if (fhmEntityManager == null) {
			fhmEntityManager = AaaOAPDBConfig.entityManagerFactory(fhmDataSource(), "FHM",
				new String[] { "com.tpa.ap.domain.fhm" });
		}
		return fhmEntityManager;
	}

	@Bean(name = "fhmTransactionManager")
	@DBPresentConditionController(db = "FHM")
	public PlatformTransactionManager fhmTransactionManager() {
		return AaaOAPDBConfig.transactionManager(fhmEntityManagerFactory(), fhmDataSource());
	}
}
