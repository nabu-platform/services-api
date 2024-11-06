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

import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.services.api.DefinedServiceResolver;

public class MultipleDefinedServiceResolver implements DefinedServiceResolver {

	private List<DefinedServiceResolver> resolvers;
	
	public MultipleDefinedServiceResolver(List<DefinedServiceResolver> resolvers) {
		this.resolvers = resolvers;
	}
	
	@Override
	public DefinedService resolve(String id) {
		DefinedService service = null;
		for (DefinedServiceResolver resolver : resolvers) {
			service = resolver.resolve(id);
			if (service != null)
				break;
		}
		return service;
	}

}
