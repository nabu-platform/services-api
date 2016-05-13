package be.nabu.libs.services.api;

import be.nabu.libs.artifacts.api.ArtifactResolver;

/**
 * The service resolver can go very far
 * You can introduce an execution context with security and only resolve if the user has access to it for example
 */
public interface DefinedServiceResolver extends ArtifactResolver<DefinedService> {
	@Override
	public DefinedService resolve(String id);
}
