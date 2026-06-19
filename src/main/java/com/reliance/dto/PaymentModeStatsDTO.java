package com.reliance.dto;
public class PaymentModeStatsDTO {
    private String paymentMode;
    private long totalTransactions;
    private long successfulTransactions;
    private double totalAmount;
    private double successfulAmount;
    private double successRate;

    // Getters and setters
    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }
    
    public long getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(long totalTransactions) { this.totalTransactions = totalTransactions; }
    
    public long getSuccessfulTransactions() { return successfulTransactions; }
    public void setSuccessfulTransactions(long successfulTransactions) { this.successfulTransactions = successfulTransactions; }
    
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    
    public double getSuccessfulAmount() { return successfulAmount; }
    public void setSuccessfulAmount(double successfulAmount) { this.successfulAmount = successfulAmount; }
    
    public double getSuccessRate() { return successRate; }
    public void setSuccessRate(double successRate) { this.successRate = successRate; }
}