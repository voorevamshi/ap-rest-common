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
@EnableJpaRepositories(basePackages = "com.optum.ap.services.rest.dao", entityManagerFactoryRef = "rmdEntityManager", transactionManagerRef = "rmdTransactionManager")
@DBPresentConditionController(db = "RMD")
public class RMDDBConfig {
	
	private static LocalContainerEntityManagerFactoryBean rmdEntityManager = null;

	@Bean(name = "rmdDataSource", destroyMethod = "")
	@DBPresentConditionController(db = "RMD")
	public DataSource rmdDataSource() {
		return AaaOAPDBConfig.setDataSource("RMD");
	}

	@Bean(name = "rmdEntityManager")
	@ConfigurationProperties(prefix = "spring.jpa")
	@DBPresentConditionController(db = "RMD")
	public LocalContainerEntityManagerFactoryBean rmdEntityManagerFactory() {
		if (rmdEntityManager == null) {
			rmdEntityManager = AaaOAPDBConfig.entityManagerFactory(rmdDataSource(), "RMD",
				new String[] { "com.optum.ap.domain.rmd.entity" });
		}
		return rmdEntityManager;
	}

	@Bean(name = "rmdTransactionManager")
	@DBPresentConditionController(db = "RMD")
	public PlatformTransactionManager rmdTransactionManager() {
		return AaaOAPDBConfig.transactionManager(rmdEntityManagerFactory(), rmdDataSource());
	}
}
