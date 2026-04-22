package com.optum.ap.services.rest.common.config.db;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Component
@Conditional(value = { ServerDBPresentCondition.class })
public class DBConfigBeanHelper implements BeanDefinitionRegistryPostProcessor {

	private static final String PRIMARY_DB_SOURCE_BEAN = "apsDataSource";
	private static final String PRIMARY_EM_BEAN = "apsEntityManager";
	private static final String PRIMARY_TRANS_BEAN = "apsTransactionManager";

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) {
		if (configurableListableBeanFactory.containsBean(PRIMARY_DB_SOURCE_BEAN)) {
			configurableListableBeanFactory.getBeanDefinition(PRIMARY_DB_SOURCE_BEAN).setPrimary(true);
			configurableListableBeanFactory.getBeanDefinition(PRIMARY_EM_BEAN).setPrimary(true);
			configurableListableBeanFactory.getBeanDefinition(PRIMARY_TRANS_BEAN).setPrimary(true);
		}
	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry arg0) {
		// do nothing
	}
}
