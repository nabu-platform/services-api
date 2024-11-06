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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import be.nabu.libs.services.api.TransactionContext;
import be.nabu.libs.services.api.Transactionable;

public class SimpleTransactionContext implements TransactionContext {
	
	private Map<String, List<Transactionable>> transactions = new HashMap<String, List<Transactionable>>();
	private String defaultTransactionId = "default";

	@Override
	public String start() {
		return UUID.randomUUID().toString();
	}

	@Override
	public void commit(String transactionId) {
		if (transactionId == null) {
			transactionId = defaultTransactionId;
		}
		List<Transactionable> list = transactions.get(transactionId);
		if (list != null) {
			Exception lastException = null;
			// this avoids concurrent modification exception if new stuff is added while we close stuff
			while (list != null && list.size() > 0) {
				Transactionable transactionable = list.remove(0);
				try {
					transactionable.commit();
				}
				catch (Exception e) {
					lastException = e;
				}
				list = transactions.get(transactionId);
			}
			transactions.remove(transactionId);
			if (lastException != null) {
				if (lastException instanceof RuntimeException) {
					throw (RuntimeException) lastException;
				}
				else {
					throw new RuntimeException(lastException);
				}
			}
		}
	}

	@Override
	public void rollback(String transactionId) {
		if (transactionId == null) {
			transactionId = defaultTransactionId;
		}
		List<Transactionable> list = transactions.get(transactionId);
		if (list != null) {
			Exception lastException = null;
			while (list != null && list.size() > 0) {
				Transactionable transactionable = list.remove(0);
				try {
					transactionable.rollback();
				}
				catch (Exception e) {
					lastException = e;
				}
				list = transactions.get(transactionId);
			}
			transactions.remove(transactionId);
			if (lastException != null) {
				if (lastException instanceof RuntimeException) {
					throw (RuntimeException) lastException;
				}
				else {
					throw new RuntimeException(lastException);
				}
			}
		}
	}

	@Override
	public void add(String transactionId, Transactionable transactionable) {
		if (transactionId == null) {
			transactionId = defaultTransactionId;
		}
		if (!transactions.containsKey(transactionId)) {
			transactions.put(transactionId, new ArrayList<Transactionable>());
		}
		transactions.get(transactionId).add(transactionable);
	}

	@Override
	public Transactionable get(String transactionId, String resourceId) {
		if (transactionId == null) {
			transactionId = defaultTransactionId;
		}
		if (transactions.containsKey(transactionId)) {
			for (Transactionable transactionable : transactions.get(transactionId)) {
				if (resourceId.equals(transactionable.getId())) {
					return transactionable;
				}
			}
		}
		return null;
	}

	@Override
	public Iterator<String> iterator() {
		return new ArrayList<String>(transactions.keySet()).iterator();
	}

	@Override
	public Collection<Transactionable> getAll(String transactionId) {
		if (transactionId == null) {
			transactionId = defaultTransactionId;
		}
		return transactions.get(transactionId);
	}

	@Override
	public void push(String transactionId, Transactionable transactionable) {
		if (transactionId == null) {
			transactionId = defaultTransactionId;
		}
		if (!transactions.containsKey(transactionId)) {
			transactions.put(transactionId, new ArrayList<Transactionable>());
		}
		transactions.get(transactionId).add(0, transactionable);
	}

	@Override
	public String getDefaultTransactionId() {
		return defaultTransactionId;
	}

	@Override
	public void setDefaultTransactionId(String defaultTransactionId) {
		this.defaultTransactionId = defaultTransactionId;
	}
	
}

