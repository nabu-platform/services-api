/*
* Copyright (C) 2016 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

package be.nabu.libs.services;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.nabu.libs.services.api.ServiceException;
import be.nabu.libs.services.api.ServiceResult;
import be.nabu.libs.services.api.ServiceRunnableObserver;
import be.nabu.libs.types.api.ComplexContent;

/**
 * A lightweight class that wraps around a service runtime and allows it to be run in a different thread (e.g. using thread pools)
 */
public class ServiceRunnable implements Runnable, Callable<ServiceResult> {

	private ComplexContent input, output;
	private ServiceRuntime runtime;
	private Thread runningThread;
	private ServiceException exception;
	private List<ServiceRunnableObserver> observers;
	private Logger logger = LoggerFactory.getLogger(getClass());

	public ServiceRunnable(ServiceRuntime runtime, ComplexContent input, ServiceRunnableObserver...observers) {
		this.runtime = runtime;
		this.input = input;
		this.observers = Arrays.asList(observers);
	}
	
	@Override
	public void run() {
		runningThread = Thread.currentThread();
		for (ServiceRunnableObserver observer : observers) {
			try {
				observer.start(this);
			}
			catch (Exception e) {
				logger.warn("Could not start observer", e);
			}
		}
		try {
			output = runtime.run(input);
		}
		catch (Exception e) {
			if (e instanceof ServiceException) {
				exception = (ServiceException) e;
			}
			else {
				exception = new ServiceException("RUNNER-1", "Unexpected exception", e);
			}
		}
		finally {
			for (ServiceRunnableObserver observer : observers) {
				try {
					observer.stop(this);
				}
				catch (Exception e) {
					logger.warn("Could not start observer", e);
				}
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

	@Override
	public ServiceResult call() {
		this.run();
		return new SimpleServiceResult(output, exception);
	}

	public ServiceRuntime getRuntime() {
		return runtime;
	}
	
}
