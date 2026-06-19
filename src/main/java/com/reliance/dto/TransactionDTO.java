package com.reliance.dto;

import java.math.BigDecimal;
import java.sql.Date;

import com.opencsv.bean.CsvBindByPosition;

public class TransactionDTO {
	@CsvBindByPosition(position = 0)
    private String policyNumber;

    @CsvBindByPosition(position = 1)
    private String customerName;

    @CsvBindByPosition(position = 2)
    private BigDecimal amount;

    @CsvBindByPosition(position = 3)
    private String mobileNumber;

    @CsvBindByPosition(position = 4)
    private String emailID;

    @CsvBindByPosition(position = 5)
    private String transactionKey;

    @CsvBindByPosition(position = 6)
    private String paymentGateway;

    @CsvBindByPosition(position = 7)
    private String paymentOption;

    @CsvBindByPosition(position = 8)
    private int transactionStatus;

    @CsvBindByPosition(position = 9)
    private String nameOnCard;

    @CsvBindByPosition(position = 10)
    private String relationWithAssured;

    @CsvBindByPosition(position = 11)
    private String convenienceApplied;

    @CsvBindByPosition(position = 12)
    private BigDecimal convenienceAmount;

    @CsvBindByPosition(position = 13)
    private BigDecimal amountDeductedAfterConvenience;

    @CsvBindByPosition(position = 14)
    private Date transactionDate;
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
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public String getMobileNumber() {
		return mobileNumber;
	}
	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}
	public String getEmailID() {
		return emailID;
	}
	public void setEmailID(String emailID) {
		this.emailID = emailID;
	}
	public String getTransactionKey() {
		return transactionKey;
	}
	public void setTransactionKey(String transactionKey) {
		this.transactionKey = transactionKey;
	}
	public String getPaymentGateway() {
		return paymentGateway;
	}
	public void setPaymentGateway(String paymentGateway) {
		this.paymentGateway = paymentGateway;
	}
	public String getPaymentOption() {
		return paymentOption;
	}
	public void setPaymentOption(String paymentOption) {
		this.paymentOption = paymentOption;
	}
	public int getTransactionStatus() {
		return transactionStatus;
	}
	public void setTransactionStatus(int transactionStatus) {
		this.transactionStatus = transactionStatus;
	}
	public String getNameOnCard() {
		return nameOnCard;
	}
	public void setNameOnCard(String nameOnCard) {
		this.nameOnCard = nameOnCard;
	}
	public String getRelationWithAssured() {
		return relationWithAssured;
	}
	public void setRelationWithAssured(String relationWithAssured) {
		this.relationWithAssured = relationWithAssured;
	}
	public String getConvenienceApplied() {
		return convenienceApplied;
	}
	public void setConvenienceApplied(String convenienceApplied) {
		this.convenienceApplied = convenienceApplied;
	}
	public BigDecimal getConvenienceAmount() {
		return convenienceAmount;
	}
	public void setConvenienceAmount(BigDecimal convenienceAmount) {
		this.convenienceAmount = convenienceAmount;
	}
	public BigDecimal getAmountDeductedAfterConvenience() {
		return amountDeductedAfterConvenience;
	}
	public void setAmountDeductedAfterConvenience(BigDecimal amountDeductedAfterConvenience) {
		this.amountDeductedAfterConvenience = amountDeductedAfterConvenience;
	}
	public Date getTransactionDate() {
		return transactionDate;
	}
	public void setTransactionDate(Date transactionDate) {
		this.transactionDate = transactionDate;
	}
	@Override
	public String toString() {
		return "TransactionDTO [policyNumber=" + policyNumber + ", customerName=" + customerName + ", amount=" + amount
				+ ", mobileNumber=" + mobileNumber + ", emailID=" + emailID + ", transactionKey=" + transactionKey
				+ ", paymentGateway=" + paymentGateway + ", paymentOption=" + paymentOption + ", transactionStatus="
				+ transactionStatus + ", nameOnCard=" + nameOnCard + ", relationWithAssured=" + relationWithAssured
				+ ", convenienceApplied=" + convenienceApplied + ", convenienceAmount=" + convenienceAmount
				+ ", amountDeductedAfterConvenience=" + amountDeductedAfterConvenience + ", transactionDate="
				+ transactionDate + "]";
	}

    
    
    // Getters and setters
}
