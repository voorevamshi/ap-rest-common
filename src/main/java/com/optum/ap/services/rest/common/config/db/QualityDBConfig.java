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
@EnableJpaRepositories(basePackages = "com.optum.ap.services.rest.dao", entityManagerFactoryRef = "qualityEntityManager", transactionManagerRef = "qualityTransactionManager")
@DBPresentConditionController(db = "quality-qualityUsr")
public class QualityDBConfig {
	
	private static LocalContainerEntityManagerFactoryBean qualityEntityManager = null;

	@Bean(name = "qualityDataSource", destroyMethod = "")
	@DBPresentConditionController(db = "QUALITY")
	public DataSource qualityDataSource() {
		return AaaOAPDBConfig.setDataSource("QUALITY");
	}

	@Bean(name = "qualityEntityManager")
	@ConfigurationProperties(prefix = "spring.jpa")
	@DBPresentConditionController(db = "QUALITY")
	public LocalContainerEntityManagerFactoryBean qualityEntityManagerFactory() {
		if (qualityEntityManager == null) {
			qualityEntityManager = AaaOAPDBConfig.entityManagerFactory(qualityDataSource(), "QUALITY",
				new String[] { "com.tpa.ap.domain.quality" });
		}
		return qualityEntityManager;
	}

	@Bean(name = "qualityTransactionManager")
	@DBPresentConditionController(db = "QUALITY")
	public PlatformTransactionManager qualityTransactionManager() {
		return AaaOAPDBConfig.transactionManager(qualityEntityManagerFactory(), qualityDataSource());
	}
}
