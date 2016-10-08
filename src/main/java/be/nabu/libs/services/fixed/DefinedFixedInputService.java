package be.nabu.libs.services.fixed;

import be.nabu.libs.services.api.DefinedService;

public class DefinedFixedInputService extends FixedInputService implements DefinedService {

	public DefinedFixedInputService(DefinedService service) {
		super(service);
	}

	@Override
	public String getId() {
		return ((DefinedService) getOriginal()).getId();
	}

}
