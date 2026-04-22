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
@EnableJpaRepositories(basePackages = {"com.optum.ap.services.rest.dao", "com.optum.ap.services.rest.opd3.repository"}, entityManagerFactoryRef = "gpdEntityManager", transactionManagerRef = "gpdTransactionManager")
@DBPresentConditionController(db = "GPD")
public class GpdDBConfig {
	
	private static LocalContainerEntityManagerFactoryBean gpdEntityManager = null;

	@Bean(name = "gpdDataSource", destroyMethod = "")
	@DBPresentConditionController(db = "GPD")
	public DataSource gpdDataSource() {
		return AaaOAPDBConfig.setDataSource("GPD");
	}

	@Bean(name = "gpdEntityManager")
	@ConfigurationProperties(prefix = "spring.jpa")
	@DBPresentConditionController(db = "GPD")
	public LocalContainerEntityManagerFactoryBean gpdEntityManagerFactory() {
		if (gpdEntityManager == null) {
			gpdEntityManager = AaaOAPDBConfig.entityManagerFactory(gpdDataSource(), "GPD",
				new String[] { "com.tpa.ap.domain.opd3" });
		}
		return gpdEntityManager;
	}

	@Bean(name = "gpdTransactionManager")
	@DBPresentConditionController(db = "GPD")
	public PlatformTransactionManager gpdTransactionManager() {
		return AaaOAPDBConfig.transactionManager(gpdEntityManagerFactory(), gpdDataSource());
	}
}
