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
@EnableJpaRepositories(basePackages = "com.optum.ap.services.rest.dao", entityManagerFactoryRef = "caiEntityManager", transactionManagerRef = "caiTransactionManager")
@DBPresentConditionController(db = "CAI")
public class CaiDBConfig {
	
	private static LocalContainerEntityManagerFactoryBean caiEntityManager = null;

	@Bean(name = "caiDataSource", destroyMethod = "")
	@DBPresentConditionController(db = "CAI")
	public DataSource caiDataSource() {
		return AaaOAPDBConfig.setDataSource("CAI");
	}

	@Bean(name = "caiEntityManager")
	@ConfigurationProperties(prefix = "spring.jpa")
	@DBPresentConditionController(db = "CAI")
	public LocalContainerEntityManagerFactoryBean caiEntityManagerFactory() {
		if (caiEntityManager == null) {
			caiEntityManager = AaaOAPDBConfig.entityManagerFactory(caiDataSource(), "CAI",
				new String[] { "com.tpa.ap.domain.cai" });
		}
		return caiEntityManager;
	}

	@Bean(name = "caiTransactionManager")
	@DBPresentConditionController(db = "CAI")
	public PlatformTransactionManager caiTransactionManager() {
		return AaaOAPDBConfig.transactionManager(caiEntityManagerFactory(), caiDataSource());
	}
}
