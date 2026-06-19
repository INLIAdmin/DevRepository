package com.reliance.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.reliance.dto.ErrorAnalysisDTO;
import com.reliance.dto.OverallStatsDTO;
import com.reliance.dto.PaymentAnalyticsDTO;
import com.reliance.dto.PaymentChannelStatsDTO;
import com.reliance.dto.PaymentModeStatsDTO;
import com.reliance.repository.SuvidhaTxnRepository;
import com.reliance.repository.TrxTransactionsRepository;
import com.reliance.repository.UpiCollectionsRepository;

import java.time.LocalDate;
import java.util.*;

@Service
public class PaymentAnalyticsService {

    @Autowired
    private TrxTransactionsRepository trxRepository;

    @Autowired
    private SuvidhaTxnRepository suvidhaRepository;

    @Autowired
    private UpiCollectionsRepository upiRepository;

    public PaymentAnalyticsDTO getPaymentAnalytics(LocalDate startDate, LocalDate endDate) {
        PaymentAnalyticsDTO analytics = new PaymentAnalyticsDTO();

        // Get overall statistics
        analytics.setOverallStats(getOverallStats(startDate, endDate));

        // Get RNLIC Online payment statistics
        analytics.setRnlicStats(getRnlicStats(startDate, endDate));

        // Get Third-party payment statistics
        analytics.setSuvidhaStats(getSuvidhaStats(startDate, endDate));

        // Get UPI Collections statistics
        analytics.setUpiStats(getUpiStats(startDate, endDate));

        // Get payment mode wise statistics for RNLIC
        analytics.setPaymentModeStats(getPaymentModeStats(startDate, endDate));

        // Get error analysis
        analytics.setErrorAnalysis(getErrorAnalysis(startDate, endDate));

        return analytics;
    }

    private OverallStatsDTO getOverallStats(LocalDate startDate, LocalDate endDate) {
        OverallStatsDTO stats = new OverallStatsDTO();

        Map<String, Object> rnlicData = trxRepository.getOverallStats(startDate, endDate);
        Map<String, Object> suvidhaData = suvidhaRepository.getOverallStats(startDate, endDate);
        Map<String, Object> upiData = upiRepository.getOverallStats(startDate, endDate);

        long totalTransactions = toLong(rnlicData.get("totalCount"))
                + toLong(suvidhaData.get("totalCount"))
                + toLong(upiData.get("totalCount"));

        long totalSuccessful = toLong(rnlicData.get("successCount"))
                + toLong(suvidhaData.get("successCount"))
                + toLong(upiData.get("successCount"));

        double totalAmount = toDouble(rnlicData.get("totalAmount"))
                + toDouble(suvidhaData.get("totalAmount"))
                + toDouble(upiData.get("totalAmount"));

        double successfulAmount = toDouble(rnlicData.get("successAmount"))
                + toDouble(suvidhaData.get("successAmount"))
                + toDouble(upiData.get("successAmount"));

        stats.setTotalTransactions(totalTransactions);
        stats.setSuccessfulTransactions(totalSuccessful);
        stats.setFailedTransactions(totalTransactions - totalSuccessful);
        stats.setTotalAmount(totalAmount);
        stats.setSuccessfulAmount(successfulAmount);
        stats.setOverallSuccessRate(totalTransactions > 0 ? (double) totalSuccessful / totalTransactions * 100 : 0);

        return stats;
    }

    private PaymentChannelStatsDTO getRnlicStats(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> data = trxRepository.getChannelStats(startDate, endDate);
        return mapToChannelStats(data, "RNLIC Online");
    }

    private PaymentChannelStatsDTO getSuvidhaStats(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> data = suvidhaRepository.getChannelStats(startDate, endDate);
        return mapToChannelStats(data, "Third-party");
    }

    private PaymentChannelStatsDTO getUpiStats(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> data = upiRepository.getChannelStats(startDate, endDate);
        return mapToChannelStats(data, "UPI Collections");
    }

    private PaymentChannelStatsDTO mapToChannelStats(Map<String, Object> data, String channelName) {
        PaymentChannelStatsDTO stats = new PaymentChannelStatsDTO();
        stats.setChannelName(channelName);

        long total = toLong(data.get("totalCount"));
        long success = toLong(data.get("successCount"));
        double amount = toDouble(data.get("totalAmount"));
        double successAmount = toDouble(data.get("successAmount"));

        stats.setTotalTransactions(total);
        stats.setSuccessfulTransactions(success);
        stats.setFailedTransactions(total - success);
        stats.setTotalAmount(amount);
        stats.setSuccessfulAmount(successAmount);
        stats.setSuccessRate(total > 0 ? (double) success / total * 100 : 0);

        return stats;
    }

    private List<PaymentModeStatsDTO> getPaymentModeStats(LocalDate startDate, LocalDate endDate) {
        return trxRepository.getPaymentModeStats(startDate, endDate);
    }

    private ErrorAnalysisDTO getErrorAnalysis(LocalDate startDate, LocalDate endDate) {
        ErrorAnalysisDTO analysis = new ErrorAnalysisDTO();

        analysis.setRnlicErrors(trxRepository.getErrorStats(startDate, endDate));
        analysis.setSuvidhaErrors(suvidhaRepository.getErrorStats(startDate, endDate));
        analysis.setUpiErrors(upiRepository.getErrorStats(startDate, endDate));

        return analysis;
    }

    public Map<String, Object> getPaymentTrends(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> trends = new HashMap<>();

        trends.put("rnlicTrends", trxRepository.getDailyTrends(startDate, endDate));
        trends.put("suvidhaTrends", suvidhaRepository.getDailyTrends(startDate, endDate));
        trends.put("upiTrends", upiRepository.getDailyTrends(startDate, endDate));

        return trends;
    }

    public Map<String, Object> getSuccessRateByGateway(LocalDate startDate, LocalDate endDate) {
        return trxRepository.getSuccessRateByGateway(startDate, endDate);
    }

    private long toLong(Object value) {
        return value == null ? 0L : ((Number) value).longValue();
    }

    private double toDouble(Object value) {
        return value == null ? 0.0 : ((Number) value).doubleValue();
    }
}
