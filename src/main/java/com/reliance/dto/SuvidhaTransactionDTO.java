package com.reliance.dto;

import java.math.BigDecimal;
import java.sql.Date;

public class SuvidhaTransactionDTO {

    private String policyNumber;
    private String customerName;
    private BigDecimal premiumAmount;
    private String policyName;
    private String transactionStatus;
    private Date transactionDate;
    private String Consumer; 
	public String getPolicyNumber() {
		return policyNumber;
	}
	public void setPolicyNumber(String policyNumber) {
		this.policyNumber = policyNumber;
	}
	public String getCustomerName() {
		return customerName;
	}
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}
	public BigDecimal getPremiumAmount() {
		return premiumAmount;
	}
	public void setPremiumAmount(BigDecimal premiumAmount) {
		this.premiumAmount = premiumAmount;
	}
	public String getPolicyName() {
		return policyName;
	}
	public void setPolicyName(String policyName) {
		this.policyName = policyName;
	}
	public String getTransactionStatus() {
		return transactionStatus;
	}
	public void setTransactionStatus(String transactionStatus) {
		this.transactionStatus = transactionStatus;
	}
	public Date getTransactionDate() {
		return transactionDate;
	}
	public void setTransactionDate(Date transactionDate) {
		this.transactionDate = transactionDate;
	}
	
	
	
	public String getConsumer() {
		return Consumer;
	}
	public void setConsumer(String consumer) {
		Consumer = consumer;
	}
	@Override
	public String toString() {
		return "SuvidhaTransactionDTO [policyNumber=" + policyNumber + ", customerName=" + customerName
				+ ", premiumAmount=" + premiumAmount + ", policyName=" + policyName + ", transactionStatus="
				+ transactionStatus + ", transactionDate=" + transactionDate + ", Consumer=" + Consumer + "]";
	}
	

    
    
    // Getters and Setters
}
