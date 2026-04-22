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
@EnableJpaRepositories(basePackages = "com.optum.ap.services.rest.dao", entityManagerFactoryRef = "vaEntityManager", transactionManagerRef = "vaTransactionManager")
@DBPresentConditionController(db = "VADR")
public class VADBConfig {
	
	private static LocalContainerEntityManagerFactoryBean vaEntityManager = null;

	@Bean(name = "vaDataSource", destroyMethod = "")
	@DBPresentConditionController(db = "VA")
	public DataSource vaDataSource() {
		return AaaOAPDBConfig.setDataSource("VA");
	}

	@Bean(name = "vaEntityManager")
	@ConfigurationProperties(prefix = "spring.jpa")
	@DBPresentConditionController(db = "VA")
	public LocalContainerEntityManagerFactoryBean vaEntityManagerFactory() {
		if (vaEntityManager == null) {
			vaEntityManager = AaaOAPDBConfig.entityManagerFactory(vaDataSource(), "VA",
				new String[] { "com.tpa.ap.domain.va" });
		}
		return vaEntityManager;
	}

	@Bean(name = "vaTransactionManager")
	@DBPresentConditionController(db = "VA")
	public PlatformTransactionManager vaTransactionManager() {
		return AaaOAPDBConfig.transactionManager(vaEntityManagerFactory(), vaDataSource());
	}
}
