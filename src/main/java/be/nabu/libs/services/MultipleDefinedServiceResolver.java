package be.nabu.libs.services;

import java.util.List;

import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.services.api.DefinedServiceResolver;

public class MultipleDefinedServiceResolver implements DefinedServiceResolver {

	private List<DefinedServiceResolver> resolvers;
	
	public MultipleDefinedServiceResolver(List<DefinedServiceResolver> resolvers) {
		this.resolvers = resolvers;
	}
	
	@Override
	public DefinedService resolve(String id) {
		DefinedService service = null;
		for (DefinedServiceResolver resolver : resolvers) {
			service = resolver.resolve(id);
			if (service != null)
				break;
		}
		return service;
	}

}
