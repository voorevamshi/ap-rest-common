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
@EnableJpaRepositories(basePackages = {"com.optum.ap.services.rest.dao", "com.optum.ap.services.rest.opd.repository"}, entityManagerFactoryRef = "opdEntityManager", transactionManagerRef = "opdTransactionManager")
@DBPresentConditionController(db = "OPD")
public class OpdDBConfig {
	
	private static LocalContainerEntityManagerFactoryBean opdEntityManager = null;

	@Bean(name = "opdDataSource", destroyMethod = "")
	@DBPresentConditionController(db = "OPD")
	public DataSource opdDataSource() {
		return AaaOAPDBConfig.setDataSource("OPD");
	}

	@Bean(name = "opdEntityManager")
	@ConfigurationProperties(prefix = "spring.jpa")
	@DBPresentConditionController(db = "OPD")
	public LocalContainerEntityManagerFactoryBean opdEntityManagerFactory() {
		if (opdEntityManager == null) {
			opdEntityManager = AaaOAPDBConfig.entityManagerFactory(opdDataSource(), "OPD",
				new String[] { "com.tpa.ap.domain.opd" });
		}
		return opdEntityManager;
	}

	@Bean(name = "opdTransactionManager")
	@DBPresentConditionController(db = "OPD")
	public PlatformTransactionManager opdTransactionManager() {
		return AaaOAPDBConfig.transactionManager(opdEntityManagerFactory(), opdDataSource());
	}
}
