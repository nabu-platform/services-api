package be.nabu.libs.services.fixed;

import be.nabu.libs.services.api.ExecutionContext;
import be.nabu.libs.services.api.Service;
import be.nabu.libs.services.api.ServiceException;
import be.nabu.libs.services.api.ServiceInstance;
import be.nabu.libs.types.api.ComplexContent;

public class FixedInputServiceInstance implements ServiceInstance {

	private FixedInputService definition;
	private ServiceInstance instance;

	FixedInputServiceInstance(FixedInputService definition) {
		this.definition = definition;
		this.instance = definition.getOriginal().newInstance();
	}
	
	@Override
	public Service getDefinition() {
		return definition;
	}

	@Override
	public ComplexContent execute(ExecutionContext executionContext, ComplexContent input) throws ServiceException {
		for (String key : definition.getInputKeys()) {
			input.set(key, definition.getInput(key));
		}
		return instance.execute(executionContext, input);
	}

}
