package be.nabu.libs.services;

import be.nabu.libs.services.SimpleExecutionContext.SimpleServiceContext;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.services.api.ExecutionContext;
import be.nabu.libs.services.api.SecurityContext;
import be.nabu.libs.services.api.Service;
import be.nabu.libs.services.api.ServiceContext;
import be.nabu.libs.services.api.ServiceInterface;
import be.nabu.libs.services.api.ServiceWrapper;
import be.nabu.libs.services.api.TransactionContext;
import be.nabu.libs.types.TypeUtils;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.api.ComplexType;

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
		runtime.getContext().put("service.context", context);
	}
	
	public static String getServiceContext(ServiceRuntime runtime) {
		return getServiceContext(runtime, true);
	}
	
	public static String getServiceContext(ServiceRuntime runtime, boolean fail) {
		String context = (String) runtime.getContext().get("service.context");
		if (context != null) {
			return context;
		}
		if (ServiceRuntime.getGlobalContext() != null) {
			context = (String) ServiceRuntime.getGlobalContext().get("service.context");
		}
		if (context != null) {
			return context;
		}
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
}
