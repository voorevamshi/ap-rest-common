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
		"com.optum.ap.services.rest.dao", "com.tpa.ap.domain.aps.Repository" }, entityManagerFactoryRef = "apsEntityManager", transactionManagerRef = "apsTransactionManager")
@DBPresentConditionController(db = "APS")
public class ApsDBConfig {
	
	private static LocalContainerEntityManagerFactoryBean apsEntityManager = null;

	@Bean(name = "apsDataSource", destroyMethod = "")
	@DBPresentConditionController(db = "APS")
	public DataSource apsDataSource() {
		return AaaOAPDBConfig.setDataSource("APS");
	}

	@Bean(name = "apsEntityManager")
	@ConfigurationProperties(prefix = "spring.jpa")
	@DBPresentConditionController(db = "APS")
	public LocalContainerEntityManagerFactoryBean apsEntityManagerFactory() {
		if (apsEntityManager == null) {
			apsEntityManager = AaaOAPDBConfig.entityManagerFactory(apsDataSource(), "APS",
				new String[] { "com.tpa.ap.domain.aps", "com.tpa.cdhclaims.service.ejb.domain.aps" });
		}
		return apsEntityManager;
	}

	@Bean(name = "apsTransactionManager")
	@DBPresentConditionController(db = "APS")
	public PlatformTransactionManager apsTransactionManager() {
		return AaaOAPDBConfig.transactionManager(apsEntityManagerFactory(), apsDataSource());
	}
}
