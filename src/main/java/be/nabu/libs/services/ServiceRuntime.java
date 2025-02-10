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

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.nabu.libs.artifacts.api.ExternalDependency;
import be.nabu.libs.artifacts.api.ExternalDependencyArtifact;
import be.nabu.libs.authentication.api.Device;
import be.nabu.libs.authentication.api.Token;
import be.nabu.libs.authentication.api.principals.DevicePrincipal;
import be.nabu.libs.cache.api.Cache;
import be.nabu.libs.cache.api.CacheProvider;
import be.nabu.libs.converter.ConverterFactory;
import be.nabu.libs.events.api.EventTarget;
import be.nabu.libs.metrics.api.MetricInstance;
import be.nabu.libs.metrics.api.MetricTimer;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.services.api.ExecutionContext;
import be.nabu.libs.services.api.FeaturedExecutionContext;
import be.nabu.libs.services.api.Service;
import be.nabu.libs.services.api.ServiceAuthorizer;
import be.nabu.libs.services.api.ServiceException;
import be.nabu.libs.services.api.ServiceInstance;
import be.nabu.libs.services.api.ServiceLevelAgreement;
import be.nabu.libs.services.api.ServiceLevelAgreementProvider;
import be.nabu.libs.services.api.ServiceRuntimeTracker;
import be.nabu.libs.services.api.Transactionable;
import be.nabu.libs.services.impl.ServiceComplexEvent;
import be.nabu.libs.services.impl.TransactionReport;
import be.nabu.libs.types.CollectionHandlerFactory;
import be.nabu.libs.types.ComplexContentWrapperFactory;
import be.nabu.libs.types.ParsedPath;
import be.nabu.libs.types.SimpleTypeWrapperFactory;
import be.nabu.libs.types.TypeUtils;
import be.nabu.libs.types.api.CollectionHandlerProvider;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.DefinedSimpleType;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.api.SimpleType;
import be.nabu.libs.types.binding.json.JSONBinding;
import be.nabu.libs.types.java.BeanType;
import be.nabu.utils.cep.api.EventSeverity;
import be.nabu.utils.cep.impl.CEPUtils;

public class ServiceRuntime {

	private static boolean SUPPORTS_CPU_TIME = false;
	
	static {
		try {
			ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
			if (threadMXBean.isThreadCpuTimeSupported()) {
				if (!threadMXBean.isThreadCpuTimeEnabled()) {
					threadMXBean.setThreadCpuTimeEnabled(true);
				}
				SUPPORTS_CPU_TIME = true;
			}
		}
		catch (Exception e) {
			System.err.println("Can not determine whether the system supports cpu time");
			e.printStackTrace();
		}
	}
	
	private static Boolean UPGRADE_CACHE_HITS = Boolean.parseBoolean(System.getProperty("service.event.upgradeCacheHits", "false"));
	private static Boolean UPGRADE_CACHE_MISSES = Boolean.parseBoolean(System.getProperty("service.event.upgradeCacheMisses", "false"));
	public static final String METRIC_CACHE_RETRIEVE = "cacheRetrieve";
	public static final String METRIC_CACHE_STORE = "cacheStore";
	public static final String METRIC_CACHE_HIT = "cacheHit";
	public static final String METRIC_CACHE_MISS = "cacheMiss";
	public static final String METRIC_CACHE_FAILURE = "cacheFailure";
	public static final String METRIC_EXECUTION_TIME = "executionTime";
	public static final String METRIC_CPU_TIME = "cpuTime";
	public static final String METRIC_AUDIT_OVERHEAD = "auditOverhead";
	
	public static final String DEFAULT_CACHE_TIMEOUT = "be.nabu.services.cacheTimeout";
	public static final String NO_AUTHENTICATION = "AUTHORIZATION-0";
	public static final String NO_AUTHORIZATION = "AUTHORIZATION-1";
	public static final String ENABLED_FEATURES = "enabledFeatures";
	private String correlationId;
	
	private WeakHashMap<ComplexType, List<ParsedPath>> closeables = new WeakHashMap<ComplexType, List<ParsedPath>>();
	private static ThreadLocal<ServiceRuntime> runtime = new ThreadLocal<ServiceRuntime>();
	private static List<ServiceRuntime> running = Collections.synchronizedList(new ArrayList<ServiceRuntime>());
	
