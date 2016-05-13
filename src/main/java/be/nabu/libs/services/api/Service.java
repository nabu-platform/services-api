package be.nabu.libs.services.api;

import java.util.Set;

public interface Service {
	public ServiceInterface getServiceInterface();
	public ServiceInstance newInstance();
	
	/**
	 * This should return a list of reference ids
	 * This should contain all the things that are resolved through "a" resolver
	 * Currently there is no distinction in what you are actually resolving (e.g. a service, a type,...)
	 * This is necessary to build a dependency map to calculate impact and update changes
	 * For types this is less necessary because they are traversable, services are entirely opaque though
	 * Note that this is now performed by the manager
	 */
	@Deprecated
	public Set<String> getReferences();
}
