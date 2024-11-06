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

import java.util.Set;

public interface Service {
	public ServiceInterface getServiceInterface();
	public ServiceInstance newInstance();
	public default String getDescription() {
		return null;
	}
	
	/**
	 * This should return a list of reference ids
	 * This should contain all the things that are resolved through "a" resolver
	 * Currently there is no distinction in what you are actually resolving (e.g. a service, a type,...)
	 * This is necessary to build a dependency map to calculate impact and update changes
	 * For types this is less necessary because they are traversable, services are entirely opaque though
	 * Note that this is now performed by the manager
	 */
	@Deprecated
	public Set<String> getReferences();
}