	// you can force an execution to bypass any cached value and update the cached value if it applies
	// this allows for very specific cache resets.
	private boolean recache;
	
	// whether or not a cached result was used
	private Boolean cachedResult;
	
	private Object report;
	
	// allows for a context that can exist cross root service runtime and is managed externally by some component
	private static ThreadLocal<Map<String, Object>> globalContext = new ThreadLocal<Map<String, Object>>();
	
	// here we can set externally managed state that does not impact the running services but can be used to share properties that are relevant
	private static Map<String, Object> serverContext = new HashMap<String, Object>();
	
	private ServiceRuntime parent, child;
	private Map<String, Object> context;
	private ExecutionContext executionContext;
	private Service service;
	private ServiceRuntimeTracker runtimeTracker;
	private CacheProvider cache;
	private boolean aborted;
	private ServiceException exception;
	private Boolean allowCaching;
	private static AtomicLong idGenerator = new AtomicLong();
	private Date started, stopped;
	private long startCpuTime;
	
	private long id;
	
	/**
	 * By default any closeable returned by a service invoke will be autoclosed at the end of the root service
	 * This does have a tiny performance impact (need to scan for the closeables) but it makes more than up for it in convenience
	 * If you turn this off, the performance hit is fully gone and the closeables are not managed
	 * 
	 * IMPORTANT: This works on the identity of the object so if you do advanced stuff like chaining streams into a new stream, the underlying streams will be closed!!
	 * At some point (if it comes up) I can add a property to a closeable to not manage it
	 */
	private Boolean manageCloseables = true;
	/**
	 * By default the closeables that are returned by the _root_ service are _not_ managed, this because we assume that whoever is calling the service wants to do something with those streams
	 * Toggle this to close even root streams, this may have strange behavior to external components that use services though (e.g. web artifacts, rest services,...)
	 */
	private Boolean allowRootCloseables = true;
	/**
	 * Total auditing overhead
	 */
	private long auditingOverhead = 0;

	private ServiceInstance serviceInstance;
	private ComplexContent input;
	private ComplexContent output;
	private Logger logger = LoggerFactory.getLogger(getClass());
	// you can set one directly on the runtime
	private ServiceLevelAgreementProvider slaProvider;
	private long cpuTime;
	private long threadId = -1;
	
	private long getCpuTime() {
		return threadId > 0 ? ManagementFactory.getThreadMXBean().getThreadCpuTime(threadId) : 0;
	}
	
	public long getCurrentCpuTime() {
		return SUPPORTS_CPU_TIME ? getCpuTime() - cpuTime : 0;
	}
	
	public static ServiceRuntime getRuntime() {
		return runtime.get();
	}
	
	public ServiceRuntime(Service service, ExecutionContext executionContext) {
		this.service = service;
		this.executionContext = executionContext;
		this.id = idGenerator.getAndIncrement();
	}
	
	public ExecutionContext getExecutionContext() {
		return executionContext;
	}

	public void report(Object report) {
		if (runtimeTracker != null) {
			runtimeTracker.report(report);
		}
		this.report = report;
	}
	
