package be.nabu.libs.services.api;

import java.security.Principal;

public interface SecurityContext {
	public Principal getPrincipal();
}
