package be.nabu.libs.services.api;

import be.nabu.libs.types.api.ComplexContent;

public interface ClusteredServiceRunner extends ServiceRunner {
	public void runAnywhere(Service service, ExecutionContext context, ComplexContent input, String target);
	public void runEverywhere(Service service, ExecutionContext context, ComplexContent input, String target);
}
