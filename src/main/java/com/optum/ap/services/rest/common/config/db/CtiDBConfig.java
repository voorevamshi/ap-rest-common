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
		"com.optum.ap.services.rest.dao" }, entityManagerFactoryRef = "ctiEntityManager", transactionManagerRef = "ctiTransactionManager")
@DBPresentConditionController(db = "CTI")
public class CtiDBConfig {
	
	private static LocalContainerEntityManagerFactoryBean ctiEntityManager = null;

	@Bean(name = "ctiDataSource", destroyMethod = "")
	@DBPresentConditionController(db = "CTI")
	public DataSource ctiDataSource() {
		return AaaOAPDBConfig.setDataSource("CTI");
	}

	@Bean(name = "ctiEntityManager")
	@ConfigurationProperties(prefix = "spring.jpa")
	@DBPresentConditionController(db = "CTI")
	public LocalContainerEntityManagerFactoryBean ctiEntityManagerFactory() {
		if (ctiEntityManager == null) {
			ctiEntityManager = AaaOAPDBConfig.entityManagerFactory(ctiDataSource(), "CTI",
				new String[] { "com.tpa.mw.tpa.service.ejb.domain.cti", "com.tpa.mw.cti.service.ejb.domain" });
		}
		return ctiEntityManager;
	}

	@Bean(name = "ctiTransactionManager")
	@DBPresentConditionController(db = "CTI")
	public PlatformTransactionManager ctiTransactionManager() {
		return AaaOAPDBConfig.transactionManager(ctiEntityManagerFactory(), ctiDataSource());
	}
}
