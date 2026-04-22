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
@EnableJpaRepositories(basePackages = "com.optum.ap.services.rest.dao", entityManagerFactoryRef = "webEntityManager", transactionManagerRef = "webTransactionManager")
@DBPresentConditionController(db = "WEB")
public class WebDBConfig {
	
	private static LocalContainerEntityManagerFactoryBean webEntityManager = null;

	@Bean(name = "webDataSource", destroyMethod = "")
	@DBPresentConditionController(db = "WEB")
	public DataSource webDataSource() {
		return AaaOAPDBConfig.setDataSource("WEB");
	}

	@Bean(name = "webEntityManager")
	@ConfigurationProperties(prefix = "spring.jpa")
	@DBPresentConditionController(db = "WEB")
	public LocalContainerEntityManagerFactoryBean webEntityManagerFactory() {
		if (webEntityManager == null) {
			webEntityManager = AaaOAPDBConfig.entityManagerFactory(webDataSource(), "WEB",
				new String[] { "com.tpa.ap.domain.web" });
		}
		return webEntityManager;
	}

	@Bean(name = "webTransactionManager")
	@DBPresentConditionController(db = "WEB")
	public PlatformTransactionManager webTransactionManager() {
		return AaaOAPDBConfig.transactionManager(webEntityManagerFactory(), webDataSource());
	}
}
