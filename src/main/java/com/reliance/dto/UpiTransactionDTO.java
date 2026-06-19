package com.reliance.dto;

import java.math.BigDecimal;
import java.sql.Date;

public class UpiTransactionDTO {

    private String policyNumber;
    private BigDecimal amount;
    private String customerUPIID;
    private String transactionKey;
    private String transactionStatus;
    private String bankCode;
    private String source;
    private Date transactionDate;
	public String getPolicyNumber() {
		return policyNumber;
	}
	public void setPolicyNumber(String policyNumber) {
		this.policyNumber = policyNumber;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public String getCustomerUPIID() {
		return customerUPIID;
	}
	public void setCustomerUPIID(String customerUPIID) {
		this.customerUPIID = customerUPIID;
	}
	public String getTransactionKey() {
		return transactionKey;
	}
	public void setTransactionKey(String transactionKey) {
		this.transactionKey = transactionKey;
	}
	public String getTransactionStatus() {
		return transactionStatus;
	}
	public void setTransactionStatus(String transactionStatus) {
		this.transactionStatus = transactionStatus;
	}
	public String getBankCode() {
		return bankCode;
	}
	public void setBankCode(String bankCode) {
		this.bankCode = bankCode;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public Date getTransactionDate() {
		return transactionDate;
	}
	public void setTransactionDate(Date transactionDate) {
		this.transactionDate = transactionDate;
	}
	@Override
	public String toString() {
		return "UpiTransactionDTO [policyNumber=" + policyNumber + ", amount=" + amount + ", customerUPIID="
				+ customerUPIID + ", transactionKey=" + transactionKey + ", transactionStatus=" + transactionStatus
				+ ", bankCode=" + bankCode + ", source=" + source + ", transactionDate=" + transactionDate + "]";
	}
    
    

    // Getters and Setters
}
