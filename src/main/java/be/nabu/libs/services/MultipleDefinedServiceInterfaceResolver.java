package be.nabu.libs.services;

import java.util.List;

import be.nabu.libs.services.api.DefinedServiceInterface;
import be.nabu.libs.services.api.DefinedServiceInterfaceResolver;

public class MultipleDefinedServiceInterfaceResolver implements DefinedServiceInterfaceResolver {

	private List<DefinedServiceInterfaceResolver> resolvers;
	
	public MultipleDefinedServiceInterfaceResolver(List<DefinedServiceInterfaceResolver> resolvers) {
		this.resolvers = resolvers;
	}
	
	@Override
	public DefinedServiceInterface resolve(String id) {
		DefinedServiceInterface service = null;
		for (DefinedServiceInterfaceResolver resolver : resolvers) {
			service = resolver.resolve(id);
			if (service != null) {
				return service;
			}
		}
		return null;
	}

}
