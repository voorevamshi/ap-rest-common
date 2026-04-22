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
@EnableJpaRepositories(basePackages = "com.optum.ap.services.rest.dao", entityManagerFactoryRef = "slcEntityManager", transactionManagerRef = "slcTransactionManager")
@DBPresentConditionController(db = "SLC")
public class SlcDBConfig {
	
	private static LocalContainerEntityManagerFactoryBean slcEntityManager = null;

	@Bean(name = "slcDataSource", destroyMethod = "")
	@DBPresentConditionController(db = "SLC")
	public DataSource slcDataSource() {
		return AaaOAPDBConfig.setDataSource("SLC");
	}

	@Bean(name = "slcEntityManager")
	@ConfigurationProperties(prefix = "spring.jpa")
	@DBPresentConditionController(db = "SLC")
	public LocalContainerEntityManagerFactoryBean slcEntityManagerFactory() {
		if (slcEntityManager == null) {
			slcEntityManager = AaaOAPDBConfig.entityManagerFactory(slcDataSource(), "SLC",
				new String[] { "com.tpa.ap.domain.slc" });
		}
		return slcEntityManager;
	}

	@Bean(name = "slcTransactionManager")
	@DBPresentConditionController(db = "SLC")
	public PlatformTransactionManager slcTransactionManager() {
		return AaaOAPDBConfig.transactionManager(slcEntityManagerFactory(), slcDataSource());
	}
}
