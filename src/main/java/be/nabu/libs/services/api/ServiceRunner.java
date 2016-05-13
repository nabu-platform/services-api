package be.nabu.libs.services.api;

import java.util.concurrent.Future;

import be.nabu.libs.types.api.ComplexContent;

public interface ServiceRunner {
	public Future<ServiceResult> run(Service service, ExecutionContext context, ComplexContent input, ServiceRunnableObserver...observers);
}
