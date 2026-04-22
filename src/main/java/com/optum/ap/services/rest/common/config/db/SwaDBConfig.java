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
@EnableJpaRepositories(basePackages = "com.optum.ap.services.rest.dao", entityManagerFactoryRef = "swaEntityManager", transactionManagerRef = "swaTransactionManager")
@DBPresentConditionController(db = "SWA")
public class SwaDBConfig {
	
	private static LocalContainerEntityManagerFactoryBean swaEntityManager = null;

	@Bean(name = "swaDataSource", destroyMethod = "")
	@DBPresentConditionController(db = "SWA")
	public DataSource swaDataSource() {
		return AaaOAPDBConfig.setDataSource("SWA");
	}

	@Bean(name = "swaEntityManager")
	@ConfigurationProperties(prefix = "spring.jpa")
	@DBPresentConditionController(db = "SWA")
	public LocalContainerEntityManagerFactoryBean swaEntityManagerFactory() {
		if (swaEntityManager == null) {
			swaEntityManager = AaaOAPDBConfig.entityManagerFactory(swaDataSource(), "SWA",
				new String[] { "com.tpa.ap.domain.swa" });
		}
		return swaEntityManager;
	}

	@Bean(name = "swaTransactionManager")
	@DBPresentConditionController(db = "SWA")
	public PlatformTransactionManager swaTransactionManager() {
		return AaaOAPDBConfig.transactionManager(swaEntityManagerFactory(), swaDataSource());
	}
}
