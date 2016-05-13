package be.nabu.libs.services;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.services.api.DefinedServiceResolver;

public class SPIDefinedServiceResolver implements DefinedServiceResolver {

	private static DefinedServiceResolver resolver;
	
	@Override
	public DefinedService resolve(String id) {
		return getResolver().resolve(id);
	}

	private DefinedServiceResolver getResolver() {
		if (resolver == null) {
			synchronized(this) {
				if (resolver == null) {
					List<DefinedServiceResolver> resolvers = new ArrayList<DefinedServiceResolver>();
					ServiceLoader<DefinedServiceResolver> serviceLoader = ServiceLoader.load(DefinedServiceResolver.class);
					for (DefinedServiceResolver resolver : serviceLoader) {
						resolvers.add(resolver);
					}
					resolver = new MultipleDefinedServiceResolver(resolvers);
				}
			}
		}
		return resolver;
	}
}
