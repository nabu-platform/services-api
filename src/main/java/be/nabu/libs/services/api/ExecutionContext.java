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

package be.nabu.libs.services.api;

import be.nabu.libs.cluster.api.ClusterInstance;
import be.nabu.libs.events.api.EventTarget;
import be.nabu.libs.metrics.api.MetricProvider;

public interface ExecutionContext extends MetricProvider {
	public ServiceContext getServiceContext();
	public TransactionContext getTransactionContext();
	public SecurityContext getSecurityContext();
	public default LanguageContext getLanguageContext() { return null; };
	public boolean isDebug();
	public default EventTarget getEventTarget() { return null; };
	public default ClusterInstance getCluster() { return null; };
}
