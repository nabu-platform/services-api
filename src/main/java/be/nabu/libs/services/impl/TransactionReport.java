package be.nabu.libs.services.impl;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "transaction")
public class TransactionReport {
	private String transactionId;
	private String action;
	public TransactionReport() {
		// auto construct
	}
	public TransactionReport(String transactionId, String action) {
		this.transactionId = transactionId;
		this.action = action;
	}
	public String getTransactionId() {
		return transactionId;
	}
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
}
