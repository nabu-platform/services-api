package be.nabu.libs.services.api;

import be.nabu.libs.authentication.api.Token;

public interface ExecutionContextProvider {
	public ExecutionContext newExecutionContext(Token primaryToken, Token...alternativeTokens);
}
