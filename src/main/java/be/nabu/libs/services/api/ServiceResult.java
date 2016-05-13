package be.nabu.libs.services.api;

import be.nabu.libs.types.api.ComplexContent;

public interface ServiceResult {
	public ComplexContent getOutput();
	public ServiceException getException();
}
