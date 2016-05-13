package be.nabu.libs.services;

import java.util.Arrays;
import java.util.List;

import be.nabu.libs.services.ServiceRuntime;
import be.nabu.libs.services.api.ServiceException;
import be.nabu.libs.services.api.ServiceRunnableObserver;
import be.nabu.libs.types.api.ComplexContent;

/**
 * A lightweight class that wraps around a service runtime and allows it to be run in a different thread (e.g. using thread pools)
 */
public class ServiceRunnable implements Runnable {

	private ComplexContent input, output;
	private ServiceRuntime runtime;
	private Thread runningThread;
	private ServiceException exception;
	private List<ServiceRunnableObserver> observers;

	public ServiceRunnable(ServiceRuntime runtime, ComplexContent input, ServiceRunnableObserver...observers) {
		this.runtime = runtime;
		this.input = input;
		this.observers = Arrays.asList(observers);
	}
	
	@Override
	public void run() {
		runningThread = Thread.currentThread();
		for (ServiceRunnableObserver observer : observers) {
			observer.start(this);
		}
		try {
			output = runtime.run(input);
		}
		catch (ServiceException e) {
			exception = e;
		}
		finally {
			for (ServiceRunnableObserver observer : observers) {
				observer.stop(this);
			}
			runningThread = null;
		}
	}

	public ComplexContent getOutput() {
		return output;
	}

	public ServiceException getException() {
		return exception;
	}

	public Thread getRunningThread() {
		return runningThread;
	}
}
