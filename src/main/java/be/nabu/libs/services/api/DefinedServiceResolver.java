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

package be.nabu.libs.services.api;

import be.nabu.libs.artifacts.api.ArtifactResolver;

/**
 * The service resolver can go very far
 * You can introduce an execution context with security and only resolve if the user has access to it for example
 */
public interface DefinedServiceResolver extends ArtifactResolver<DefinedService> {
	@Override
	public DefinedService resolve(String id);
}
