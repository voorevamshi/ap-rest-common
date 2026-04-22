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
@EnableJpaRepositories(basePackages = "com.optum.ap.services.rest.dao", entityManagerFactoryRef = "iproEntityManager", transactionManagerRef = "iproTransactionManager")
@DBPresentConditionController(db = "IPRO")
public class IproDBConfig {
	
	private static LocalContainerEntityManagerFactoryBean iproEntityManager = null;

	@Bean(name = "iproDataSource", destroyMethod = "")
	@DBPresentConditionController(db = "IPRO")
	public DataSource iproDataSource() {
		return AaaOAPDBConfig.setDataSource("IPRO");
	}

	@Bean(name = "iproEntityManager")
	@ConfigurationProperties(prefix = "spring.jpa")
	@DBPresentConditionController(db = "IPRO")
	public LocalContainerEntityManagerFactoryBean iproEntityManagerFactory() {
		if (iproEntityManager == null) {
			iproEntityManager = AaaOAPDBConfig.entityManagerFactory(iproDataSource(), "IPRO",
				new String[] { "com.tpa.ap.domain.ipro", "com.tpa.cdhclaims.service.ejb.domain.ipro" });
		}
		return iproEntityManager;
	}

	@Bean(name = "iproTransactionManager")
	@DBPresentConditionController(db = "IPRO")
	public PlatformTransactionManager iproTransactionManager() {
		return AaaOAPDBConfig.transactionManager(iproEntityManagerFactory(), iproDataSource());
	}
}
