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

package be.nabu.libs.services.fixed;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import be.nabu.libs.services.api.Service;
import be.nabu.libs.services.api.ServiceInstance;
import be.nabu.libs.services.api.ServiceInterface;
import be.nabu.libs.services.api.ServiceWrapper;

public class FixedInputService implements ServiceWrapper {

	private Service service;
	
	private Map<String, Object> fixedInputs = new HashMap<String, Object>();

	public FixedInputService(Service service) {
		this.service = service;
	}
	
	@Override
	public ServiceInterface getServiceInterface() {
		return service.getServiceInterface();
	}

	@Override
	public ServiceInstance newInstance() {
		return new FixedInputServiceInstance(this);
	}

	@Deprecated
	@Override
	public Set<String> getReferences() {
		return service.getReferences();
	}

	public void setInput(String name, Object object) {
		fixedInputs.put(name, object);
	}
	
	Object getInput(String name) {
		return fixedInputs.get(name);
	}
	
	Collection<String> getInputKeys() {
		return fixedInputs.keySet();
	}
	
	@Override
	public Service getOriginal() {
		return service;
	}
}
