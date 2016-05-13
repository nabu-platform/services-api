package be.nabu.libs.services.api;

import be.nabu.libs.services.ServiceRuntime;

public interface ServiceRuntimeTrackerProvider {
	public ServiceRuntimeTracker getTracker(ServiceRuntime runtime);
}
