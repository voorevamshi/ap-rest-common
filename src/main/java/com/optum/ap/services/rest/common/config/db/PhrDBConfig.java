/**
 * 
 */
package com.optum.ap.services.rest.common.config.db;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author amahesh9
 * Configuration - setting up a JPA based database connection.
 */

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.optum.ap.services.rest.dao", entityManagerFactoryRef = "phrEntityManager", transactionManagerRef = "phrTransactionManager")
@DBPresentConditionController(db = "PHR")
public class PhrDBConfig {
	
	private static LocalContainerEntityManagerFactoryBean phrEntityManager = null;

	@Bean(name = "phrDataSource", destroyMethod = "")
	@DBPresentConditionController(db = "PHR")
	public DataSource phrDataSource() {
		return AaaOAPDBConfig.setDataSource("PHR");
	}

	@Bean(name = "phrEntityManager")
	@ConfigurationProperties(prefix = "spring.jpa")
	@DBPresentConditionController(db = "PHR")
	public LocalContainerEntityManagerFactoryBean phrEntityManagerFactory() {
		if (phrEntityManager == null) {
			phrEntityManager = AaaOAPDBConfig.entityManagerFactory(phrDataSource(), "PHR",
				new String[] { "com.ap.domain.phr" });
		}
		return phrEntityManager;
	}

	@Bean(name = "phrTransactionManager")
	@DBPresentConditionController(db = "PHR")
	public PlatformTransactionManager phrTransactionManager() {
		return AaaOAPDBConfig.transactionManager(phrEntityManagerFactory(), phrDataSource());
	}
}
