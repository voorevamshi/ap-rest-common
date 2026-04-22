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
@EnableJpaRepositories(basePackages = {"com.optum.ap.services.rest.dao","com.optum.ap.services.rest.aos.repository"}, entityManagerFactoryRef = "aosEntityManager", transactionManagerRef = "aosTransactionManager")
@DBPresentConditionController(db = "AOS")
public class AosDBConfig {
	
	private static LocalContainerEntityManagerFactoryBean aosEntityManager = null;

	@Bean(name = "aosDataSource", destroyMethod = "")
	@DBPresentConditionController(db = "AOS")
	public DataSource aosDataSource() {
		return AaaOAPDBConfig.setDataSource("AOS");
	}

	@Bean(name = "aosEntityManager")
	@ConfigurationProperties(prefix = "spring.jpa")
	@DBPresentConditionController(db = "AOS")
	public LocalContainerEntityManagerFactoryBean aosEntityManagerFactory() {
		if (aosEntityManager == null) {
			aosEntityManager = AaaOAPDBConfig.entityManagerFactory(aosDataSource(), "AOS",
				new String[] { "com.tpa.ap.domain.aos", "com.tpa.cdhclaims.service.ejb.domain.aos", "com.tpa.mw.tpa.service.ejb.domain" });
		}
		return aosEntityManager;
	}

	@Bean(name = "aosTransactionManager")
	@DBPresentConditionController(db = "AOS")
	public PlatformTransactionManager aosTransactionManager() {
		return AaaOAPDBConfig.transactionManager(aosEntityManagerFactory(), aosDataSource());
	}
}
