package be.nabu.libs.services.api;

import java.util.List;

public interface FeaturedExecutionContext extends ExecutionContext {
	// features are uniquely identified by their name, this is the name of the feature
	public List<String> getEnabledFeatures();
}
