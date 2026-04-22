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
@EnableJpaRepositories(basePackages = { "com.optum.ap.services.rest.dao" }, entityManagerFactoryRef = "ivrEntityManager", transactionManagerRef = "ivrTransactionManager")
//@EnableJpaRepositories(basePackages = { "com.optum.ap.services.rest.dao",
//"com.tpa.mw.tpa.service.ejb.domain.tpa.dao" }, entityManagerFactoryRef = "ivrEntityManager", transactionManagerRef = "ivrTransactionManager")
@DBPresentConditionController(db = "IVR")
public class IvrDBConfig {
	
	private static LocalContainerEntityManagerFactoryBean ivrEntityManager = null;

	@Bean(name = "ivrDataSource", destroyMethod = "")
	@DBPresentConditionController(db = "IVR")
	public DataSource ivrDataSource() {
		return AaaOAPDBConfig.setDataSource("IVR");
	}

	@Bean(name = "ivrEntityManager")
	@ConfigurationProperties(prefix = "spring.jpa")
	@DBPresentConditionController(db = "IVR")
	public LocalContainerEntityManagerFactoryBean ivrEntityManagerFactory() {
		if (ivrEntityManager == null) {
			ivrEntityManager = AaaOAPDBConfig.entityManagerFactory(ivrDataSource(), "IVR",
				new String[] { "com.tpa.mw.tpa.service.ejb.domain.ivr", "com.tpa.mw.ivr.service.ejb.domain" });
		}
		return ivrEntityManager;
	}

	@Bean(name = "ivrTransactionManager")
	@DBPresentConditionController(db = "IVR")
	public PlatformTransactionManager ivrTransactionManager() {
		return AaaOAPDBConfig.transactionManager(ivrEntityManagerFactory(), ivrDataSource());
	}
}
