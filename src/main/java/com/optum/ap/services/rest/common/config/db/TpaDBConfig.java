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
@EnableJpaRepositories(basePackages = { "com.optum.ap.services.rest.dao",
		"com.tpa.mw.tpa.service.ejb.domain.tpa.dao" }, entityManagerFactoryRef = "tpaEntityManager", transactionManagerRef = "tpaTransactionManager")
@DBPresentConditionController(db = "TPA")
public class TpaDBConfig {
	
	private static LocalContainerEntityManagerFactoryBean tpaEntityManager = null;

	@Bean(name = "tpaDataSource", destroyMethod = "")
	@DBPresentConditionController(db = "TPA")
	public DataSource tpaDataSource() {
		return AaaOAPDBConfig.setDataSource("TPA");
	}

	@Bean(name = "tpaEntityManager")
	@ConfigurationProperties(prefix = "spring.jpa")
	@DBPresentConditionController(db = "TPA")
	public LocalContainerEntityManagerFactoryBean tpaEntityManagerFactory() {
		if (tpaEntityManager == null) {
			tpaEntityManager = AaaOAPDBConfig.entityManagerFactory(tpaDataSource(), "TPA",
				new String[] { "com.tpa.mw.tpa.service.ejb.domain", "com.tpa.ap.domain.www.entity", "com.tpa.mw.pss.service.ejb.domain", "com.tpa.mw.key.service.ejb.domain" });
		}
		return tpaEntityManager;
	}

	@Bean(name = "tpaTransactionManager")
	@DBPresentConditionController(db = "TPA")
	public PlatformTransactionManager tpaTransactionManager() {
		return AaaOAPDBConfig.transactionManager(tpaEntityManagerFactory(), tpaDataSource());
	}
}
