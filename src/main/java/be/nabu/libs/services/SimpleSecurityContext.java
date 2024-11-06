/*
* Copyright (C) 2016 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

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
