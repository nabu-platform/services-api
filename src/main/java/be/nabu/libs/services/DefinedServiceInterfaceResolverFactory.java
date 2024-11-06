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

import be.nabu.libs.services.api.DefinedServiceInterfaceResolver;

public class DefinedServiceInterfaceResolverFactory {

	private static DefinedServiceInterfaceResolverFactory instance;
	private DefinedServiceInterfaceResolver resolver;
	
	public static DefinedServiceInterfaceResolverFactory getInstance() {
		if (instance == null)
			instance = new DefinedServiceInterfaceResolverFactory();
		return instance;
	}
	
	private List<DefinedServiceInterfaceResolver> resolvers = new ArrayList<DefinedServiceInterfaceResolver>();
	
	public DefinedServiceInterfaceResolver getResolver() {
		if (resolver == null) {
			synchronized(this) {
				if (resolver == null) {
					if (resolvers.isEmpty()) {
						resolvers.add(new SPIDefinedServiceInterfaceResolver());
					}
				}
				resolver = new MultipleDefinedServiceInterfaceResolver(resolvers);
			}
		}
		return resolver;
	}
	
	public void addResolver(DefinedServiceInterfaceResolver resolver) {
		resolvers.add(resolver);
		this.resolver = null;
	}
	
	public void removeResolver(DefinedServiceInterfaceResolver resolver) {
		resolvers.remove(resolver);
		this.resolver = null;
	}
	
	@SuppressWarnings("unused")
	private void activate() {
		instance = this;
	}
	@SuppressWarnings("unused")
	private void deactivate() {
		instance = null;
	}
}
