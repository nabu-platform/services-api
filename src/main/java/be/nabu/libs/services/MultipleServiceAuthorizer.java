package be.nabu.libs.services;

import java.util.Collection;

import be.nabu.libs.services.api.ServiceAuthorizer;
import be.nabu.libs.types.api.ComplexContent;

public class MultipleServiceAuthorizer implements ServiceAuthorizer {

	private Collection<ServiceAuthorizer> authorizers;
	private boolean denyWins;

	public MultipleServiceAuthorizer(Collection<ServiceAuthorizer> authorizers, boolean denyWins) {
		this.authorizers = authorizers;
		this.denyWins = denyWins;
	}
	
	@Override
	public boolean canRun(ServiceRuntime runtime, ComplexContent input) {
		for (ServiceAuthorizer authorizer : authorizers) {
			if (!authorizer.canRun(runtime, input)) {
				if (denyWins) {
					return false;
				}
			}
			else {
				if (!denyWins) {
					return true;
				}
			}
		}
		return true;
	}

}
