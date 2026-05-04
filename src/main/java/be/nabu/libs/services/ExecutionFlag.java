package be.nabu.libs.services;

import be.nabu.utils.cep.api.EventSeverity;

/**
 * Add flags to the current execution which can be used to guide decisions like which logs to keep
 */
public enum ExecutionFlag {
	// when inside a loop, we likely don't want to keep events because they will explode
	LOOP(EventSeverity.TRACE);
	
	private EventSeverity severity;

	private ExecutionFlag(EventSeverity severity) {
		this.severity = severity;
	}

	public EventSeverity getSeverity() {
		return severity;
	}
}
