package be.nabu.libs.services;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.nabu.libs.cache.api.Cache;
import be.nabu.libs.cache.api.CacheProvider;
import be.nabu.libs.metrics.api.MetricInstance;
import be.nabu.libs.metrics.api.MetricTimer;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.services.api.ExecutionContext;
import be.nabu.libs.services.api.Service;
import be.nabu.libs.services.api.ServiceAuthorizer;
import be.nabu.libs.services.api.ServiceException;
import be.nabu.libs.services.api.ServiceInstance;
import be.nabu.libs.services.api.ServiceRuntimeTracker;
import be.nabu.libs.services.api.Transactionable;
import be.nabu.libs.types.CollectionHandlerFactory;
import be.nabu.libs.types.ComplexContentWrapperFactory;
import be.nabu.libs.types.ParsedPath;
import be.nabu.libs.types.TypeUtils;
import be.nabu.libs.types.api.CollectionHandlerProvider;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.api.SimpleType;
import be.nabu.libs.types.java.BeanType;

public class ServiceRuntime {

	public static final String METRIC_CACHE_RETRIEVE = "cacheRetrieve";
	public static final String METRIC_CACHE_STORE = "cacheStore";
	public static final String METRIC_CACHE_HIT = "cacheHit";
	public static final String METRIC_CACHE_MISS = "cacheMiss";
	public static final String METRIC_CACHE_FAILURE = "cacheFailure";
	public static final String METRIC_EXECUTION_TIME = "executionTime";
	
	public static final String DEFAULT_CACHE_TIMEOUT = "be.nabu.services.cacheTimeout";
	public static final String NO_AUTHENTICATION = "AUTHORIZATION-0";
	public static final String NO_AUTHORIZATION = "AUTHORIZATION-1";
	
	private WeakHashMap<ComplexType, List<ParsedPath>> closeables = new WeakHashMap<ComplexType, List<ParsedPath>>();
	private static ThreadLocal<ServiceRuntime> runtime = new ThreadLocal<ServiceRuntime>();
	private static List<ServiceRuntime> running = Collections.synchronizedList(new ArrayList<ServiceRuntime>());
	
	// allows for a context that can exist cross root service runtime and is managed externally by some component
	private static ThreadLocal<Map<String, Object>> globalContext = new ThreadLocal<Map<String, Object>>();
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

	private ServiceInstance serviceInstance;
	private ComplexContent input;
	private ComplexContent output;
	private Logger logger = LoggerFactory.getLogger(getClass());
	
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

	public ComplexContent run(ComplexContent input) throws ServiceException {
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
		try {
			running.add(this);

			ServiceAuthorizer authorizer = executionContext.getServiceContext().getServiceAuthorizerProvider().getAuthorizer(this);
			if (authorizer != null && !authorizer.canRun(this, input)) {
				throw new ServiceException(executionContext.getSecurityContext().getToken() == null ? NO_AUTHENTICATION : NO_AUTHORIZATION, "Unauthorized");
			}
			
			// map the input so we can inspect it from the service trackers
			this.input = input;
			// the runtime tracker provider might use contextual data (e.g. the service call stack) to determine which trackers to return, so only call it after we have register this service runtime
			this.runtimeTracker = executionContext.getServiceContext().getServiceTrackerProvider().getTracker(this);
			this.cache = executionContext.getServiceContext().getCacheProvider();
			MetricInstance metrics = service instanceof DefinedService ? executionContext.getMetricInstance(((DefinedService) service).getId()) : null;
			
			if (runtimeTracker != null) {
				runtimeTracker.start(service);
			}
			output = null;
			List<ParsedPath> closeables = manageCloseables != null && manageCloseables ? getCloseablePaths(service.getServiceInterface().getOutputDefinition()) : null;
			if ((closeables == null || closeables.isEmpty()) && isAllowCaching() && getCache() != null && service instanceof DefinedService) {
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
				}
			}
			// if there is no cached version, execute the target service
			if (output == null) {
				serviceInstance = service.newInstance();
				MetricTimer timer = metrics == null ? null : metrics.start(METRIC_EXECUTION_TIME);
				output = serviceInstance.execute(getExecutionContext(), input);
				if (timer != null) {
					timer.stop();
				}
				// store the newly calculated result in the cache if applicable
				if ((closeables == null || closeables.isEmpty()) && output != null && isAllowCaching() && getCache() != null && service instanceof DefinedService) {
					Cache serviceCache = getCache().get(((DefinedService) service).getId());
					if (serviceCache != null) {
						timer = metrics == null ? null : metrics.start(METRIC_CACHE_STORE);
						if (!serviceCache.put(input, output) && metrics != null) {
							metrics.increment(METRIC_CACHE_FAILURE, 1);
						}
						if (timer != null) {
							timer.stop();
						}
					}
				}
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
				runtimeTracker.stop(service);
			}
			return output;
		}
		catch (ServiceException e) {
			exception = e;
			if (runtimeTracker != null) {
				runtimeTracker.error(service, e);
			}
			throw e;
		}
		catch (Exception e) {
			exception = new ServiceException(e);
			if (runtimeTracker != null) {
				runtimeTracker.error(service, e);
			}
			throw exception;
		}
		finally {
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
				}
				else {
					getExecutionContext().getTransactionContext().rollback(transactionId);
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
}
