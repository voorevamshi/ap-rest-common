/**
 * 
 */
package com.optum.ap.services.rest.service.controller;

import java.lang.Package;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import jakarta.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import com.tpa.ap.common.service.util.configuration.ConfigurationBuilder;
import com.tpa.ap.common.service.util.configuration.configs.Configuration;
import com.tpa.ap.domain.aps.entity.Aps0036t;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

/**
 * @author mgenoj & jsturde1
 *
 */
@PreAuthorize("@basicSecurity.hasAuthScope(authentication)")
@SecurityRequirement(name = "basicAuth")
public abstract class BaseController {

	private Logger logger = Logger.getLogger(BaseController.class.getName());

	private static final String[] DISALLOWED_FILEDS = new String[0];

	@Autowired(required=false)
	@Qualifier("apsEntityManager")
	private EntityManager apsEntityManager;

	private static final String UNKNOWN_SERVICE_INFO = "UNKNOWN";
	public static final String PROP_SYSTEM_SERVER_IMPLEMENTATION = "server.implementation";
	public static final String PROP_SYSTEM_SERVER_HOSTNAME = "server.hostname";
	public static final String PROP_SYSTEM_SERVER_INSTANCE = "server.instance";
	public static final String SYSTEM_PROPS_FILE = "system-environment.properties";

	private static String serviceVersion = null;
	private static String serviceProvider = null;
	private String serviceSiteCd = null;

	public BaseController() {
		setServiceProviderAndVersion();
	}
	/**
	 * Subclasses that make use of the methods provided by this base class
	 * should override this method to return a Logger initialized
	 * 
	 * @return
	 */
	protected Logger getLogger() {
		return logger;
	}

	/*
	 * Send the response back as OK or error or any other status with wither the
	 * message or the actual response
	 */
	@SuppressWarnings("unchecked")
	protected <T> ResponseEntity<T> returnResponse(HttpStatus status, Object entity) {
		return (ResponseEntity<T>) ResponseEntity.status(status).body(entity);
	}

	@InitBinder
	public void allowedInitBinder(WebDataBinder binder) {
		binder.setDisallowedFields(DISALLOWED_FILEDS);
	}

	public static String getServiceVersion() {
		return serviceVersion;
	}

	public static void setServiceVersion(String serviceVersion) {
		BaseController.serviceVersion = serviceVersion;
	}

	public void setServiceProviderAndVersion() {
		Package pack = this.getClass().getPackage();
		serviceVersion = pack.getImplementationVersion();

		if (System.getProperties().containsKey(PROP_SYSTEM_SERVER_IMPLEMENTATION)) {
			serviceProvider = System.getProperty(PROP_SYSTEM_SERVER_IMPLEMENTATION);
		} else {
			serviceProvider = "unknown";
		}
		if (System.getProperties().containsKey(PROP_SYSTEM_SERVER_HOSTNAME)) {
			serviceProvider += "." + System.getProperty(PROP_SYSTEM_SERVER_HOSTNAME);
		} else {
			try {
				serviceProvider += "." + InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e) {
				serviceProvider += ".unknown";
			}
		}
		if (System.getProperties().containsKey(PROP_SYSTEM_SERVER_HOSTNAME)) {
			serviceProvider += "." + System.getProperty(PROP_SYSTEM_SERVER_HOSTNAME);
		} else {
		}
		if (System.getProperties().containsKey(PROP_SYSTEM_SERVER_INSTANCE)) {
			serviceProvider += "." + System.getProperty(PROP_SYSTEM_SERVER_INSTANCE);
		} else {
			serviceProvider += ".unknown";
		}
	}

	public static String getServiceProvider() {
		return serviceProvider;
	}

	public static void setServiceProvider(String serviceProvider) {
		BaseController.serviceProvider = serviceProvider;
	}

	private void loadPlatformSite() {
		try {
			String platform = System.getProperty("server.implementation"); /* umr allsavers vaccn etc */
			if (apsEntityManager == null) {
				setServiceSiteCd("??");
				return;
			}
			ArrayList<Aps0036t> sites = (ArrayList<Aps0036t>) apsEntityManager.createNamedQuery("Aps0036t.getSites").getResultList();

			String env = System.getProperty("environment");
			if (env.equals("local")) env = "test";
			Configuration globalConfig = ConfigurationBuilder.createConfiguration("Global", null, null, null, null, null, null);
			ArrayList<String> hosts = globalConfig.getProperty("domainNameList-" + env);

			for (Aps0036t site : sites) {
				Configuration config = ConfigurationBuilder.createConfiguration(null, site.getSiteCd(), null, null, null, null, null);

				ArrayList<String> domains = config.getProperty("domain.base");
				if (domains != null) {
					for (String domainBase : domains) {
						if (domainBase.contains(platform)) {
							for (String hostValue : hosts) {
								if (hostValue.contains(domainBase)) {
									String[] hostParams = hostValue.split(";");
									if (hostParams.length > 2) {
										setServiceSiteCd(hostParams[2]);
										return;
									}
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error in loadPlatformSite :" + e.getMessage());
		}
	}

	public String getServiceSiteCd() {
		if (serviceSiteCd == null) {
			loadPlatformSite();
		}
		return serviceSiteCd;
	}
	public void setServiceSiteCd(String serviceSiteCd) {
		this.serviceSiteCd = serviceSiteCd;
	}
}
