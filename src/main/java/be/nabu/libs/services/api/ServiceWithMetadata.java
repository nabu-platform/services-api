package be.nabu.libs.services.api;

import java.util.List;

import be.nabu.libs.artifacts.api.ExceptionDescription;

@Deprecated
public interface ServiceWithMetadata {
	public List<ExceptionDescription> getExceptions(ServiceContext context);
}
