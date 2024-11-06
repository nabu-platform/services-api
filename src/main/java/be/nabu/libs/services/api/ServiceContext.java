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

import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.artifacts.api.ArtifactResolver;
import be.nabu.libs.cache.api.CacheProvider;
import be.nabu.libs.services.ServiceRunnerFactory;

public interface ServiceContext {
	public <T extends Artifact> ArtifactResolver<T> getResolver(Class<T> artifactType);
	public CacheProvider getCacheProvider();
	public ServiceRuntimeTrackerProvider getServiceTrackerProvider();
	public ServiceAuthorizerProvider getServiceAuthorizerProvider();
	public default ServiceRunner getServiceRunner() {
		return ServiceRunnerFactory.getInstance().getServiceRunner();
	}
	public default ServiceLevelAgreementProvider getServiceLevelAgreementProvider() {
		return null;
	}
	public default String getCorrelationId() {
		return null;
	}
}
