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
@EnableJpaRepositories(basePackages = "com.optum.ap.services.rest.dao", entityManagerFactoryRef = "cdhEntityManager", transactionManagerRef = "cdhTransactionManager")
@DBPresentConditionController(db = "CDH")
public class CdhDBConfig {
	
	private static LocalContainerEntityManagerFactoryBean cdhEntityManager = null;

	@Bean(name = "cdhDataSource", destroyMethod = "")
	@DBPresentConditionController(db = "CDH")
	public DataSource cdhDataSource() {
		return AaaOAPDBConfig.setDataSource("CDH");
	}

	@Bean(name = "cdhEntityManager")
	@ConfigurationProperties(prefix = "spring.jpa")
	@DBPresentConditionController(db = "CDH")
	public LocalContainerEntityManagerFactoryBean cdhEntityManagerFactory() {
		if (cdhEntityManager == null) {
			cdhEntityManager = AaaOAPDBConfig.entityManagerFactory(cdhDataSource(), "CDH",
				new String[] { "com.tpa.cdhclaims.service.ejb.domain.cdh" });
		}
		return cdhEntityManager;
	}

	@Bean(name = "cdhTransactionManager")
	@DBPresentConditionController(db = "CDH")
	public PlatformTransactionManager cdhTransactionManager() {
		return AaaOAPDBConfig.transactionManager(cdhEntityManagerFactory(), cdhDataSource());
	}
}
