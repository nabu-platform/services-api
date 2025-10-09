package be.nabu.libs.services.api;

public interface PersistedServiceRuntimeTrackerProvider extends ServiceRuntimeTrackerProvider {
	// returns a marshalled configuration which can be used to restore the state
	public String getConfiguration();
	public void setConfiguration(String configuration);
}
