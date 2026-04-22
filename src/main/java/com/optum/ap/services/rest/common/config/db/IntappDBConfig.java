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
@EnableJpaRepositories(basePackages = "com.optum.ap.services.rest.dao", entityManagerFactoryRef = "intappEntityManager", transactionManagerRef = "intappTransactionManager")
@DBPresentConditionController(db = "INTAPP")
public class IntappDBConfig {
	
	private static LocalContainerEntityManagerFactoryBean intappEntityManager = null;

	@Bean(name = "intappDataSource", destroyMethod = "")
	@DBPresentConditionController(db = "INTAPP")
	public DataSource intappDataSource() {
		return AaaOAPDBConfig.setDataSource("INTAPP");
	}

	@Bean(name = "intappEntityManager")
	@ConfigurationProperties(prefix = "spring.jpa")
	@DBPresentConditionController(db = "INTAPP")
	public LocalContainerEntityManagerFactoryBean intappEntityManagerFactory() {
		if (intappEntityManager == null) {
			intappEntityManager = AaaOAPDBConfig.entityManagerFactory(intappDataSource(), "INTAPP",
				new String[] { "com.tpa.ap.domain.intapp.entity" });
		}
		return intappEntityManager;
	}

	@Bean(name = "intappTransactionManager")
	@DBPresentConditionController(db = "INTAPP")
	public PlatformTransactionManager intappTransactionManager() {
		return AaaOAPDBConfig.transactionManager(intappEntityManagerFactory(), intappDataSource());
	}
}
