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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import be.nabu.libs.services.api.ExecutionContext;
import be.nabu.libs.services.api.Service;
import be.nabu.libs.services.api.ServiceResult;
import be.nabu.libs.services.api.ServiceRunnableObserver;
import be.nabu.libs.services.api.ServiceRunner;
import be.nabu.libs.types.api.ComplexContent;

public class ServiceRunnerFactory {
	
	private static ServiceRunnerFactory instance;
	
	public static ServiceRunnerFactory getInstance() {
		if (instance == null) {
			instance = new ServiceRunnerFactory();
		}
		return instance;
	}
	
	private ServiceRunner serviceRunner;
	
	public ServiceRunner getServiceRunner() {
		if (serviceRunner == null) {
			serviceRunner = new ServiceRunner() {

				@Override
				public Future<ServiceResult> run(Service service, ExecutionContext context, ComplexContent input, ServiceRunnableObserver...observers) {
					ServiceRuntime runtime = new ServiceRuntime(service, context);
					ServiceRunnable runnable = new ServiceRunnable(runtime, input, observers);
					runnable.run();
					final ServiceResult result = new SimpleServiceResult(runnable.getOutput(), runnable.getException());
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
							return true;
						}
						@Override
						public ServiceResult get() throws InterruptedException, ExecutionException {
							return result;
						}
						@Override
						public ServiceResult get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
							return result;
						}
					};
				}
			};
		}
		return serviceRunner;
	}
	public void setServiceRunner(ServiceRunner serviceRunner) {
		this.serviceRunner = serviceRunner;
	}
	
	@SuppressWarnings("unused")
	private void activate() {
		instance = this;
	}
	@SuppressWarnings("unused")
	private void deactivate() {
		instance = null;
	}
}
