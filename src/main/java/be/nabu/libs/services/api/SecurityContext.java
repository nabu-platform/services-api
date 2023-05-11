package be.nabu.libs.services.api;

import java.security.Principal;
import java.util.List;

import be.nabu.libs.authentication.api.Device;
import be.nabu.libs.authentication.api.PermissionHandler;
import be.nabu.libs.authentication.api.RoleHandler;
import be.nabu.libs.authentication.api.Token;
import be.nabu.libs.authentication.api.principals.DevicePrincipal;

public interface SecurityContext {
	// the main token for this execution, it is linked to the realm that created the context
	public Token getToken();
	// alternate tokens for different realms, sometimes you want cross-realm actions to be allowed
	public List<Token> getAlternateTokens();
	// a role handler that can validate the roles a token has
	public RoleHandler getRoleHandler();
	// the permission handler can check if a token can perform a certain action
	public PermissionHandler getPermissionHandler();
	// get the device, by default we will try to get it from the token
	public default Device getDevice() {
		Token token = getToken();
		Device device = null;
		if (token != null && token instanceof DevicePrincipal) {
			device = ((DevicePrincipal) token).getDevice();
		}
		if (device == null && token != null && token.getCredentials() != null && !token.getCredentials().isEmpty()) {
			for (Principal credential : token.getCredentials()) {
				if (credential instanceof DevicePrincipal) {
					device = ((DevicePrincipal) credential).getDevice();
					if (device != null) {
						break;
					}
				}
			}
		}
		return device;
	}
}
