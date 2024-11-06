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

package be.nabu.libs.services.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;

import javax.jws.WebParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.nabu.libs.authentication.api.Token;
import be.nabu.libs.services.ServiceRuntime;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.services.api.Service;
import be.nabu.libs.services.api.ServiceException;
import be.nabu.libs.services.api.ServiceRuntimeTracker;

public class ServiceRunReporter implements ServiceRuntimeTracker {

	private ServiceRunReport report;
	private ServiceRunReportHandler handler;
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	public static interface ServiceRunReportHandler {
		public void handle(@WebParam(name = "report") ServiceRunReport report);
	}
	
	public ServiceRunReporter(ServiceRunReportHandler handler) {
		this.handler = handler;
	}
	
	@Override
	public void report(Object object) {
		if (report != null) {
			if (report.getReports() == null) {
				report.setReports(new ArrayList<Object>());
			}
			report.getReports().add(object);
		}
	}

	@Override
	public void start(Service service) {
		if (report == null) {
			report = new ServiceRunReport();
			report.setCorrelationId(getCorrelationId());
		}
		else {
			ServiceRunReport child = new ServiceRunReport();
			child.setParent(report);
			if (report.getChildren() == null) {
				report.setChildren(new ArrayList<ServiceRunReport>());
			}
			report.getChildren().add(child);
			report = child;
		}
		report.setService(service instanceof DefinedService ? ((DefinedService) service).getId() : "$anonymous");
		report.setStarted(new Date());
		Token token = ServiceRuntime.getRuntime().getExecutionContext().getSecurityContext().getToken();
		if (token != null) {
			report.setAlias(token.getName());
			report.setRealm(token.getRealm());
		}
	}

	@Override
	public void stop(Service service) {
		report.setStopped(new Date());
		try {
			if (report.getParent() == null) {
				handler.handle(report);
			}
		}
		catch (Exception e) {
			logger.error("Could not handle the report", e);
			throw new RuntimeException(e);
		}
		finally {
			report = report.getParent();
		}
	}

	@Override
	public void error(Service service, Exception exception) {
		report.setStopped(new Date());
		ServiceException serviceException = getServiceException(exception);
		if (serviceException != null) {
			report.setErrorCode(serviceException.getCode());
		}
		StringWriter writer = new StringWriter();
		PrintWriter printer = new PrintWriter(writer);
		exception.printStackTrace(printer);
		printer.flush();
		report.setStacktrace(writer.toString());
	}

	@Override
	public void before(Object step) {
		// ignore
	}

	@Override
	public void after(Object step) {
		// ignore
	}

	@Override
	public void error(Object step, Exception exception) {
		// ignore
	}

	private ServiceException getServiceException(Throwable throwable) {
		ServiceException serviceException = null;
		// the deepest service exception (if there are multiple) is what we are interested in
		while(throwable != null) {
			if (throwable instanceof ServiceException && ((ServiceException) throwable).getCode() != null) {
				serviceException = (ServiceException) throwable;
			}
			throwable = throwable.getCause();
		}
		return serviceException;
	}
	
	private String getCorrelationId() {
		ServiceRuntime runtime = ServiceRuntime.getRuntime();
		return runtime == null ? null : (String) runtime.getContext().get("correlationId");
	}
}
