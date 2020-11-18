package be.nabu.libs.services.impl;

import be.nabu.utils.cep.impl.ComplexEventImpl;

public class ServiceComplexEvent extends ComplexEventImpl {
	private Boolean cached;
	private Long threshold;

	public Boolean getCached() {
		return cached;
	}
	public void setCached(Boolean cached) {
		this.cached = cached;
	}
	public Long getThreshold() {
		return threshold;
	}
	public void setThreshold(Long slaThreshold) {
		this.threshold = slaThreshold;
	}
}
