package be.nabu.libs.services.api;

import be.nabu.libs.types.api.ComplexContent;

public interface ServiceInstanceWithPipeline extends ServiceInstance {
	public ComplexContent getPipeline();
}
