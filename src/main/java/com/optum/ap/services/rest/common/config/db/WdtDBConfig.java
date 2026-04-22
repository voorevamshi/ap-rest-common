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
@EnableJpaRepositories(basePackages = "com.optum.ap.services.rest.dao", entityManagerFactoryRef = "wdtEntityManager", transactionManagerRef = "wdtTransactionManager")
@DBPresentConditionController(db = "WDT")
public class WdtDBConfig {
	
	private static LocalContainerEntityManagerFactoryBean wdtEntityManager = null;

	@Bean(name = "wdtDataSource", destroyMethod = "")
	@DBPresentConditionController(db = "WDT")
	public DataSource wdtDataSource() {
		return AaaOAPDBConfig.setDataSource("WDT");
	}

	@Bean(name = "wdtEntityManager")
	@ConfigurationProperties(prefix = "spring.jpa")
	@DBPresentConditionController(db = "WDT")
	public LocalContainerEntityManagerFactoryBean wdtEntityManagerFactory() {
		if (wdtEntityManager == null) {
			wdtEntityManager = AaaOAPDBConfig.entityManagerFactory(wdtDataSource(), "WDT",
				new String[] { "com.tpa.ap.domain.wdt", "com.tpa.cdhclaims.service.ejb.domain.wdt" });
		}
		return wdtEntityManager;
	}

	@Bean(name = "wdtTransactionManager")
	@DBPresentConditionController(db = "WDT")
	public PlatformTransactionManager wdtTransactionManager() {
		return AaaOAPDBConfig.transactionManager(wdtEntityManagerFactory(), wdtDataSource());
	}
}
