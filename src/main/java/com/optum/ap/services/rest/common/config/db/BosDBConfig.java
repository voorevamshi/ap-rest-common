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
@EnableJpaRepositories(basePackages = "com.optum.ap", entityManagerFactoryRef = "bosEntityManager", transactionManagerRef = "bosTransactionManager")
@DBPresentConditionController(db = "bos001db-BOSWeb")
public class BosDBConfig {

	private static LocalContainerEntityManagerFactoryBean bosEntityManager = null;

	@Bean(name = "bosDataSource", destroyMethod = "")
	@DBPresentConditionController(db = "bos001db-BOSWeb")
	public DataSource bosDataSource() {
		return AaaOAPDBConfig.setDataSource("bos001db-BOSWeb");
	}

	@Bean(name = "bosEntityManager")
	@ConfigurationProperties(prefix = "spring.jpa")
	@DBPresentConditionController(db = "bos001db-BOSWeb")
	public LocalContainerEntityManagerFactoryBean bosEntityManagerFactory() {
		if (bosEntityManager == null) {
			bosEntityManager = AaaOAPDBConfig.entityManagerFactory(bosDataSource(), "bos001db-BOSWeb",
				new String[] { "com.optum.ap.domain.bos" });
		}
		return bosEntityManager;
	}

	@Bean(name = "bosTransactionManager")
	@DBPresentConditionController(db = "bos001db-BOSWeb")
	public PlatformTransactionManager bosTransactionManager() {
		return AaaOAPDBConfig.transactionManager(bosEntityManagerFactory(), bosDataSource());
	}
}
