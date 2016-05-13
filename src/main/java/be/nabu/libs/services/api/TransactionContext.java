package be.nabu.libs.services.api;

import java.util.Collection;

/**
 * You can iterate over the still open transactions
 */
public interface TransactionContext extends Iterable<String> {
	public String start();
	public void commit(String transactionId);
	public void rollback(String transactionId);
	public void add(String transactionId, Transactionable transactionable);
	public Transactionable get(String transactionId, String resourceId);
	public Collection<Transactionable> getAll(String transactionId);
}
