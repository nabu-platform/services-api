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

package be.nabu.libs.services.impl;

import be.nabu.utils.cep.impl.ComplexEventImpl;

public class ServiceComplexEvent extends ComplexEventImpl {
	private Boolean cached;
	private Long threshold;

	public Boolean getCached() {
		return cached;
	}
	public void setCached(Boolean cached) {
		this.cached = cached;
	}
	public Long getThreshold() {
		return threshold;
	}
	public void setThreshold(Long slaThreshold) {
		this.threshold = slaThreshold;
	}
}
