package be.nabu.libs.services.api;

public interface ForkableExecutionContext extends ExecutionContext {
	public ExecutionContext fork();
}
