package be.nabu.libs.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import be.nabu.libs.services.api.DefinedServiceInterface;
import be.nabu.libs.services.api.DefinedServiceInterfaceResolver;

public class MultipleDefinedServiceInterfaceResolver implements DefinedServiceInterfaceResolver {

	private List<DefinedServiceInterfaceResolver> resolvers;
	private Map<String, DefinedServiceInterface> ifaces = new HashMap<String, DefinedServiceInterface>();
	
	public MultipleDefinedServiceInterfaceResolver(List<DefinedServiceInterfaceResolver> resolvers) {
		this.resolvers = resolvers;
	}
	
	@Override
	public DefinedServiceInterface resolve(String id) {
		if (!ifaces.containsKey(id)) {
			synchronized(this) {
				if (!ifaces.containsKey(id)) {
					DefinedServiceInterface service = null;
					for (DefinedServiceInterfaceResolver resolver : resolvers) {
						service = resolver.resolve(id);
						if (service != null)
							break;
					}
					ifaces.put(id, service);
				}
			}
		}
		return ifaces.get(id);
	}

}
