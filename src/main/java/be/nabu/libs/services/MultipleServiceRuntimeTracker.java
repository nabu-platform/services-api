package be.nabu.libs.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import be.nabu.libs.services.api.Service;
import be.nabu.libs.services.api.ServiceRuntimeTracker;

public class MultipleServiceRuntimeTracker implements ServiceRuntimeTracker {

	private List<ServiceRuntimeTracker> trackers = new ArrayList<ServiceRuntimeTracker>();
	
	public MultipleServiceRuntimeTracker(ServiceRuntimeTracker...trackers) {
		this.trackers.addAll(Arrays.asList(trackers));
	}
	
	@Override
	public void start(Service service) {
		for (ServiceRuntimeTracker tracker : trackers) {
			if (tracker != null) {
				tracker.start(service);
			}
		}
	}

	@Override
	public void stop(Service service) {
		for (ServiceRuntimeTracker tracker : trackers) {
			if (tracker != null) {
				tracker.stop(service);
			}
		}
	}

	@Override
	public void error(Service service, Exception exception) {
		for (ServiceRuntimeTracker tracker : trackers) {
			if (tracker != null) {
				tracker.error(service, exception);
			}
		}
	}

	@Override
	public void before(Object step) {
		for (ServiceRuntimeTracker tracker : trackers) {
			if (tracker != null) {
				tracker.before(step);
			}
		}
	}

	@Override
	public void after(Object step) {
		for (ServiceRuntimeTracker tracker : trackers) {
			if (tracker != null) {
				tracker.after(step);
			}
		}
	}

	@Override
	public void error(Object step, Exception exception) {
		for (ServiceRuntimeTracker tracker : trackers) {
			if (tracker != null) {
				tracker.error(step, exception);
			}
		}
	}

	public List<ServiceRuntimeTracker> getTrackers() {
		return trackers;
	}

	@Override
	public void report(Object object) {
		for (ServiceRuntimeTracker tracker : trackers) {
			if (tracker != null) {
				tracker.report(object);
			}
		}
	}
}
