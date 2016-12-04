package be.nabu.libs.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import be.nabu.libs.services.api.ExecutionContext;
import be.nabu.libs.services.api.Service;
import be.nabu.libs.services.api.ServiceException;
import be.nabu.libs.services.api.ServiceResult;
import be.nabu.libs.services.api.ServiceRunnableObserver;
import be.nabu.libs.services.api.ServiceRunner;
import be.nabu.libs.types.api.ComplexContent;

public class CombinedServiceRunner implements ServiceRunner {

	public static class CombinedServiceResult implements ServiceResult {
		
		private final ComplexContent output;
		private final ServiceException exception;
		private Map<ServiceRunner, ServiceResult> results;

		private CombinedServiceResult(Map<ServiceRunner, ServiceResult> results, ComplexContent output, ServiceException exception) {
			this.results = results;
			this.output = output;
			this.exception = exception;
		}

		@Override
		public ComplexContent getOutput() {
			return output;
		}

		@Override
		public ServiceException getException() {
			return exception;
		}

		public Map<ServiceRunner, ServiceResult> getResults() {
			return results;
		}
	}

	private List<ServiceRunner> runners;
	
	public CombinedServiceRunner(ServiceRunner...runners) {
		this.runners = Arrays.asList(runners);
	}
	
	public CombinedServiceRunner(List<ServiceRunner> runners) {
		this.runners = runners;
	}
	
	@Override
	public Future<ServiceResult> run(final Service service, ExecutionContext context, ComplexContent input, ServiceRunnableObserver...observers) {
		final Map<ServiceRunner, Future<ServiceResult>> futures = new HashMap<ServiceRunner, Future<ServiceResult>>();
		final List<Exception> exceptions = new ArrayList<Exception>();
		for (ServiceRunner runner : runners) {
			try {
				futures.put(runner, runner.run(service, context, input, observers));
			}
			catch (Exception e) {
				exceptions.add(e);
			}
		}
		return new Future<ServiceResult>() {
			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				return false;
			}
			
			@Override
			public boolean isCancelled() {
				return false;
			}
			
			@Override
			public boolean isDone() {
				return false;
			}
			
			@Override
			public ServiceResult get() throws InterruptedException, ExecutionException {
				try {
					return get(36500, TimeUnit.DAYS);
				}
				catch (TimeoutException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public ServiceResult get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
				Date started = new Date();
				long ms = TimeUnit.MILLISECONDS.convert(timeout, unit);
				List<ComplexContent> outputs = new ArrayList<ComplexContent>();
				Map<ServiceRunner, ServiceResult> results = new HashMap<ServiceRunner, ServiceResult>();
				for (ServiceRunner runner : futures.keySet()) {
					Future<ServiceResult> future = futures.get(runner);
					long duration = ms - (new Date().getTime() - started.getTime());
					if (duration < 0) {
						throw new TimeoutException();
					}
					try {
						ServiceResult serviceResult = future.get(duration, TimeUnit.MILLISECONDS);
						if (serviceResult.getException() != null) {
							exceptions.add(serviceResult.getException());
						}
						if (serviceResult.getOutput() != null) {
							outputs.add(serviceResult.getOutput());
						}
						results.put(runner, serviceResult);
					}
					catch (TimeoutException e) {
						throw e;
					}
					catch (InterruptedException e) {
						throw e;
					}
					catch (Exception e) {
						exceptions.add(e);
					}
				}
				final ServiceException exception;
				if (!exceptions.isEmpty()) {
					exception = new ServiceException("SERVICE-0", "Exceptions occurred when running on multiple runners");
					for (Exception e : exceptions) {
						exception.addSuppressed(e);
					}
				}
				else {
					exception = null;
				}
				// send back an empty content instead of null, not everyone can deal with null very well
				final ComplexContent output = service.getServiceInterface().getOutputDefinition().newInstance();
				// there is no result, concatenation can not be done on multiple results
				return new CombinedServiceResult(results, output, exception);
			}
			
		};
	}

}
