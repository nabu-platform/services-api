package be.nabu.libs.services.api;

import be.nabu.libs.metrics.api.MetricProvider;

public interface ExecutionContext extends MetricProvider {
	public ServiceContext getServiceContext();
	public TransactionContext getTransactionContext();
	public SecurityContext getSecurityContext();
	public boolean isDebug();
}
