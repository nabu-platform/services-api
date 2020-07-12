package be.nabu.libs.services.api;

import be.nabu.libs.authentication.api.Token;

public interface UpgradeableSecurityContext extends SecurityContext {
	public void upgrade(Token token, Token...alternateTokens);
}
