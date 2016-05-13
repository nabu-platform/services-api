package be.nabu.libs.services.api;

public interface Transactionable {
	/**
	 * A transaction is over a specific resource
	 * If you want to reuse the transaction (e.g. to combine all actions on a single resource), you need to know which one it is
	 */
	public String getId();
	public void start();
	public void commit();
	public void rollback();
}
