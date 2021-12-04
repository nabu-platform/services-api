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
