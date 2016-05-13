package be.nabu.libs.services;

import be.nabu.libs.services.SimpleExecutionContext.EmptySecurityContext;
import be.nabu.libs.services.SimpleExecutionContext.SimpleServiceContext;
import be.nabu.libs.services.api.ExecutionContext;
import be.nabu.libs.services.api.SecurityContext;
import be.nabu.libs.services.api.ServiceContext;
import be.nabu.libs.services.api.ServiceInterface;
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
		return newExecutionContext(new SimpleServiceContext(), transactionContext, new EmptySecurityContext());
	}
	public static ExecutionContext newExecutionContext() {
		return newExecutionContext(new SimpleServiceContext(), new SimpleTransactionContext(), new EmptySecurityContext());
	}
	/**
	 * This checks that all the input and output variables are stringifiable 
	 */
	public static boolean isCacheable(ServiceInterface service) {
		return TypeUtils.isMarshallable(service.getInputDefinition())
			&& TypeUtils.isMarshallable(service.getOutputDefinition());
	}
}
