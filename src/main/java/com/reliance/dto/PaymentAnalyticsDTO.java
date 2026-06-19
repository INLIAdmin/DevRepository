package com.reliance.dto;
import java.util.List;

public class PaymentAnalyticsDTO {
    private OverallStatsDTO overallStats;
    private PaymentChannelStatsDTO rnlicStats;
    private PaymentChannelStatsDTO suvidhaStats;
    private PaymentChannelStatsDTO upiStats;
    private List<PaymentModeStatsDTO> paymentModeStats;
    private ErrorAnalysisDTO errorAnalysis;

    // Getters and setters
    public OverallStatsDTO getOverallStats() { return overallStats; }
    public void setOverallStats(OverallStatsDTO overallStats) { this.overallStats = overallStats; }
    
    public PaymentChannelStatsDTO getRnlicStats() { return rnlicStats; }
    public void setRnlicStats(PaymentChannelStatsDTO rnlicStats) { this.rnlicStats = rnlicStats; }
    
    public PaymentChannelStatsDTO getSuvidhaStats() { return suvidhaStats; }
    public void setSuvidhaStats(PaymentChannelStatsDTO suvidhaStats) { this.suvidhaStats = suvidhaStats; }
    
    public PaymentChannelStatsDTO getUpiStats() { return upiStats; }
    public void setUpiStats(PaymentChannelStatsDTO upiStats) { this.upiStats = upiStats; }
    
    public List<PaymentModeStatsDTO> getPaymentModeStats() { return paymentModeStats; }
    public void setPaymentModeStats(List<PaymentModeStatsDTO> paymentModeStats) { this.paymentModeStats = paymentModeStats; }
    
    public ErrorAnalysisDTO getErrorAnalysis() { return errorAnalysis; }
    public void setErrorAnalysis(ErrorAnalysisDTO errorAnalysis) { this.errorAnalysis = errorAnalysis; }
}