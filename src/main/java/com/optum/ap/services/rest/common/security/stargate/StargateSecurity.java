package com.optum.ap.services.rest.common.security.stargate;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.optum.ap.services.rest.common.security.dto.StargateAuthUser;
import com.optum.ap.services.rest.common.security.dto.StargateConfigProperties;

/**
 * @author dgoyal2
 *
 */
@Component
public class StargateSecurity {
	private static final Logger logger = Logger.getLogger(StargateSecurity.class);

	@Autowired
	private StargateConfigProperties gateConfigProps;

	@Autowired
	public StargateSecurity(StargateConfigProperties gateConfigProps) {
		this.gateConfigProps = gateConfigProps;
	}

	public boolean hasAuthScope(Authentication authentication) {

		if (authentication != null && authentication instanceof StargateAuthenticationToken) {
//			logger.error("hasAuthScope true");
			return true;
		} else {
//			logger.error("hasAuthScope false");
			return false;
		}
	}

	/**
	 * @param role
	 * @return
	 */
	public boolean hasStargateScope(String[] role) {
		boolean isValid = false;
		String reqUser = "";
		if (SecurityContextHolder.getContext() != null) {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			if (auth != null) {
				reqUser = auth.getPrincipal().toString();
			}
		}
		List<StargateAuthUser> authorizedUser = gateConfigProps.getUsers();

		for (StargateAuthUser authUser : authorizedUser) {
			if (authUser.getName().equalsIgnoreCase(reqUser)) {

				for (String controllerRole : role) {
					if (!isValid) {
						for (String yamlRole : authUser.getRoles()) {

							if (controllerRole.equalsIgnoreCase(yamlRole)) {
								isValid = true;
								break;
							} else {
								continue;
							}

						}
					}

				}
			}
		}
//		logger.error("hasStargateScope: " + isValid);
		return isValid;

	}

}
