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
@EnableJpaRepositories(basePackages = { "com.optum.ap.services.rest.dao" },
						entityManagerFactoryRef = "emsRptEntityManager",
						transactionManagerRef = "emsRptTransactionManager")
@DBPresentConditionController(db = "EMSRpt")
public class EmsRptDBConfig {
	
	private static LocalContainerEntityManagerFactoryBean emsRptEntityManager = null;

	@Bean(name = "emsRptDataSource", destroyMethod = "")
	@DBPresentConditionController(db = "EMSRpt")
	public DataSource emsRptDataSource() {
		return AaaOAPDBConfig.setDataSource("EMSRpt");
	}

	@Bean(name = "emsRptEntityManager")
	@ConfigurationProperties(prefix = "spring.jpa")
	@DBPresentConditionController(db = "EMSRpt")
	public LocalContainerEntityManagerFactoryBean emsRptEntityManagerFactory() {
		if (emsRptEntityManager == null) {
			emsRptEntityManager = AaaOAPDBConfig.entityManagerFactory(emsRptDataSource(), "EMSRpt",
				new String[] { "com.tpa.ap.domain.ems" });
		}
		return emsRptEntityManager;
	}

	@Bean(name = "emsRptTransactionManager")
	@DBPresentConditionController(db = "EMSRpt")
	public PlatformTransactionManager emsRptTransactionManager() {
		return AaaOAPDBConfig.transactionManager(emsRptEntityManagerFactory(), emsRptDataSource());
	}
}
