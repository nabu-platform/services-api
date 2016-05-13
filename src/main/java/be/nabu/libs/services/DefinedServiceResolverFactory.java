package be.nabu.libs.services;

import java.util.ArrayList;
import java.util.List;

import be.nabu.libs.services.api.DefinedServiceResolver;

public class DefinedServiceResolverFactory {

	private static DefinedServiceResolverFactory instance;
	
	public static DefinedServiceResolverFactory getInstance() {
		if (instance == null)
			instance = new DefinedServiceResolverFactory();
		return instance;
	}
	
	private List<DefinedServiceResolver> resolvers = new ArrayList<DefinedServiceResolver>();
	
	public DefinedServiceResolver getResolver() {
		if (resolvers.isEmpty()) {
			resolvers.add(new SPIDefinedServiceResolver());
		}
		return new MultipleDefinedServiceResolver(resolvers);
	}
	
	public void addResolver(DefinedServiceResolver resolver) {
		resolvers.add(resolver);
	}
	
	public void removeResolver(DefinedServiceResolver resolver) {
		resolvers.remove(resolver);
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
