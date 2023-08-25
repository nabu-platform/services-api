package be.nabu.libs.services;

import java.util.List;

import be.nabu.libs.services.SimpleExecutionContext.SimpleServiceContext;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.services.api.ExecutionContext;
import be.nabu.libs.services.api.SecurityContext;
import be.nabu.libs.services.api.Service;
import be.nabu.libs.services.api.ServiceContext;
import be.nabu.libs.services.api.ServiceInterface;
import be.nabu.libs.services.api.ServiceWrapper;
import be.nabu.libs.services.api.TransactionContext;
import be.nabu.libs.types.DefinedTypeResolverFactory;
import be.nabu.libs.types.TypeUtils;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.DefinedType;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.api.KeyValuePair;
import be.nabu.libs.types.api.Type;
import be.nabu.libs.types.java.BeanType;

public class ServiceUtils {
	public static ComplexContent map(ComplexContent from, ComplexType to) {
		return from;
	}
	public static ExecutionContext newExecutionContext(ServiceContext serviceContext, TransactionContext transactionContext, SecurityContext securityContext) {
		return new SimpleExecutionContext(serviceContext, transactionContext, securityContext);
	}
	public static ExecutionContext newExecutionContext(SecurityContext securityContext) {
		return newExecutionContext(new SimpleServiceContext(), new SimpleTransactionContext(), securityContext);
	}
	public static ExecutionContext newExecutionContext(TransactionContext transactionContext) {
		return newExecutionContext(new SimpleServiceContext(), transactionContext, new SimpleSecurityContext());
	}
	public static ExecutionContext newExecutionContext() {
		return newExecutionContext(new SimpleServiceContext(), new SimpleTransactionContext(), new SimpleSecurityContext());
	}
	/**
	 * This checks that all the input and output variables are stringifiable 
	 */
	public static boolean isCacheable(ServiceInterface service) {
		return TypeUtils.isMarshallable(service.getInputDefinition())
			&& TypeUtils.isMarshallable(service.getOutputDefinition());
	}
	
	public static Service unwrap(Service wrapper) {
		while (wrapper instanceof ServiceWrapper) {
			wrapper = ((ServiceWrapper) wrapper).getOriginal();
		}
		return wrapper;
	}
	
	public static void setServiceContext(ServiceRuntime runtime, String context) {
		if (runtime == null && ServiceRuntime.getGlobalContext() != null) {
			ServiceRuntime.getGlobalContext().put("service.context", context);
		}
		else {
			runtime.getContext().put("service.context", context);
		}
	}
	
	public static String getServiceContext(ServiceRuntime runtime) {
		return getServiceContext(runtime, true);
	}
	
	public static String getServiceContext(ServiceRuntime runtime, boolean fail) {
		String context = null;

		// the runtime.getContext() _can_ be a local hashmap it if was initialized at the wrong time (before the runtime had a parent at all)
		// so to be sure we get the correct one, we loop over the runtimes based on the parent they have _now_
		ServiceRuntime current = runtime;
		while (current != null) {
			context = (String) current.getContext().get("service.context");
			if (context != null) {
				return context;
			}
			current = current.getParent();
		}
		// check if we have a global context
		if (ServiceRuntime.getGlobalContext() != null) {
			context = (String) ServiceRuntime.getGlobalContext().get("service.context");
		}
		if (context != null) {
			return context;
		}
		// we have no context, we will use the root service name
		while (runtime != null) {
			Service service = unwrap(runtime.getService());
			if (service instanceof DefinedService && !((DefinedService) service).getId().startsWith("$self")) {
				context = ((DefinedService) service).getId();
			}
			runtime = runtime.getParent();
		}
		if (context == null && fail) {
			throw new IllegalStateException("No service context found");
		}
		return context;
	}

	// check the matching percentage with given implementations
	// most likely you define a java.lang.Object in the input of your service interface specification and want to find the best implementation for a specific type being passed along
	// the implementation can implement the interface, restrict the object and reinsert it with the type that it expects
	// the list only contains values that are implementations and is sorted from most specific to least specific
	// this means if you have B extends A and have three implementations (one for B, one for A and one for generic object), the order will be: B, A, generic
	// taking the first implementation is most likely the best
	// note that implementations can be for both input & output! just copy the full path (e.g. input/myObject)
	public static Integer getMatchPercentage(DefinedService service, List<KeyValuePair> implementations) {
		Integer prio = 0;
		if (implementations != null && !implementations.isEmpty()) {
			for (KeyValuePair implementation : implementations) {
				ComplexType type;
				String path;
				if (implementation.getKey().startsWith("input/")) {
					type = service.getServiceInterface().getInputDefinition();
					path = implementation.getKey().substring("input/".length());
				}
				else if (implementation.getKey().startsWith("output/")) {
					type = service.getServiceInterface().getOutputDefinition();
					path = implementation.getKey().substring("output/".length());
				}
				else {
					type = service.getServiceInterface().getInputDefinition();
					path = implementation.getKey();
				}
				Element<?> element = type.get(path);
				// if the element doesn't exist at all, it is not compatible
				if (element == null) {
					return null;
				}
				DefinedType resolve = DefinedTypeResolverFactory.getInstance().getResolver().resolve(implementation.getValue());
				if (resolve == null) {
					throw new IllegalArgumentException("Can not resolve type: " + implementation.getValue());
				}
				boolean found = false;
				Type search = element.getType();
				while (search != null) {
					if (search instanceof DefinedType) {
						String id = ((DefinedType) search).getId();
						if (id == null) {
							System.out.println("id is empty??? " + element.getName() + " => " + element.getType() + " / " + search);
						}
						if (id.equals(implementation.getValue())) {
							found = true;
							break;
						}
					}
					// we decrease the prio per distance we have to go
					prio--;
					search = search.getSuperType();
				}
				// if we can't find a match, no prio, just don't add it
				if (!found) {
					// if the target is actually java.lang.Object, we still allow it
					if (element.getType() instanceof BeanType && Object.class.equals(((BeanType<?>) element.getType()).getBeanClass())) {
						prio--;
					}
					else {
						return null;
					}
				}
			}
		}
		return prio;
	}
}
