package be.nabu.libs.services.api;

import be.nabu.libs.types.api.ComplexContent;

public interface ServiceInstance {
	public Service getDefinition();
	public ComplexContent execute(ExecutionContext executionContext, ComplexContent input) throws ServiceException;
}
