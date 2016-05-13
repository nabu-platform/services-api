package be.nabu.libs.services;

import java.util.Collection;

import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.services.api.ServiceContext;

public interface ListableServiceContext extends ServiceContext {
	public <T extends Artifact> Collection<T> getArtifacts(Class<T> artifactType);
}
