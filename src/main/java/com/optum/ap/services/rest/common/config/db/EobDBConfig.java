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
		"com.optum.ap.services.rest.dao" }, entityManagerFactoryRef = "eobEntityManager", transactionManagerRef = "eobTransactionManager")
@DBPresentConditionController(db = "EOB")
public class EobDBConfig {
	
	private static LocalContainerEntityManagerFactoryBean eobEntityManager = null;

	@Bean(name = "eobDataSource", destroyMethod = "")
	@DBPresentConditionController(db = "EOB")
	public DataSource eobDataSource() {
		return AaaOAPDBConfig.setDataSource("EOB");
	}

	@Bean(name = "eobEntityManager")
	@ConfigurationProperties(prefix = "spring.jpa")
	@DBPresentConditionController(db = "EOB")
	public LocalContainerEntityManagerFactoryBean eobEntityManagerFactory() {
		if (eobEntityManager == null) {
			eobEntityManager = AaaOAPDBConfig.entityManagerFactory(eobDataSource(), "EOB",
				new String[] { "com.tpa.ap.domain.eob" });
		}
		return eobEntityManager;
	}

	@Bean(name = "eobTransactionManager")
	@DBPresentConditionController(db = "EOB")
	public PlatformTransactionManager eobTransactionManager() {
		return AaaOAPDBConfig.transactionManager(eobEntityManagerFactory(), eobDataSource());
	}
}
