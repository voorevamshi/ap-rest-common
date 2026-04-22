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
@EnableJpaRepositories(basePackages = "com.optum.ap.services.rest.dao", entityManagerFactoryRef = "msrEntityManager", transactionManagerRef = "msrTransactionManager")
@DBPresentConditionController(db = "MSR")
public class MsrDBConfig {
	
	private static LocalContainerEntityManagerFactoryBean msrEntityManager = null;

	@Bean(name = "msrDataSource", destroyMethod = "")
	@DBPresentConditionController(db = "MSR")
	public DataSource msrDataSource() {
		return AaaOAPDBConfig.setDataSource("MSR");
	}

	@Bean(name = "msrEntityManager")
	@ConfigurationProperties(prefix = "spring.jpa")
	@DBPresentConditionController(db = "MSR")
	public LocalContainerEntityManagerFactoryBean msrEntityManagerFactory() {
		if (msrEntityManager == null) {
			msrEntityManager = AaaOAPDBConfig.entityManagerFactory(msrDataSource(), "MSR",
				new String[] { "com.tpa.ap.domain.msr" });
		}
		return msrEntityManager;
	}

	@Bean(name = "msrTransactionManager")
	@DBPresentConditionController(db = "MSR")
	public PlatformTransactionManager msrTransactionManager() {
		return AaaOAPDBConfig.transactionManager(msrEntityManagerFactory(), msrDataSource());
	}
}
