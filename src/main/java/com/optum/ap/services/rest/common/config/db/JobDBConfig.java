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
@EnableJpaRepositories(basePackages = {"com.optum.ap.services.rest.dao"}, entityManagerFactoryRef = "jobEntityManager", transactionManagerRef = "jobTransactionManager")
@DBPresentConditionController(db = "JOB")
public class JobDBConfig {
	
	private static LocalContainerEntityManagerFactoryBean jobEntityManager = null;

	@Bean(name = "jobDataSource", destroyMethod = "")
	@DBPresentConditionController(db = "JOB")
	public DataSource jobDataSource() {
		return AaaOAPDBConfig.setDataSource("JOB");
	}

	@Bean(name = "jobEntityManager")
	@ConfigurationProperties(prefix = "spring.jpa")
	@DBPresentConditionController(db = "JOB")
	public LocalContainerEntityManagerFactoryBean jobEntityManagerFactory() {
		if (jobEntityManager == null) {
			jobEntityManager = AaaOAPDBConfig.entityManagerFactory(jobDataSource(), "JOB",
				new String[] { "com.tpa.ap.domain.job" });
		}
		return jobEntityManager;
	}

	@Bean(name = "jobTransactionManager")
	@DBPresentConditionController(db = "JOB")
	public PlatformTransactionManager jobTransactionManager() {
		return AaaOAPDBConfig.transactionManager(jobEntityManagerFactory(), jobDataSource());
	}
}
