package be.nabu.libs.services.api;

import java.security.Principal;

public interface ExecutionContextProvider {
	public ExecutionContext newExecutionContext(Principal principal);
}
