package be.nabu.libs.services.api;

public interface ServiceWrapper extends Service {
	public Service getOriginal();
}
