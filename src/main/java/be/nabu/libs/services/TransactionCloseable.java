package be.nabu.libs.services;

import java.io.Closeable;
import java.io.IOException;

import be.nabu.libs.services.api.Transactionable;

public class TransactionCloseable implements Transactionable {

	private Closeable closeable;
	private boolean disabled;

	public TransactionCloseable(Closeable closeable) {
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
			catch (IOException e) {
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
			catch (IOException e) {
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

	public Closeable getCloseable() {
		return closeable;
	}
	
}
