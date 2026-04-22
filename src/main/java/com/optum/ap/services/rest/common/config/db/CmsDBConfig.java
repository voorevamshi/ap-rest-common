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
@EnableJpaRepositories(basePackages = "com.optum.ap.services.rest.dao", entityManagerFactoryRef = "cmsEntityManager", transactionManagerRef = "cmsTransactionManager")
@DBPresentConditionController(db = "CMS")
public class CmsDBConfig {
	
	private static LocalContainerEntityManagerFactoryBean cmsEntityManager = null;

	@Bean(name = "cmsDataSource", destroyMethod = "")
	@DBPresentConditionController(db = "CMS")
	public DataSource cmsDataSource() {
		return AaaOAPDBConfig.setDataSource("CMS");
	}

	@Bean(name = "cmsEntityManager")
	@ConfigurationProperties(prefix = "spring.jpa")
	@DBPresentConditionController(db = "CMS")
	public LocalContainerEntityManagerFactoryBean cmsEntityManagerFactory() {
		if (cmsEntityManager == null) {
			cmsEntityManager = AaaOAPDBConfig.entityManagerFactory(cmsDataSource(), "CMS",
				new String[] { "com.tpa.ap.domain.cms","com.tpa.ap.domain.swa" });
		}
		return cmsEntityManager;
	}

	@Bean(name = "cmsTransactionManager")
	@DBPresentConditionController(db = "CMS")
	public PlatformTransactionManager cmsTransactionManager() {
		return AaaOAPDBConfig.transactionManager(cmsEntityManagerFactory(), cmsDataSource());
	}
}
