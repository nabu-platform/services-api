package be.nabu.libs.services.api;

import java.util.List;

// rules of execution for each service in this run
// sla requirements?
public interface RunProfile {
	public ServiceProfile getServiceProfile(String serviceId);
	public DataProfile getDataProfile(String typeId);
	public RunHandler getHandler();
	
	public static interface RunHandler {
		public void capture(String serviceId, String name, Object value);
		public void mutate(String typeId, Object newValue);
	}
	
	public enum CapturePhase {
		INPUT, OUTPUT
	}
	public static interface Capture {
		public String getName();
		public String getQuery();
		public CapturePhase getPhase();
	}
	// we want to be able to capture input/output/pipeline values
	// we want to be able to capture descriptions
	// we need to capture ids to correlate all this
	public static interface ServiceProfile {
		public String getServiceId();
		// for SLA purposes
		public Long getMaximumDuration();
		public List<Capture> getCaptures();
	}
	// we want to be able to capture data mutation (CRUD) in the database
	public static interface DataProfile {
		public String getTypeId();
	}
}
