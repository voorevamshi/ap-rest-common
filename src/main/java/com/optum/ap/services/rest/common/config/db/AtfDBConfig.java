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
@EnableJpaRepositories(basePackages = "com.optum.ap.services.rest.dao", entityManagerFactoryRef = "atfEntityManager", transactionManagerRef = "atfTransactionManager")
@DBPresentConditionController(db = "ATF")
public class AtfDBConfig {
	
	private static LocalContainerEntityManagerFactoryBean atfEntityManager = null;

	@Bean(name = "atfDataSource", destroyMethod = "")
	@DBPresentConditionController(db = "ATF")
	public DataSource atfDataSource() {
		return AaaOAPDBConfig.setDataSource("ATF");
	}

	@Bean(name = "atfEntityManager")
	@ConfigurationProperties(prefix = "spring.jpa")
	@DBPresentConditionController(db = "ATF")
	public LocalContainerEntityManagerFactoryBean atfEntityManagerFactory() {
		if (atfEntityManager == null) {
			atfEntityManager = AaaOAPDBConfig.entityManagerFactory(atfDataSource(), "ATF",
				new String[] { "com.tpa.ap.domain.atf", "com.tpa.cdhclaims.service.ejb.domain.activitycenter","com.tpa.mw.activitycenter.service.ejb.domain.activitycenter" });
		}
		return atfEntityManager;
	}

	@Bean(name = "atfTransactionManager")
	@DBPresentConditionController(db = "ATF")
	public PlatformTransactionManager atfTransactionManager() {
		return AaaOAPDBConfig.transactionManager(atfEntityManagerFactory(), atfDataSource());
	}
}
