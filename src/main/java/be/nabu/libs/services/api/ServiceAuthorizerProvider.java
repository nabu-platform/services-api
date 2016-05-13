package be.nabu.libs.services.api;

import be.nabu.libs.services.ServiceRuntime;

public interface ServiceAuthorizerProvider {
	public ServiceAuthorizer getAuthorizer(ServiceRuntime runtime);
}
