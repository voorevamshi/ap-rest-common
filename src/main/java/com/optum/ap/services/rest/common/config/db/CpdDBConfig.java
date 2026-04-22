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
@EnableJpaRepositories(basePackages = {"com.optum.ap.services.rest.dao", "com.optum.ap.services.rest.opd2.repository"}, entityManagerFactoryRef = "cpdEntityManager", transactionManagerRef = "cpdTransactionManager")
@DBPresentConditionController(db = "CPD")
public class CpdDBConfig {
	
	private static LocalContainerEntityManagerFactoryBean cpdEntityManager = null;

	@Bean(name = "cpdDataSource", destroyMethod = "")
	@DBPresentConditionController(db = "CPD")
	public DataSource cpdDataSource() {
		return AaaOAPDBConfig.setDataSource("CPD");
	}

	@Bean(name = "cpdEntityManager")
	@ConfigurationProperties(prefix = "spring.jpa")
	@DBPresentConditionController(db = "CPD")
	public LocalContainerEntityManagerFactoryBean cpdEntityManagerFactory() {
		if (cpdEntityManager == null) {
			cpdEntityManager = AaaOAPDBConfig.entityManagerFactory(cpdDataSource(), "CPD",
				new String[] { "com.tpa.ap.domain.opd2" });
		}
		return cpdEntityManager;
	}

	@Bean(name = "cpdTransactionManager")
	@DBPresentConditionController(db = "CPD")
	public PlatformTransactionManager cpdTransactionManager() {
		return AaaOAPDBConfig.transactionManager(cpdEntityManagerFactory(), cpdDataSource());
	}
}
