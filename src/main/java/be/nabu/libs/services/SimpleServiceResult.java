package be.nabu.libs.services;

import be.nabu.libs.services.api.ServiceException;
import be.nabu.libs.services.api.ServiceResult;
import be.nabu.libs.types.api.ComplexContent;

public class SimpleServiceResult implements ServiceResult {

	private ServiceException exception;
	private ComplexContent output;

	public SimpleServiceResult(ComplexContent output, ServiceException exception) {
		this.output = output;
		this.exception = exception;
	}
	
	@Override
	public ComplexContent getOutput() {
		return output;
	}

	@Override
	public ServiceException getException() {
		return exception;
	}
}
