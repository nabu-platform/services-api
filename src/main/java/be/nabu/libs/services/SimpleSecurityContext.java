package be.nabu.libs.services;

import java.security.Principal;

import be.nabu.libs.services.api.SecurityContext;

public class SimpleSecurityContext implements SecurityContext {

	private Principal principal;
	
	public SimpleSecurityContext(Principal principal) {
		this.principal = principal;
	}
	@Override
	public Principal getPrincipal() {
		return principal;
	}
}
