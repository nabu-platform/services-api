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

import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "serviceRun")
public class ServiceRunReport {
	// the service context that was active at the time
	private String serviceContext;
	// any correlation id you set to link this run to something
	private String correlationId;
	// when the run started & stopped
	private Date started, stopped;
	// the service that ran
	private String service;
	// any reports you encountered
	private List<Object> reports;
	// any child services you encountered
	private List<ServiceRunReport> children;
	// the stacktrace (if any)
	private String stacktrace, errorCode;
	// the parent
	private ServiceRunReport parent;
	// the user
	private String alias, realm;
	
	public String getServiceContext() {
		return serviceContext;
	}
	public void setServiceContext(String serviceContext) {
		this.serviceContext = serviceContext;
	}
	public String getCorrelationId() {
		return correlationId;
	}
	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}
	public Date getStarted() {
		return started;
	}
	public void setStarted(Date started) {
		this.started = started;
	}
	public Date getStopped() {
		return stopped;
	}
	public void setStopped(Date stopped) {
		this.stopped = stopped;
	}
	public String getService() {
		return service;
	}
	public void setService(String service) {
		this.service = service;
	}
	public List<Object> getReports() {
		return reports;
	}
	public void setReports(List<Object> reports) {
		this.reports = reports;
	}
	public List<ServiceRunReport> getChildren() {
		return children;
	}
	public void setChildren(List<ServiceRunReport> children) {
		this.children = children;
	}
	public String getStacktrace() {
		return stacktrace;
	}
	public void setStacktrace(String stacktrace) {
		this.stacktrace = stacktrace;
	}
	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	@XmlTransient
	public ServiceRunReport getParent() {
		return parent;
	}
	public void setParent(ServiceRunReport parent) {
		this.parent = parent;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public String getRealm() {
		return realm;
	}
	public void setRealm(String realm) {
		this.realm = realm;
	}
}
