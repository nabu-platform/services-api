package be.nabu.libs.services;

import java.util.List;

import be.nabu.libs.authentication.api.PermissionHandler;
import be.nabu.libs.authentication.api.RoleHandler;
import be.nabu.libs.authentication.api.Token;
import be.nabu.libs.services.api.SecurityContext;

public class SimpleSecurityContext implements SecurityContext {

	private Token token;
	
	public SimpleSecurityContext() {
		// auto construct
	}
	public SimpleSecurityContext(Token token) {
		this.token = token;
	}
	@Override
	public Token getToken() {
		return token;
	}
	@Override
	public RoleHandler getRoleHandler() {
		return null;
	}
	@Override
	public PermissionHandler getPermissionHandler() {
		return null;
	}
	@Override
	public List<Token> getAlternateTokens() {
		return null;
	}
}
