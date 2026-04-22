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
@EnableJpaRepositories(basePackages = "com.optum.ap.services.rest.dao", entityManagerFactoryRef = "bassEntityManager", transactionManagerRef = "bassTransactionManager")
@DBPresentConditionController(db = "BASS")
public class BassDBConfig {
	
	private static LocalContainerEntityManagerFactoryBean bassEntityManager = null;

	@Bean(name = "bassDataSource", destroyMethod = "")
	@DBPresentConditionController(db = "BASS")
	public DataSource bassDataSource() {
		return AaaOAPDBConfig.setDataSource("BASS");
	}

	@Bean(name = "bassEntityManager")
	@ConfigurationProperties(prefix = "spring.jpa")
	@DBPresentConditionController(db = "BASS")
	public LocalContainerEntityManagerFactoryBean bassEntityManagerFactory() {
		if (bassEntityManager == null) {
			bassEntityManager = AaaOAPDBConfig.entityManagerFactory(bassDataSource(), "BASS",
				new String[] { "com.tpa.ap.domain.bass", "com.tpa.mw.bass.service.ejb.domain" });
		}
		return bassEntityManager;
	}

	@Bean(name = "bassTransactionManager")
	@DBPresentConditionController(db = "BASS")
	public PlatformTransactionManager bassTransactionManager() {
		return AaaOAPDBConfig.transactionManager(bassEntityManagerFactory(), bassDataSource());
	}
}
