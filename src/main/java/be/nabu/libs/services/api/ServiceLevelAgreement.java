package be.nabu.libs.services.api;

import be.nabu.utils.cep.api.EventSeverity;

public interface ServiceLevelAgreement {
	public Long getThresholdDuration();
	public EventSeverity getSeverity();
	// whether it is an explicit agreement or implicit
	// implicit can be if it is a derivative
	public boolean isExplicit();
}
