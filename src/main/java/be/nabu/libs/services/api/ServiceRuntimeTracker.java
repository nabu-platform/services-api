package be.nabu.libs.services.api;

public interface ServiceRuntimeTracker {
	// descriptions are aimed at non-technical users to give them an insight into how/why it runs
	public default void describe(Object object) {
		// do nothing
	}
	// reports are aimed at technical users
	public void report(Object object);
	public void start(Service service);
	public void stop(Service service);
	public void error(Service service, Exception exception);
	
	public void before(Object step);
	public void after(Object step);
	public void error(Object step, Exception exception);
}
