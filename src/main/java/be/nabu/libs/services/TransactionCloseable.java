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

import be.nabu.libs.services.api.Transactionable;

public class TransactionCloseable implements Transactionable {

	private AutoCloseable closeable;
	private boolean disabled;

	public TransactionCloseable(AutoCloseable closeable) {
		this.closeable = closeable;
	}
	
	@Override
	public String getId() {
		return null;
	}

	@Override
	public void start() {
		// do nothing
	}

	@Override
	public void commit() {
		if (!disabled) {
			try {
				closeable.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void rollback() {
		if (!disabled) {
			try {
				closeable.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean equals(Object object) {
		return object instanceof TransactionCloseable && ((TransactionCloseable) object).closeable.equals(closeable);
	}
	
	@Override
	public int hashCode() {
		return closeable.hashCode();
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public AutoCloseable getCloseable() {
		return closeable;
	}
	
}
