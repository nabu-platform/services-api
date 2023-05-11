package be.nabu.libs.services.api;

import be.nabu.libs.cluster.api.ClusterInstance;
import be.nabu.libs.events.api.EventTarget;
import be.nabu.libs.metrics.api.MetricProvider;

public interface ExecutionContext extends MetricProvider {
	public ServiceContext getServiceContext();
	public TransactionContext getTransactionContext();
	public SecurityContext getSecurityContext();
	public default LanguageContext getLanguageContext() { return null; };
	public boolean isDebug();
	public default EventTarget getEventTarget() { return null; };
	public default ClusterInstance getCluster() { return null; };
}
