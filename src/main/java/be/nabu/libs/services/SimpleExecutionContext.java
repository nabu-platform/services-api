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

import be.nabu.libs.artifacts.ArtifactResolverFactory;
import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.artifacts.api.ArtifactResolver;
import be.nabu.libs.cache.api.CacheProvider;
import be.nabu.libs.metrics.api.MetricInstance;
import be.nabu.libs.services.api.ExecutionContext;
import be.nabu.libs.services.api.SecurityContext;
import be.nabu.libs.services.api.ServiceAuthorizer;
import be.nabu.libs.services.api.ServiceAuthorizerProvider;
import be.nabu.libs.services.api.ServiceContext;
import be.nabu.libs.services.api.ServiceRuntimeTracker;
import be.nabu.libs.services.api.ServiceRuntimeTrackerProvider;
import be.nabu.libs.services.api.TransactionContext;

public class SimpleExecutionContext implements ExecutionContext {

	private TransactionContext transactionContext;
	private SecurityContext securityContext;
	private ServiceContext serviceContext;
	private boolean isDebug;
	
	public SimpleExecutionContext(ServiceContext serviceContext, TransactionContext transactionContext, SecurityContext securityContext) {
		this.serviceContext = serviceContext;
		this.transactionContext = transactionContext;
		this.securityContext = securityContext;
	}
	
	@Override
	public TransactionContext getTransactionContext() {
		return transactionContext;
	}

	@Override
	public SecurityContext getSecurityContext() {
		return securityContext;
	}

	@Override
	public ServiceContext getServiceContext() {
		return serviceContext;
	}
	
	public static class SimpleServiceContext implements ServiceContext {
		@SuppressWarnings("unchecked")
		@Override
		public <T extends Artifact> ArtifactResolver<T> getResolver(Class<T> artifactType) {
			return (ArtifactResolver<T>) ArtifactResolverFactory.getInstance().getResolver();
		}
		@Override
		public CacheProvider getCacheProvider() {
			return null;
		}
		@Override
		public ServiceRuntimeTrackerProvider getServiceTrackerProvider() {
			return new ServiceRuntimeTrackerProvider() {
				@Override
				public ServiceRuntimeTracker getTracker(ServiceRuntime runtime) {
					return null;
				}
			};
		}
		@Override
		public ServiceAuthorizerProvider getServiceAuthorizerProvider() {
			return new ServiceAuthorizerProvider() {
				@Override
				public ServiceAuthorizer getAuthorizer(ServiceRuntime runtime) {
					return null;
				}
			};
		}
	}

	@Override
	public boolean isDebug() {
		return isDebug;
	}

	public void setDebug(boolean isDebug) {
		this.isDebug = isDebug;
	}

	@Override
	public MetricInstance getMetricInstance(String id) {
		return null;
	}
}
