/**
 * 
 */
package com.optum.ap.services.rest.common.security.stargate;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * @author dgoyal2
 *
 */

public class StargateAuthenticationToken extends AbstractAuthenticationToken {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String username;

	
	
	public StargateAuthenticationToken(String username, Collection<? extends GrantedAuthority> authorities) {
		super(authorities);
		this.username = username;
		setAuthenticated(true);
	}

	public Object getCredentials() {
		return null;
	}

	public Object getPrincipal() {
		return username;
	}
	
}