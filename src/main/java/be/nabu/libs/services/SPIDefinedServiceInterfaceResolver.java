package be.nabu.libs.services;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import be.nabu.libs.services.api.DefinedServiceInterface;
import be.nabu.libs.services.api.DefinedServiceInterfaceResolver;

public class SPIDefinedServiceInterfaceResolver implements DefinedServiceInterfaceResolver {

	private static DefinedServiceInterfaceResolver resolver;
	
	@Override
	public DefinedServiceInterface resolve(String id) {
		return getResolver().resolve(id);
	}

	private DefinedServiceInterfaceResolver getResolver() {
		if (resolver == null) {
			synchronized(this) {
				if (resolver == null) {
					List<DefinedServiceInterfaceResolver> resolvers = new ArrayList<DefinedServiceInterfaceResolver>();
					ServiceLoader<DefinedServiceInterfaceResolver> serviceLoader = ServiceLoader.load(DefinedServiceInterfaceResolver.class);
					for (DefinedServiceInterfaceResolver resolver : serviceLoader) {
						resolvers.add(resolver);
					}
					resolver = new MultipleDefinedServiceInterfaceResolver(resolvers);
				}
			}
		}
		return resolver;
	}
}
