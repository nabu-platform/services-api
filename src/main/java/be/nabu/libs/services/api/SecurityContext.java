package be.nabu.libs.services.api;

import java.util.List;

import be.nabu.libs.authentication.api.PermissionHandler;
import be.nabu.libs.authentication.api.RoleHandler;
import be.nabu.libs.authentication.api.Token;

public interface SecurityContext {
	// the main token for this execution, it is linked to the realm that created the context
	public Token getToken();
	// alternate tokens for different realms, sometimes you want cross-realm actions to be allowed
	public List<Token> getAlternateTokens();
	// a role handler that can validate the roles a token has
	public RoleHandler getRoleHandler();
	// the permission handler can check if a token can perform a certain action
	public PermissionHandler getPermissionHandler();
}
