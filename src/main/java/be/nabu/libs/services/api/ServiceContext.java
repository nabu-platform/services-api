package be.nabu.libs.services.api;

import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.artifacts.api.ArtifactResolver;
import be.nabu.libs.cache.api.CacheProvider;
import be.nabu.libs.services.ServiceRunnerFactory;

public interface ServiceContext {
	public <T extends Artifact> ArtifactResolver<T> getResolver(Class<T> artifactType);
	public CacheProvider getCacheProvider();
	public ServiceRuntimeTrackerProvider getServiceTrackerProvider();
	public ServiceAuthorizerProvider getServiceAuthorizerProvider();
	public default ServiceRunner getServiceRunner() {
		return ServiceRunnerFactory.getInstance().getServiceRunner();
	}
}
