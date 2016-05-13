package be.nabu.libs.services.api;

public interface ServiceRuntimeTracker {
	public void report(Object object);
	public void start(Service service);
	public void stop(Service service);
	public void error(Service service, Exception exception);
	
	public void before(Object step);
	public void after(Object step);
	public void error(Object step, Exception exception);
}
