package com.reliance.dto;
public class PaymentChannelStatsDTO {
    private String channelName;
    private long totalTransactions;
    private long successfulTransactions;
    private long failedTransactions;
    private double totalAmount;
    private double successfulAmount;
    private double successRate;

    // Getters and setters
    public String getChannelName() { return channelName; }
    public void setChannelName(String channelName) { this.channelName = channelName; }
    
    public long getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(long totalTransactions) { this.totalTransactions = totalTransactions; }
    
    public long getSuccessfulTransactions() { return successfulTransactions; }
    public void setSuccessfulTransactions(long successfulTransactions) { this.successfulTransactions = successfulTransactions; }
    
    public long getFailedTransactions() { return failedTransactions; }
    public void setFailedTransactions(long failedTransactions) { this.failedTransactions = failedTransactions; }
    
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    
    public double getSuccessfulAmount() { return successfulAmount; }
    public void setSuccessfulAmount(double successfulAmount) { this.successfulAmount = successfulAmount; }
    
    public double getSuccessRate() { return successRate; }
    public void setSuccessRate(double successRate) { this.successRate = successRate; }
}