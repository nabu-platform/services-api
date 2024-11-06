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

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import be.nabu.libs.services.api.DefinedServiceInterface;
import be.nabu.libs.services.api.DefinedServiceInterfaceResolver;

public class SPIDefinedServiceInterfaceResolver implements DefinedServiceInterfaceResolver {

	private static DefinedServiceInterfaceResolver resolver;
	
	@Override
	public DefinedServiceInterface resolve(String id) {
		return getResolver().resolve(id);
	}

	private DefinedServiceInterfaceResolver getResolver() {
		if (resolver == null) {
			synchronized(this) {
				if (resolver == null) {
					List<DefinedServiceInterfaceResolver> resolvers = new ArrayList<DefinedServiceInterfaceResolver>();
					ServiceLoader<DefinedServiceInterfaceResolver> serviceLoader = ServiceLoader.load(DefinedServiceInterfaceResolver.class);
					for (DefinedServiceInterfaceResolver resolver : serviceLoader) {
						resolvers.add(resolver);
					}
					resolver = new MultipleDefinedServiceInterfaceResolver(resolvers);
				}
			}
		}
		return resolver;
	}
}
