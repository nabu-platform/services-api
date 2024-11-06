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

	@Override
	public void describe(Object object) {
		for (ServiceRuntimeTracker tracker : trackers) {
			if (tracker != null) {
				tracker.describe(object);
			}
		}
	}
	
}
