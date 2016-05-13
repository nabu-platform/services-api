package be.nabu.libs.services;

import java.util.ArrayList;
import java.util.List;

import be.nabu.libs.services.api.DefinedServiceInterfaceResolver;

public class DefinedServiceInterfaceResolverFactory {

	private static DefinedServiceInterfaceResolverFactory instance;
	private DefinedServiceInterfaceResolver resolver;
	
	public static DefinedServiceInterfaceResolverFactory getInstance() {
		if (instance == null)
			instance = new DefinedServiceInterfaceResolverFactory();
		return instance;
	}
	
	private List<DefinedServiceInterfaceResolver> resolvers = new ArrayList<DefinedServiceInterfaceResolver>();
	
	public DefinedServiceInterfaceResolver getResolver() {
		if (resolver == null) {
			synchronized(this) {
				if (resolver == null) {
					if (resolvers.isEmpty()) {
						resolvers.add(new SPIDefinedServiceInterfaceResolver());
					}
				}
				resolver = new MultipleDefinedServiceInterfaceResolver(resolvers);
			}
		}
		return resolver;
	}
	
	public void addResolver(DefinedServiceInterfaceResolver resolver) {
		resolvers.add(resolver);
		this.resolver = null;
	}
	
	public void removeResolver(DefinedServiceInterfaceResolver resolver) {
		resolvers.remove(resolver);
		this.resolver = null;
	}
	
	@SuppressWarnings("unused")
	private void activate() {
		instance = this;
	}
	@SuppressWarnings("unused")
	private void deactivate() {
		instance = null;
	}
}
