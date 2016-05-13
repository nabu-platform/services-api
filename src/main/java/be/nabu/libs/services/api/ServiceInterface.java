package be.nabu.libs.services.api;

import be.nabu.libs.types.api.ComplexType;

public interface ServiceInterface {
	public ComplexType getInputDefinition();
	public ComplexType getOutputDefinition();
	public ServiceInterface getParent();
}
