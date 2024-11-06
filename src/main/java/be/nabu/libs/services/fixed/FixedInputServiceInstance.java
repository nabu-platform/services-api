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

import be.nabu.libs.services.api.ExecutionContext;
import be.nabu.libs.services.api.Service;
import be.nabu.libs.services.api.ServiceException;
import be.nabu.libs.services.api.ServiceInstance;
import be.nabu.libs.types.BaseTypeInstance;
import be.nabu.libs.types.TypeConverterFactory;
import be.nabu.libs.types.TypeUtils;
import be.nabu.libs.types.api.ComplexContent;

public class FixedInputServiceInstance implements ServiceInstance {

	private FixedInputService definition;
	private ServiceInstance instance;

	FixedInputServiceInstance(FixedInputService definition) {
		this.definition = definition;
		this.instance = definition.getOriginal().newInstance();
	}
	
	@Override
	public Service getDefinition() {
		return definition;
	}

	@Override
	public ComplexContent execute(ExecutionContext executionContext, ComplexContent input) throws ServiceException {
		// if the type of the input is not the same and not an extension, try to convert it
		if (!(input.getType().equals(definition.getOriginal().getServiceInterface().getInputDefinition())) && TypeUtils.getUpcastPath(input.getType(), definition.getOriginal().getServiceInterface().getInputDefinition()).isEmpty()) {
			Object converted = TypeConverterFactory.getInstance().getConverter().convert(input, new BaseTypeInstance(input.getType()), new BaseTypeInstance(definition.getOriginal().getServiceInterface().getInputDefinition()));
			if (converted == null) {
				throw new ServiceException("FIXED-1", "Can not convert input to requested format");
			}
			input = (ComplexContent) converted;
		}
		for (String key : definition.getInputKeys()) {
			input.set(key, definition.getInput(key));
		}
		return instance.execute(executionContext, input);
	}

}