	public ComplexContent run(ComplexContent input) throws ServiceException {
		threadId = Thread.currentThread().getId();
		// note to self: we must NEVER run the getContext before the parent is hooked up or we can seriously mess up context inheritance
		ServiceComplexEvent event = null;
		if (getExecutionContext().getEventTarget() != null) {
			event = new ServiceComplexEvent();
			event.setCreated(new Date());
			event.setEventCategory("service");
			event.setEventName("service-execute");
			event.setCorrelationId(getCorrelationId());
			// we don't want these events at the info level
			// in a lot of cases, handling the events requires more service executions and if we log all that by default, we get infinite loops...?
			// also: TMI, we got metrics by default
			event.setSeverity(EventSeverity.DEBUG);
			
			if (service instanceof DefinedService) {
				event.setArtifactId(((DefinedService) service).getId());
			}
			Token token = executionContext.getSecurityContext().getToken();
			if (token != null) {
				event.setAlias(token.getName());
				event.setRealm(token.getRealm());
				event.setAuthenticationId(token.getAuthenticationId());
				if (token instanceof DevicePrincipal) {
					Device device = ((DevicePrincipal) token).getDevice();
					if (device != null) {
						event.setDeviceId(device.getDeviceId());
					}
				}
			}
		}
		started = new Date();
		if (runtime.get() != null) {
			parent = runtime.get();
			if (parent != null) {
				ServiceRuntime checking = parent;
				while (checking != null) {
					if (checking.equals(this)) {
						parent = null;
						logger.error("A circular reference in service runtimes has been found for service: " + (service instanceof DefinedService ? ((DefinedService) service).getId() : service));
						throw new IllegalStateException("A circular reference in service runtimes has been found for service: " + (service instanceof DefinedService ? ((DefinedService) service).getId() : service));
					}
					checking = checking.parent;
				}
			}
			parent.child = this;
		}
		runtime.set(this);
		
		ServiceLevelAgreement sla = getSla();
		MetricInstance metrics = null;
		try {
			running.add(this);

			ServiceAuthorizer authorizer = executionContext.getServiceContext().getServiceAuthorizerProvider().getAuthorizer(this);
			if (authorizer != null && !authorizer.canRun(this, input)) {
				throw new ServiceException(executionContext.getSecurityContext().getToken() == null ? NO_AUTHENTICATION : NO_AUTHORIZATION, "Unauthorized");
			}

			
			// check additional features that might have been enabled
			if (getExecutionContext() instanceof FeaturedExecutionContext) {
				// enable features in the execution context that were set in the service runtime
				@SuppressWarnings("unchecked")
				List<String> additionalFeatures = (List<String>) getContext().get(ENABLED_FEATURES);
				if (additionalFeatures != null && !additionalFeatures.isEmpty()) {
					additionalFeatures.removeAll(((FeaturedExecutionContext) getExecutionContext()).getEnabledFeatures());
					((FeaturedExecutionContext) getExecutionContext()).getEnabledFeatures().addAll(additionalFeatures);
					// clear it to prevent the overhead of attempting to reset
					// TODO: this was primarily added for test purposes, it is however not entirely clear if the execution context is correctly reused cross service calls in such a scenario (check the ServiceMethodProvider)
					// so currently we leave it so it is readded in that particular case
//					additionalFeatures.clear();
				}
			}
			
			
			
			// map the input so we can inspect it from the service trackers
			this.input = input;
			// the runtime tracker provider might use contextual data (e.g. the service call stack) to determine which trackers to return, so only call it after we have register this service runtime
			this.runtimeTracker = executionContext.getServiceContext().getServiceTrackerProvider().getTracker(this);
			this.cache = executionContext.getServiceContext().getCacheProvider();
			metrics = service instanceof DefinedService ? executionContext.getMetricInstance(((DefinedService) service).getId()) : null;
			
			if (runtimeTracker != null) {
				Date auditStart = new Date();
				runtimeTracker.start(service);
				addOverhead(auditStart);
			}
			output = null;
			List<ParsedPath> closeables = manageCloseables != null && manageCloseables ? getCloseablePaths(service.getServiceInterface().getOutputDefinition()) : null;
			if (!isRecache() && (closeables == null || closeables.isEmpty()) && isAllowCaching() && getCache() != null && service instanceof DefinedService) {
				Cache serviceCache = getCache().get(((DefinedService) service).getId());
				if (serviceCache != null) {
					MetricTimer timer = metrics == null ? null : metrics.start(METRIC_CACHE_RETRIEVE);
					output = (ComplexContent) serviceCache.get(input);
					if (timer != null) {
						timer.stop();
					}
					if (metrics != null) {
						metrics.increment(output == null ? METRIC_CACHE_MISS : METRIC_CACHE_HIT, 1);
					}
					if (event != null) {
						// we have a cache but we had either a hit or a miss
						event.setCached(output != null);
					}
				}
			}
			// if there is no cached version, execute the target service
			if (output == null) {
				serviceInstance = service.newInstance();
				MetricTimer timer = metrics == null ? null : metrics.start(METRIC_EXECUTION_TIME);
				cpuTime = SUPPORTS_CPU_TIME ? getCpuTime() : 0;
				output = serviceInstance.execute(getExecutionContext(), input);
				// and after we have run it
				cpuTime = SUPPORTS_CPU_TIME ? getCpuTime() - cpuTime : 0;
				cachedResult = false;
				if (timer != null) {
					timer.stop();
				}
				if (metrics != null && SUPPORTS_CPU_TIME) {
					// all other measurements are done in ms
					// in a typical setup, when 0ms are reported, we generally don't care about the nanoseconds
					// if that ever becomes a necessarity, we have to revisit other places reporting in ms
					// at that time we could add a cpuTimeNano or whatever
					metrics.log(METRIC_CPU_TIME, (long) (cpuTime / 1000000.0));
				}
				// store the newly calculated result in the cache if applicable
				if ((closeables == null || closeables.isEmpty()) && isAllowCaching() && getCache() != null && service instanceof DefinedService) {
					Cache serviceCache = getCache().get(((DefinedService) service).getId());
					if (serviceCache != null) {
						timer = metrics == null ? null : metrics.start(METRIC_CACHE_STORE);
						// @2024-05-28 we can't cache actual null values as "null" is used to detect the absence of a cache
						// we _can_ however cache an empty object
						if (output == null) {
							output = service.getServiceInterface().getOutputDefinition().newInstance();
						}
						if (!serviceCache.put(input, output) && metrics != null) {
							metrics.increment(METRIC_CACHE_FAILURE, 1);
						}
						if (timer != null) {
							timer.stop();
						}
					}
				}
			}
			else {
				cachedResult = true;
			}
			// check if we have closeables
			if (output != null && closeables != null && !closeables.isEmpty()) {
				for (Closeable closeable : getCloseables(output, closeables)) {
					// if we allow root closeables and are at the root, disable the transactionables that would close it
					// this will leave the streams open if they are returned by the root service
					if (getParent() == null && allowRootCloseables != null && allowRootCloseables) {
						Collection<Transactionable> all = getExecutionContext().getTransactionContext().getAll(null);
						if (all != null) {
							for (Transactionable transactionable : all) {
								if (transactionable instanceof TransactionCloseable && closeable.equals(((TransactionCloseable) transactionable).getCloseable())) {
									((TransactionCloseable) transactionable).setDisabled(true);
								}
							}
						}
					}
					else {
						TransactionCloseable transactionable = new TransactionCloseable(closeable);
						Collection<Transactionable> all = getExecutionContext().getTransactionContext().getAll(null);
						if (all == null || !all.contains(transactionable)) {
							getExecutionContext().getTransactionContext().add(null, transactionable);
						}
					}
				}
			}
			if (runtimeTracker != null) {
				Date auditStart = new Date();
				runtimeTracker.stop(service);
				addOverhead(auditStart);
			}
			return output;
		}
		catch (ServiceException e) {
			exception = e;
			if (runtimeTracker != null) {
				Date auditStart = new Date();
				runtimeTracker.error(service, e);
				addOverhead(auditStart);
			}
			if (event != null) {
				CEPUtils.enrich(event, e);
				event.setSourceId(e.getSourceId());
				event.setContext(e.getServiceStack().toString());
				event.setCode(e.getCode());
				event.setReason(e.getDescription());
				event.setLocalId(e.getId());
				if (e.getToken() != null) {
					event.setAlias(e.getToken().getName());
					event.setRealm(e.getToken().getRealm());
				}
				if (e.isReported()) {
					event.setSeverity(EventSeverity.WARNING);
				}
				else {
					e.setReported(true);
				}
				if (e.getData() != null) {
					try {
						if (e.getData() instanceof Iterable) {
							StringBuilder builder = new StringBuilder();
							for (Object single : (Iterable<?>) e.getData()) {
								builder.append(stringify(single)).append("\n");
							}
							event.setData(builder.toString());
						}
						else {
							event.setData(stringify(e.getData()));
						}
					}
					catch (Exception f) {
						// best effort
						f.printStackTrace();
					}
				}
			}
			throw e;
		}
		catch (Exception e) {
			exception = new ServiceException(e);
			if (runtimeTracker != null) {
				Date auditStart = new Date();
				runtimeTracker.error(service, e);
				addOverhead(auditStart);
			}
			if (event != null) {
				CEPUtils.enrich(event, e);
				event.setLocalId(exception.getId());
			}
			throw exception;
		}
		finally {
			if (event != null) {
				event.setStarted(started);
				event.setStopped(new Date());
				
				// if it's a debug event, upgrade it to INFO in certain conditions that we want to track
				if (event.getSeverity() == EventSeverity.DEBUG) {
					// the performance tracker was ignoring glue and startup based methods? especially startup (long running) can create many unnecessary logs, glue generates lots of "root" services as well...
					Object serviceSource = getContext().get("service.source");

					if (serviceSource != null) {
						event.setOrigin(serviceSource.toString());
					}
					
					// some system-based things are only upgraded if necessary
					boolean partialUpgrade = false;
					Token token = executionContext.getSecurityContext().getToken();
					// we want to ignore system token actions, these are usually automated actions
					if (token != null && token.getRealm().equals("$system")) {
						partialUpgrade = true;
					}
					if (serviceSource != null && (serviceSource.equals("startup") || serviceSource.equals("glue"))) {
						partialUpgrade = true;
					}
					// hardcoded exception for sleep.......not ideal :|
					if (service instanceof DefinedService && ((DefinedService) service).getId().equals("nabu.utils.Server.sleep")) {
						partialUpgrade = true;
					}
					
					boolean upgrade = false;

					EventSeverity upgradeSeverity = EventSeverity.INFO;
					// we always upgrade services with an SLA
					if (sla != null) {
						// if we have an official sla, we always want to upgrade
						// for an unofficial sla, we only upgrade if we reach the threshold
						upgrade = sla.isExplicit();
						Long thresholdDuration = sla.getThresholdDuration();
						event.setThreshold(sla.getThresholdDuration());
						// if we surpassed the threshold, log it
						if (thresholdDuration != null && event.getStopped().getTime() - event.getStarted().getTime() > thresholdDuration) {
							// upgrade!
							upgrade = true;
							event.setCode("SLA-THRESHOLD-EXCEEDED");	
							upgradeSeverity = sla.getSeverity(); // EventSeverity.WARNING
						}
						// we must also log "success", otherwise we can't calculate the percentage of threshold breaches!
						else if (sla.isExplicit()) {
							event.setCode("SLA-THRESHOLD-MAINTAINED");
						}
					}
					// all root services that are not subjected to partial upgrade rules
					if (!partialUpgrade && parent == null) {
						upgrade = true;
//						event.setCode("ROOT-SERVICE");
						event.setEventName("service-execute-root");
					}
					// we no longer upgrade cache hits by default, this is "works as designed" and could trigger a lot of logs
					// we only log cache misses explicitly if it is not a root service, otherwise the boolean will suffice
					if (!upgrade && event.getCached() != null && (UPGRADE_CACHE_HITS || UPGRADE_CACHE_MISSES)) {
						// we only upgrade if we have a cache miss or if we also want to upgrade cache hits
						upgrade = (!event.getCached() && UPGRADE_CACHE_MISSES) || (event.getCached() && UPGRADE_CACHE_HITS);
						event.setCode("CACHE-" + (event.getCached() ? "HIT" : "MISS"));
					}
					// if we have external dependencies, log it
					if (!upgrade && service instanceof ExternalDependencyArtifact && ((ExternalDependencyArtifact) service).getExternalDependencies() != null && !((ExternalDependencyArtifact) service).getExternalDependencies().isEmpty()) {
						upgrade = true;
						event.setCode("EXTERNAL-DEPENDENCY");
					}
					// always set the external dependency (if any)
					if (service instanceof ExternalDependencyArtifact && ((ExternalDependencyArtifact) service).getExternalDependencies() != null && !((ExternalDependencyArtifact) service).getExternalDependencies().isEmpty()) {
						for (ExternalDependency externalDependency : ((ExternalDependencyArtifact) service).getExternalDependencies()) {
							if (externalDependency.getEndpoint() != null) {
								event.setExternalDependency(externalDependency.getEndpoint().getHost());
							}
							else if (externalDependency.getGroup() != null) {
								event.setExternalDependency(externalDependency.getGroup());
							}
							else if (externalDependency.getId() != null) {
								event.setExternalDependency(externalDependency.getId());
							}
							else if (externalDependency.getArtifactId() != null) {
								event.setExternalDependency(externalDependency.getArtifactId());
							}
							break;
						}
					}
					// we upgrade it to INFO level
					if (upgrade) {
						// we set the service context as well
						if (parent != null) {
							List<String> context = new ArrayList<String>();
							ServiceRuntime check = parent;
							while (check != null) {
								if (check.getService() instanceof DefinedService) {
									context.add(((DefinedService) check.getService()).getId());
								}
								check = check.getParent();
							}
							event.setContext(context.toString());
						}
						event.setSeverity(upgradeSeverity);
						
						// if we have a report at this time, add it
						if (report != null && event.getData() == null) {
							try {
								event.setData(stringify(report));
							}
							catch (Exception e) {
								// best effort
								logger.info("Could not serialize report", e);
							}
						}
					}
				}
				
				EventTarget eventTarget = executionContext.getEventTarget();
				// it may have been unset by the runtime!
				if (eventTarget != null) {
					eventTarget.fire(event, this);
				}
			}
			if (parent != null) {
				// if the parent does not have the same execution context, we assume that the current context needs to be cleaned up
				// TODO: an alternative implementation (if needed) could be added where the transactions of this context are passed to the parent for management
				// this is however currently not necessary
				try {
					if (!parent.executionContext.equals(executionContext)) {
						closeAllTransactions();
					}
				}
				finally {
					stopped = new Date();
					parent.child = null;
					runtime.set(parent);
					parent = null;
					running.remove(this);
				}
			}
			else {
				// if we had any auditing overhead, log it in metrics
				if (auditingOverhead > 0) {
					if (metrics != null) {
						metrics.duration(METRIC_AUDIT_OVERHEAD, auditingOverhead, TimeUnit.MILLISECONDS);
					}
				}
				try {
					closeAllTransactions();
				}
				finally {
					stopped = new Date();
					// remove this runtime from the thread local
					runtime.remove();
					running.remove(this);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private String stringify(Object data) throws IOException {
		// check if it's a simple type
		DefinedSimpleType<? extends Object> wrapped = SimpleTypeWrapperFactory.getInstance().getWrapper().wrap(data.getClass());
		if (wrapped != null) {
			return ConverterFactory.getInstance().getConverter().convert(data, String.class);
		}
		else {
			ComplexContent content = data instanceof ComplexContent ? (ComplexContent) data : ComplexContentWrapperFactory.getInstance().getWrapper().wrap(data);
			if (content != null) {
				JSONBinding binding = new JSONBinding(content.getType(), Charset.forName("UTF-8"));
				ByteArrayOutputStream serialized = new ByteArrayOutputStream();
				binding.marshal(serialized, content);
				return new String(serialized.toByteArray(), Charset.forName("UTF-8"));
			}
		}
		return null;
	}
	
	private void addOverhead(Date auditStart) {
		long overhead = new Date().getTime() - auditStart.getTime();
		auditingOverhead += overhead;
		if (getParent() != null) {
			getParent().incrementAuditingOverhead(overhead);
		}
	}

	public static Collection<ServiceRuntime> getRunning() {
		return new ArrayList<ServiceRuntime>(running);
	}

	public void closeAllTransactions() {
		// if there is no parent left, finish all the open transactions
		for (String transactionId : getExecutionContext().getTransactionContext()) {
			try {
				// if we abort a service, we also roll it back as we may have stopped mid-something important
				if (exception == null && !isAborted()) {
					getExecutionContext().getTransactionContext().commit(transactionId);
					if (runtimeTracker != null) {
						runtimeTracker.report(new TransactionReport(transactionId == null ? getExecutionContext().getTransactionContext().getDefaultTransactionId() : transactionId, "commit"));
					}
				}
				else {
					getExecutionContext().getTransactionContext().rollback(transactionId);
					if (runtimeTracker != null) {
						runtimeTracker.report(new TransactionReport(transactionId == null ? getExecutionContext().getTransactionContext().getDefaultTransactionId() : transactionId, "rollback"));
					}
				}
			}
			catch (Exception e) {
				logger.warn("Could not close transaction: " + transactionId, e);
				// don't call the tracker, otherwise he sees potentially multiple errors after it was either successfully stopped or already had another error
//				if (runtimeTracker != null) {
//					runtimeTracker.error(service, e);
//				}
			}
		}
//		if (auditingOverhead > 0) {
//			Date stopped = new Date();
//			long total = stopped.getTime() - started.getTime();
//			// for really short services, this is misleading
//			if (total >= 25) {
//				String serviceId = service instanceof DefinedService ? ((DefinedService) service).getId() : "$anonymous";
//				logger.warn("Audit overhead for '" + serviceId + "' is: " + auditingOverhead + " vs " + (stopped.getTime() - started.getTime()) + " runtime or " + (((1.0*auditingOverhead) / (stopped.getTime() - started.getTime())) * 100) + "%");
//			}
//		}
	}
	
	private List<Closeable> getCloseables(ComplexContent content, List<ParsedPath> paths) {
		List<Closeable> closeables = new ArrayList<Closeable>();
		for (ParsedPath path : paths) {
			getCloseables(content, path, closeables);
		}
		return closeables;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void getCloseables(ComplexContent content, ParsedPath path, List<Closeable> closeables) {
		Object object = content.get(path.getName());
		if (object != null) {
			if (path.getChildPath() == null) {
				// if it's a closeable, shortcut
				if (object instanceof Closeable) {
					closeables.add((Closeable) object);
				}
				// if not null and not a closeable, hopefully it's a collection of closeables...
				else {
					CollectionHandlerProvider provider = CollectionHandlerFactory.getInstance().getHandler().getHandler(object.getClass());
					if (provider != null) {
						for (Object single : provider.getAsCollection(object)) {
							if (single instanceof Closeable) {
								closeables.add((Closeable) single);
							}
						}
					}
				}
			}
			else {
				if (!(object instanceof ComplexContent)) {
					CollectionHandlerProvider provider = CollectionHandlerFactory.getInstance().getHandler().getHandler(object.getClass());
					if (provider != null) {
						for (Object single : provider.getAsCollection(object)) {
							if (!(single instanceof ComplexContent)) {
								single = ComplexContentWrapperFactory.getInstance().getWrapper().wrap(single);		
							}
							if (single instanceof ComplexContent) {
								getCloseables((ComplexContent) single, path.getChildPath(), closeables);			
							}
						}
					}
					else {
						object = ComplexContentWrapperFactory.getInstance().getWrapper().wrap(object);
					}
				}
				if (object instanceof ComplexContent) {
					getCloseables((ComplexContent) object, path.getChildPath(), closeables);
				}
			}
		}
	}
	
	private List<ParsedPath> getCloseablePaths(ComplexType type) {
		if (!closeables.containsKey(type)) {
			synchronized(closeables) {
				if (!closeables.containsKey(type)) {
					List<String> paths = new ArrayList<String>();
					List<ComplexType> blackList = new ArrayList<ComplexType>();
					getCloseablePaths(type, null, paths, blackList);
					List<ParsedPath> parsed = new ArrayList<ParsedPath>();
					for (String path : paths) {
						parsed.add(new ParsedPath(path));
					}
					closeables.put(type, parsed);
				}
			}
		}
		return closeables.get(type);
	}
	
	private void getCloseablePaths(ComplexType type, String path, List<String> paths, List<ComplexType> blackListed) {
		for (Element<?> element : TypeUtils.getAllChildren(type)) {
			String childPath = (path == null ? "" : path + "/") + element.getName();
			if (element.getType() instanceof SimpleType && Closeable.class.isAssignableFrom(((SimpleType<?>) element.getType()).getInstanceClass())) {
				paths.add(childPath);
			}
			else if (element.getType() instanceof BeanType && Closeable.class.isAssignableFrom(((BeanType<?>) element.getType()).getBeanClass())) {
				paths.add(childPath);
			}
			else if (element.getType() instanceof ComplexType && !blackListed.contains(element.getType())) {
				blackListed.add((ComplexType) element.getType());
				getCloseablePaths((ComplexType) element.getType(), childPath, paths, blackListed);
			}
		}
	}
	
	public ServiceInstance getServiceInstance() {
		return serviceInstance;
	}

	public ServiceRuntime getParent() {
		return parent;
	}
	
	public ServiceRuntime getRoot() {
		ServiceRuntime root = this;
		while (root.parent != null) {
			root = root.parent;
		}
		return root;
	}
	
	public ServiceRuntime getChild() {
		return child;
	}

	public Map<String, Object> getContext() {
		return getContext(false);
	}
	
	public void setContext(Map<String, Object> context) {
		this.context = context;
	}

	public Map<String, Object> getContext(boolean inheritRunning) {
		if (context == null) {
			context = getGlobalContext();
			if (context == null) {
				if (parent != null) {
					return parent.getContext();
				}
				// sometimes you want to access the current runtime context before it is run (and chained up to the parent)
				else if (inheritRunning && getRuntime() != null) {
					return getRuntime().getContext();
				}
				else {
					context = new HashMap<String, Object>();
				}
			}
		}
		return context;
	}

	public Service getService() {
		return service;
	}
	
	public CacheProvider getCache() {
		return cache == null && parent != null ? parent.getCache() : cache;
	}

	public boolean isAborted() {
		return parent != null ? parent.isAborted() : aborted;
	}
	
	public void abort() {
		if (parent != null) {
			parent.abort();
		}
		else {
			aborted = true;
		}
	}
	
	public boolean isAllowCaching() {
		if (allowCaching != null && !allowCaching) {
			return false;
		}
		return parent == null || parent.isAllowCaching();
	}

	public void setAllowCaching(Boolean allowCaching) {
		this.allowCaching = allowCaching;
	}

	public Boolean getManageCloseables() {
		return manageCloseables == null && parent != null ? parent.getManageCloseables() : manageCloseables;
	}

	public void setManageCloseables(Boolean manageCloseables) {
		this.manageCloseables = manageCloseables;
	}

	public ServiceRuntimeTracker getRuntimeTracker() {
		return runtimeTracker;
	}

	public ComplexContent getInput() {
		return input;
	}

	public ComplexContent getOutput() {
		return output;
	}
	
	public static void setGlobalContext(Map<String, Object> globalContext) {
		ServiceRuntime.globalContext.set(globalContext);
	}
	
	public static Map<String, Object> getGlobalContext() {
		return ServiceRuntime.globalContext.get();
	}
	
	public void registerInThread(boolean inheritParent) {
		if (runtime.get() != null && inheritParent) {
			parent = runtime.get();
			parent.child = this;
		}
		runtime.set(this);
	}
	
	public void unregisterInThread() {
		if (runtime.get() != null && runtime.get().equals(this)) {
			runtime.set(null);
		}
		if (parent != null && equals(parent.child)) {
			parent.child = null;
		}
	}

	public long getId() {
		return id;
	}

	public Date getStarted() {
		return started;
	}

	public Date getStopped() {
		return stopped;
	}

	// recaching is currently recursive, the usecase for resetting a top layer cache without resetting a bottom layer one is tiny
	// if this ever becomes an issue, we add a boolean to alter this behavior
	public boolean isRecache() {
		return recache || (getParent() != null && getParent().isRecache());
	}

	public void setRecache(boolean recache) {
		this.recache = recache;
	}

	public String getCorrelationId() {
		if (correlationId != null) {
			return correlationId;
		}
		else if (getParent() != null) {
			return getParent().getCorrelationId();
		}
		else {
			// try to get it from the context
			if (executionContext != null) {
				correlationId = executionContext.getServiceContext().getCorrelationId();
			}
			if (correlationId == null) {
				synchronized(this) {
					if (correlationId == null) {
						correlationId = UUID.randomUUID().toString().replace("-", "");
					}
				}
			}
			return correlationId;
		}
	}

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}

	public Boolean getCachedResult() {
		return cachedResult;
	}

	public void setCachedResult(Boolean cachedResult) {
		this.cachedResult = cachedResult;
	}

	public long getAuditingOverhead() {
		return auditingOverhead;
	}

	void incrementAuditingOverhead(long auditingOverhead) {
		this.auditingOverhead += auditingOverhead;
	}
	
	public ServiceLevelAgreement getSla() {
		ServiceLevelAgreementProvider slaProvider = getSlaProvider();
		ServiceLevelAgreement sla = slaProvider == null ? null : slaProvider.getAgreementFor(service);
		if (sla == null && parent != null) {
			ServiceLevelAgreement parentSla = parent.getSla();
			if (parentSla != null && parentSla.getThresholdDuration() != null) {
				// if we contribute to more than 50% of the parent threshold, we should make ourselves known
				sla = new ServiceLevelAgreement() {
					@Override
					public Long getThresholdDuration() {
						return (long) (parentSla.getThresholdDuration() / 2.0);
					}
					@Override
					public EventSeverity getSeverity() {
						return EventSeverity.WARNING;
					}
					@Override
					public boolean isExplicit() {
						return false;
					}
				};
			}
		}
		return sla;
	}

	public ServiceLevelAgreementProvider getSlaProvider() {
		if (slaProvider == null && parent != null) {
			return parent.getSlaProvider();
		}
		return slaProvider;
	}

	public void setSlaProvider(ServiceLevelAgreementProvider slaProvider) {
		this.slaProvider = slaProvider;
	}
	
}
