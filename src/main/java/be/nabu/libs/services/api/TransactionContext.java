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

import java.util.Collection;

/**
 * You can iterate over the still open transactions
 */
public interface TransactionContext extends Iterable<String> {
	public String start();
	public void commit(String transactionId);
	public void rollback(String transactionId);
	/**
	 * Add at the end of the transaction queue
	 */
	public void add(String transactionId, Transactionable transactionable);
	/**
	 * Add at the beginning of the transaction queue
	 */
	public void push(String transactionId, Transactionable transactionable);
	public Transactionable get(String transactionId, String resourceId);
	public Collection<Transactionable> getAll(String transactionId);
	
	public String getDefaultTransactionId();
	public void setDefaultTransactionId(String defaultTransactionId);
}
