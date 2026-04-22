package com.optum.ap.services.rest.common.config.db;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder.Builder;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

public class AaaOAPDBConfig {

	protected static final Logger logger = LoggerFactory.getLogger(AaaOAPDBConfig.class);

	public static DataSource setDataSource(String dataSourceName) {
		final JndiDataSourceLookup dsLookup = new JndiDataSourceLookup();
		dsLookup.setResourceRef(true);
		DataSource dataSource = dsLookup.getDataSource("java:comp/env/jdbc/" + dataSourceName);
		Context ctx = ContextHelper.getInstance().getContext();
		try {
			ctx.bind("jdbc/" + dataSourceName, dataSource);
		} catch (NamingException e) {
			logger.error("error in binding " + dataSourceName + " datasource to ctx : ", e);
		}
		return dataSource;
	}

	public static LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource, String dataSourceName, String[] packages) {
		LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
		entityManagerFactoryBean.setDataSource(dataSource);
		entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
		entityManagerFactoryBean.setPersistenceUnitName(dataSourceName.toLowerCase());
		entityManagerFactoryBean.setPackagesToScan(packages);
		return entityManagerFactoryBean;
	}

	public static PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean emfb,
			DataSource dataSource) {
		JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
		jpaTransactionManager.setEntityManagerFactory(emfb.getObject());
		jpaTransactionManager.setDataSource(dataSource);
		return jpaTransactionManager;
	}
}
