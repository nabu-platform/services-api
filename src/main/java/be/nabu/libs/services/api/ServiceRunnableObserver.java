package be.nabu.libs.services.api;

import be.nabu.libs.services.ServiceRunnable;

public interface ServiceRunnableObserver {
	public void start(ServiceRunnable runnable);
	public void stop(ServiceRunnable runnable);
}
