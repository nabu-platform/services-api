package be.nabu.libs.services.api;

public interface ModifiableServiceInterface extends ServiceInterface {
	public void setParent(ServiceInterface parent);
}
