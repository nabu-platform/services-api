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

import java.util.Collection;

import be.nabu.libs.services.api.ServiceAuthorizer;
import be.nabu.libs.types.api.ComplexContent;

public class MultipleServiceAuthorizer implements ServiceAuthorizer {

	private Collection<ServiceAuthorizer> authorizers;
	private boolean denyWins;

	public MultipleServiceAuthorizer(Collection<ServiceAuthorizer> authorizers, boolean denyWins) {
		this.authorizers = authorizers;
		this.denyWins = denyWins;
	}
	
	@Override
	public boolean canRun(ServiceRuntime runtime, ComplexContent input) {
		for (ServiceAuthorizer authorizer : authorizers) {
			if (!authorizer.canRun(runtime, input)) {
				if (denyWins) {
					return false;
				}
			}
			else {
				if (!denyWins) {
					return true;
				}
			}
		}
		return true;
	}

}
