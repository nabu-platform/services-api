package be.nabu.libs.services.api;

import be.nabu.libs.services.ServiceRuntime;
import be.nabu.libs.types.api.ComplexContent;

public interface ServiceAuthorizer {
	public boolean canRun(ServiceRuntime runtime, ComplexContent input);
}
