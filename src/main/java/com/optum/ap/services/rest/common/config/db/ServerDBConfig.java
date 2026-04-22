package com.optum.ap.services.rest.common.config.db;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.apache.catalina.Context;
import org.apache.log4j.Logger;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.tomcat.TomcatWebServer;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.servlet.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import com.optum.ap.services.rest.common.config.db.dto.DBDefinition;
import com.optum.ap.services.rest.common.config.db.dto.DbSettings;
import com.tpa.ap.common.service.util.OAPVariableManager;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties
@Conditional(value = { ServerDBPresentCondition.class })
public class ServerDBConfig {
	private static final String AP_SECRET_VAR_KEY = "${ap.secrets.";

	@Autowired
	private ConfigurationPropYaml configProps;

	@Autowired(required = false)
	private DbSettings dbProps;
	
	
	@Bean
	public EntityManagerFactoryBuilder entityManagerFactoryBuilder() {
		Map<String, Object> jpaProperties = new HashMap<>();
		return new EntityManagerFactoryBuilder(new HibernateJpaVendorAdapter(), (dataSource) -> jpaProperties, null);
	}

	@Bean
	public ServletWebServerFactory servletContainer() {
		TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
			final Logger logger = Logger.getLogger(this.getClass());
			@Override
			protected TomcatWebServer getTomcatWebServer(org.apache.catalina.startup.Tomcat tomcat) {
				tomcat.enableNaming();
				return super.getTomcatWebServer(tomcat);
			}

			@Override
			protected void postProcessContext(Context context) {
				((StandardJarScanner) context.getJarScanner()).setScanManifest(false);
				if (dbProps == null) {
					dbProps = new DbSettings();
				}

				List<String> dbList = configProps.getDatabaseList();
				for (String db : dbList) {
					DBDefinition dbDef = dbProps.getDatabases().get(db.trim());
					if (dbDef == null) {
						populateDbSettings();
						dbDef = dbProps.getDatabases().get(db.trim());
						if (dbDef == null) {
							logger.error("Cannot define database " + db);
							continue;
						}
					}

					Map<String, String> properties = dbDef.getDatasource();
					if (properties != null) {
						ContextResource dbResource = new ContextResource();
						//TODO-remove when fully converted to Hikari in all platforms
						dbResource.setProperty("factory", "org.apache.tomcat.jdbc.pool.DataSourceFactory");
						setContextResource(dbResource, "defaults");
						setContextResource(dbResource, db.trim());
						dbResource.setName(properties.get("name"));
						dbResource.setType(DataSource.class.getName());
						context.getNamingResources().addResource(dbResource);
						logger.info("Database defined: " + db.trim());
					}
				}
			}
		};
		return tomcat;
	}
	private void setContextResource(ContextResource dbResource, String dbName) {
		DBDefinition dbDef = dbProps.getDatabases().get(dbName);
		if (dbDef == null) return;
		Map<String, String> properties = dbDef.getDatasource();
		if (properties == null) return;
		List<String> dbKeys = new ArrayList<>(Arrays.asList(dbProps.getDataBaseAttrs().split(",")));
		for (String dbKey : dbKeys) {
			String value = properties.get(dbKey.trim());
			if (value != null) {
				dbResource.setProperty(dbKey.trim(), value.trim());
			}
		}
	}
	private void populateDbSettings() {
		final Logger logger = Logger.getLogger(this.getClass());
		String fileName = "application-" + System.getProperty("environment") + ".yaml";
		try {
			YamlPropertySourceLoader yamlProperyLoader = new YamlPropertySourceLoader();
			List<PropertySource<?>> propertySources = yamlProperyLoader.load("properties",
					new ClassPathResource(fileName));
			for (PropertySource<?> propertySource : propertySources) {
				Map<?, ?> sourceData = ((MapPropertySource) propertySource).getSource();
				for (Entry<?, ?> entry : sourceData.entrySet()) {
					String[] keyParts = entry.getKey().toString().split("\\.");
					if (keyParts.length < 3) {
						if (entry.getKey().equals("dataBaseKeys")) {
							dbProps.setDataBaseAttrs(varReplace(entry.getValue().toString()));
						}
					} else if (keyParts.length == 3) {
						if (dbProps.getDatabases().get(keyParts[0]) == null) {
							DBDefinition dbDef = new DBDefinition();
							dbDef.setDatasource(new HashMap<String, String>());
							dbProps.getDatabases().put(keyParts[0], dbDef);
							
						}
						if (keyParts[1].equalsIgnoreCase("datasource")) {
							dbProps.getDatabases().get(keyParts[0]).getDatasource().put(keyParts[2], varReplace(entry.getValue().toString()));
						}
					}
				}
			}
		} catch (FileNotFoundException ex) {
			logger.error("Unable to open " + fileName);
			return;
		} catch (Exception ex) {
			logger.error("Exception " + ex.getMessage() + " for " + fileName);
			return;
		}
	}
	// If you need to make a change here, make sure a corresponding change in ap-rest-client->ClientServicePropertiesV2.getProperty(String, String)
	private String varReplace(String value) {
		while ((value != null) && (value.contains(AP_SECRET_VAR_KEY))) {
			int start = value.indexOf(AP_SECRET_VAR_KEY);
			int endpt = value.indexOf('}');
			String secretName = value.substring(start + AP_SECRET_VAR_KEY.length(), endpt);
			if (secretName.contains(":")) {
				String[] parts = secretName.split(":");
				value = parts[1];
				secretName = parts[0];
			}
			String secret = OAPVariableManager.getVariable(secretName);
			if (secret != null) {
				value = value.replace(AP_SECRET_VAR_KEY + secretName + "}", secret);
			} else {
				break;		// Don't keep processing missing values
			}
		}
		return value;
	}
}
