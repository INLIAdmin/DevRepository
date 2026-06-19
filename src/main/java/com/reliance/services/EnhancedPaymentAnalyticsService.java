// Enhanced PaymentAnalyticsService.java - Complete Implementation
package com.reliance.services;

import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.reliance.dto.*;
import com.reliance.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.io.StringWriter;


@Service
public class EnhancedPaymentAnalyticsService extends PaymentAnalyticsService {

    @Autowired
    private TrxTransactionsRepository trxRepository;
    
    @Autowired
    private SuvidhaTxnRepository suvidhaRepository;
    
    @Autowired
    private UpiCollectionsRepository upiRepository;

    public Map<String, Object> getHourlyTrends(LocalDate date) {
        Map<String, Object> trends = new HashMap<>();
        
        // Get hourly trends for each payment channel
        trends.put("rnlicHourly", trxRepository.getHourlyTrends(date));
        trends.put("suvidhaHourly", suvidhaRepository.getHourlyTrends(date));
        trends.put("upiHourly", upiRepository.getHourlyTrends(date));
        
        return trends;
    }

    public String exportToCSV(LocalDate startDate, LocalDate endDate) {
        StringBuilder csv = new StringBuilder();

        // CSV Header
        csv.append("Date,Channel,Total_Transactions,Successful_Transactions,Failed_Transactions,")
           .append("Success_Rate,Total_Amount,Successful_Amount\n");

        // Get daily data for each channel
        List<Map<String, Object>> rnlicData = trxRepository.getDailyTrends(startDate, endDate);
        List<Map<String, Object>> suvidhaData = suvidhaRepository.getDailyTrends(startDate, endDate);
        List<Map<String, Object>> upiData = upiRepository.getDailyTrends(startDate, endDate);

        // Format and append each row
        for (Map<String, Object> row : rnlicData) {
            csv.append(formatCSVRow(row, "RNLIC"));
        }
        for (Map<String, Object> row : suvidhaData) {
            csv.append(formatCSVRow(row, "Suvidha"));
        }
        for (Map<String, Object> row : upiData) {
            csv.append(formatCSVRow(row, "UPI"));
        }

        return csv.toString();
    }
    
    public String exportOnlinePaymentTransaction(LocalDate startDate, LocalDate endDate, String status, String paymentOption) {
        java.sql.Date sqlStart = java.sql.Date.valueOf(startDate);
        java.sql.Date sqlEnd = java.sql.Date.valueOf(endDate);

        List<TransactionDTO> transactions = trxRepository.exportTransactions(sqlStart, sqlEnd, status, paymentOption);

        try (StringWriter writer = new StringWriter()) {
            // Write header manually
            writer.append("PolicyNumber,CustomerName,Amount,MobileNumber,EmailID,TransactionKey,PaymentGateway,PaymentOption,TransactionStatus,NameOnCard,RelationWithAssured,ConvenienceApplied,ConvenienceAmount,AmountDeductedAfterConvenience,TransactionDate\n");

            // Write data rows
            StatefulBeanToCsv<TransactionDTO> csvWriter = new StatefulBeanToCsvBuilder<TransactionDTO>(writer)
                    .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                    .withSeparator(',')
                    .withOrderedResults(true)
                    .withApplyQuotesToAll(false)
                    .build();

            csvWriter.write(transactions);
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate CSV", e);
        }
    }
    
    public String exportThirdPartyTransactionsToCsv(LocalDate start, LocalDate end) {
        Date sqlStart = java.sql.Date.valueOf(start);
        Date sqlEnd = java.sql.Date.valueOf(end);

        List<SuvidhaTransactionDTO> transactions = suvidhaRepository.fetchTransactions(sqlStart, sqlEnd);

        try (StringWriter writer = new StringWriter()) {
            // Add CSV header manually
            writer.append("PolicyNumber,CustomerName,PremiumAmount,PolicyName,Consumer,TransactionStatus,TransactionDate\n");

            // Write data
            StatefulBeanToCsv<SuvidhaTransactionDTO> csvWriter = new StatefulBeanToCsvBuilder<SuvidhaTransactionDTO>(writer)
                    .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                    .withSeparator(',')
                    .withOrderedResults(true)
                    .build();

            csvWriter.write(transactions);
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException("CSV generation failed", e);
        }
    }

    public String exportUPICollectionDataToCsv(LocalDate start, LocalDate end) {
        Date sqlStart = java.sql.Date.valueOf(start);
        Date sqlEnd = java.sql.Date.valueOf(end);

        List<UpiTransactionDTO> transactions = upiRepository.fetchTransactions(sqlStart, sqlEnd);

        try (StringWriter writer = new StringWriter()) {
            // Add header row manually
            writer.append("PolicyNumber,Amount,CustomerUPIID,TransactionKey,TransactionStatus,BankCode,Source,TransactionDate\n");

            StatefulBeanToCsv<UpiTransactionDTO> csvWriter = new StatefulBeanToCsvBuilder<UpiTransactionDTO>(writer)
                    .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                    .withSeparator(',')
                    .withOrderedResults(true)
                    .build();

            csvWriter.write(transactions);
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException("CSV generation failed", e);
        }
    }
    

    private String formatCSVRow(Map<String, Object> row, String channel) {
        LocalDate date = toLocalDate(row.get("transactionDate"));

        long total = toLong(row.get("totalTransactions"));
        long success = toLong(row.get("successfulTransactions"));
        long failed = total - success;

        BigDecimal totalAmount = toBigDecimal(row.get("totalAmount"));
        BigDecimal successfulAmount = toBigDecimal(row.get("successfulAmount"));

        double successRate = total > 0 ? (success * 100.0 / total) : 0.0;

        return String.format("%s,%s,%d,%d,%d,%.2f%%,%.2f,%.2f\n",
                date, channel, total, success, failed, successRate,
                totalAmount.doubleValue(), successfulAmount.doubleValue());
    }


    private long toLong(Object value) {
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).longValue();
        } else if (value instanceof Number) {
            return ((Number) value).longValue();
        } else {
            return 0L;
        }
    }
    
    private LocalDate toLocalDate(Object dateObj) {
        if (dateObj instanceof java.sql.Date) {
            return ((java.sql.Date) dateObj).toLocalDate();
        } else if (dateObj instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) dateObj).toLocalDateTime().toLocalDate();
        } else if (dateObj instanceof LocalDate) {
            return (LocalDate) dateObj;
        } else if (dateObj instanceof java.util.Date) {
            return new java.sql.Date(((java.util.Date) dateObj).getTime()).toLocalDate();
        } else {
            throw new IllegalArgumentException("Unrecognized date type: " + dateObj.getClass());
        }
    }


    private BigDecimal toBigDecimal(Object value) {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        } else if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        } else {
            return BigDecimal.ZERO;
        }
    }



    public Map<String, Object> getDetailedErrorAnalysis(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> analysis = new HashMap<>();
        
        // Get error trends over time
        analysis.put("errorTrends", getErrorTrends(startDate, endDate));
        
        // Get error distribution by channel
        analysis.put("errorDistribution", getErrorDistribution(startDate, endDate));
        
        // Get top error codes with resolution suggestions
        analysis.put("topErrors", getTopErrorsWithSuggestions(startDate, endDate));
        
        return analysis;
    }

    private Map<String, Object> getErrorTrends(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> trends = new HashMap<>();
        
        trends.put("rnlicErrorTrends", trxRepository.getErrorTrends(startDate, endDate));
        trends.put("suvidhaErrorTrends", suvidhaRepository.getErrorTrends(startDate, endDate));
        trends.put("upiErrorTrends", upiRepository.getErrorTrends(startDate, endDate));
        
        return trends;
    }

    private Map<String, Object> getErrorDistribution(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> distribution = new HashMap<>();
        
        // Calculate error percentages for each channel
        Long rnlicErrors = trxRepository.getErrorCount(startDate, endDate);
        Long suvidhaErrors = suvidhaRepository.getErrorCount(startDate, endDate);
        Long upiErrors = upiRepository.getErrorCount(startDate, endDate);
        
        Long totalErrors = rnlicErrors + suvidhaErrors + upiErrors;
        
        if (totalErrors > 0) {
            distribution.put("rnlicErrorPercentage", (double) rnlicErrors / totalErrors * 100);
            distribution.put("suvidhaErrorPercentage", (double) suvidhaErrors / totalErrors * 100);
            distribution.put("upiErrorPercentage", (double) upiErrors / totalErrors * 100);
        } else {
            distribution.put("rnlicErrorPercentage", 0.0);
            distribution.put("suvidhaErrorPercentage", 0.0);
            distribution.put("upiErrorPercentage", 0.0);
        }
        
        distribution.put("totalErrors", totalErrors);
        distribution.put("rnlicErrors", rnlicErrors);
        distribution.put("suvidhaErrors", suvidhaErrors);
        distribution.put("upiErrors", upiErrors);
        
        return distribution;
    }

    private List<Map<String, Object>> getTopErrorsWithSuggestions(LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> topErrors = new ArrayList<>();

        // Get top errors from each channel
        List<Map<String, Object>> rnlicTopErrors = trxRepository.getTopErrors(startDate, endDate, 5);
        List<Map<String, Object>> suvidhaTopErrors = suvidhaRepository.getTopErrors(startDate, endDate, 5);
        List<Map<String, Object>> upiTopErrors = upiRepository.getTopErrors(startDate, endDate, 5);

        // Combine all
        topErrors.addAll(rnlicTopErrors);
        topErrors.addAll(suvidhaTopErrors);
        topErrors.addAll(upiTopErrors);

        // Sort safely by errorCount descending
        topErrors.sort(Comparator.comparingLong(e -> ((Number) e.get("errorCount")).longValue()));
        Collections.reverse(topErrors);

        // Add suggestions
        topErrors.forEach(error -> {
            error.put("resolutionSuggestion", getResolutionSuggestion((String) error.get("errorCode")));
        });

        return topErrors.stream().limit(10).collect(Collectors.toList());
    }


    private String getResolutionSuggestion(String errorCode) {
        Map<String, String> suggestions = new HashMap<>();
        
        // Common error codes and their resolutions
        suggestions.put("TIMEOUT", "Check network connectivity and increase timeout values");
        suggestions.put("INSUFFICIENT_FUNDS", "User education about maintaining sufficient balance");
        suggestions.put("INVALID_CARD", "Implement card validation and user guidance");
        suggestions.put("GATEWAY_ERROR", "Contact payment gateway provider for resolution");
        suggestions.put("AUTHENTICATION_FAILED", "Review authentication process and user credentials");
        suggestions.put("NETWORK_ERROR", "Check network infrastructure and connectivity");
        suggestions.put("INVALID_REQUEST", "Validate request parameters and format");
        suggestions.put("DECLINED", "Review transaction limits and user account status");
        suggestions.put("EXPIRED_CARD", "Prompt user to update card information");
        suggestions.put("BLOCKED_CARD", "Advise user to contact their bank");
        
        return suggestions.getOrDefault(errorCode, "Contact technical support for resolution");
    }

    public Map<String, Object> getPerformanceMetrics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> metrics = new HashMap<>();
        
        // Calculate overall performance metrics
        metrics.put("averageResponseTime", calculateAverageResponseTime(startDate, endDate));
        metrics.put("peakHourAnalysis", getPeakHourAnalysis(startDate, endDate));
        metrics.put("channelComparison", getChannelComparison(startDate, endDate));
        metrics.put("volumeDistribution", getVolumeDistribution(startDate, endDate));
        
        return metrics;
    }

    private Map<String, Object> calculateAverageResponseTime(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> responseTimes = new HashMap<>();
        
        responseTimes.put("rnlicAvgResponseTime", trxRepository.getAverageResponseTime(startDate, endDate));
        responseTimes.put("suvidhaAvgResponseTime", suvidhaRepository.getAverageResponseTime(startDate, endDate));
        responseTimes.put("upiAvgResponseTime", upiRepository.getAverageResponseTime(startDate, endDate));
        
        return responseTimes;
    }

    private Map<String, Object> getPeakHourAnalysis(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> peakAnalysis = new HashMap<>();
        
        // Get hourly transaction counts for the date range
        Map<Integer, Long> hourlyTransactions = new HashMap<>();
        
        // Aggregate data from all channels
        List<Map<String, Object>> rnlicHourly = trxRepository.getHourlyAggregateData(startDate, endDate);
        List<Map<String, Object>> suvidhaHourly = suvidhaRepository.getHourlyAggregateData(startDate, endDate);
        List<Map<String, Object>> upiHourly = upiRepository.getHourlyAggregateData(startDate, endDate);
        
        // Process and combine hourly data
        processHourlyData(hourlyTransactions, rnlicHourly);
        processHourlyData(hourlyTransactions, suvidhaHourly);
        processHourlyData(hourlyTransactions, upiHourly);
        
        // Find peak hours
        List<Map.Entry<Integer, Long>> sortedHours = hourlyTransactions.entrySet()
                .stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .collect(Collectors.toList());
        
        peakAnalysis.put("peakHours", sortedHours.stream()
                .limit(3)
                .map(entry -> Map.of("hour", entry.getKey(), "transactions", entry.getValue()))
                .collect(Collectors.toList()));
        
        peakAnalysis.put("lowHours", sortedHours.stream()
                .skip(Math.max(0, sortedHours.size() - 3))
                .map(entry -> Map.of("hour", entry.getKey(), "transactions", entry.getValue()))
                .collect(Collectors.toList()));
        
        return peakAnalysis;
    }

    private void processHourlyData(Map<Integer, Long> hourlyTransactions, List<Map<String, Object>> data) {
        for (Map<String, Object> row : data) {
            Integer hour = ((Number) row.get("hour")).intValue();
            Long count = ((Number) row.get("transactionCount")).longValue();
            hourlyTransactions.merge(hour, count, Long::sum);
        }
    }


    private Map<String, Object> getChannelComparison(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> comparison = new HashMap<>();
        
        // Get analytics for each channel
        Map<String, Object> rnlicAnalytics = trxRepository.getChannelAnalytics(startDate, endDate);
        Map<String, Object> suvidhaAnalytics = suvidhaRepository.getChannelAnalytics(startDate, endDate);
        Map<String, Object> upiAnalytics = upiRepository.getChannelAnalytics(startDate, endDate);
        
        // Calculate relative performance
        comparison.put("channels", Arrays.asList(
            Map.of("name", "RNLIC", "data", rnlicAnalytics),
            Map.of("name", "Suvidha", "data", suvidhaAnalytics),
            Map.of("name", "UPI", "data", upiAnalytics)
        ));
        
        return comparison;
    }

    private Map<String, Object> getVolumeDistribution(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> distribution = new HashMap<>();
        
        // Get total volumes for each channel
        Long rnlicVolume = trxRepository.getTotalVolume(startDate, endDate);
        Long suvidhaVolume = suvidhaRepository.getTotalVolume(startDate, endDate);
        Long upiVolume = upiRepository.getTotalVolume(startDate, endDate);
        
        Long totalVolume = rnlicVolume + suvidhaVolume + upiVolume;
        
        if (totalVolume > 0) {
            distribution.put("rnlicPercentage", (double) rnlicVolume / totalVolume * 100);
            distribution.put("suvidhaPercentage", (double) suvidhaVolume / totalVolume * 100);
            distribution.put("upiPercentage", (double) upiVolume / totalVolume * 100);
        } else {
            distribution.put("rnlicPercentage", 0.0);
            distribution.put("suvidhaPercentage", 0.0);
            distribution.put("upiPercentage", 0.0);
        }
        
        distribution.put("totalVolume", totalVolume);
        distribution.put("rnlicVolume", rnlicVolume);
        distribution.put("suvidhaVolume", suvidhaVolume);
        distribution.put("upiVolume", upiVolume);
        
        return distribution;
    }

    public Map<String, Object> getRealtimeMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Get real-time metrics for current day
        LocalDate today = LocalDate.now();
        
        metrics.put("currentDayMetrics", getPaymentAnalytics(today, today));
        metrics.put("currentHourMetrics", getCurrentHourMetrics());
        metrics.put("liveTransactionCount", getLiveTransactionCount());
        metrics.put("systemHealth", getSystemHealthStatus());
        
        return metrics;
    }

    private Map<String, Object> getCurrentHourMetrics() {
        Map<String, Object> hourMetrics = new HashMap<>();
        
        // Get current hour metrics from each channel
        hourMetrics.put("rnlicCurrentHour", trxRepository.getCurrentHourMetrics());
        hourMetrics.put("suvidhaCurrentHour", suvidhaRepository.getCurrentHourMetrics());
        hourMetrics.put("upiCurrentHour", upiRepository.getCurrentHourMetrics());
        
        return hourMetrics;
    }

    private Map<String, Object> getLiveTransactionCount() {
        Map<String, Object> liveCount = new HashMap<>();
        
        // Get live transaction counts
        liveCount.put("rnlicLive", trxRepository.getLiveTransactionCount());
        liveCount.put("suvidhaLive", suvidhaRepository.getLiveTransactionCount());
        liveCount.put("upiLive", upiRepository.getLiveTransactionCount());
        
        return liveCount;
    }

    private Map<String, Object> getSystemHealthStatus() {
        Map<String, Object> health = new HashMap<>();
        
        // Check system health indicators
        health.put("rnlicHealth", checkChannelHealth("RNLIC"));
        health.put("suvidhaHealth", checkChannelHealth("Suvidha"));
        health.put("upiHealth", checkChannelHealth("UPI"));
        health.put("overallHealth", calculateOverallHealth());
        
        return health;
    }

    private Map<String, Object> checkChannelHealth(String channel) {
        Map<String, Object> channelHealth = new HashMap<>();
        
        // Basic health check logic
        LocalDate today = LocalDate.now();
        
        switch (channel) {
            case "RNLIC":
                Double rnlicSuccessRate = trxRepository.getTodaySuccessRate(today);
                channelHealth.put("status", rnlicSuccessRate > 95 ? "HEALTHY" : 
                                           rnlicSuccessRate > 85 ? "WARNING" : "CRITICAL");
                channelHealth.put("successRate", rnlicSuccessRate);
                break;
                
            case "Suvidha":
                Double suvidhaSuccessRate = suvidhaRepository.getTodaySuccessRate(today);
                channelHealth.put("status", suvidhaSuccessRate > 95 ? "HEALTHY" : 
                                           suvidhaSuccessRate > 85 ? "WARNING" : "CRITICAL");
                channelHealth.put("successRate", suvidhaSuccessRate);
                break;
                
            case "UPI":
                Double upiSuccessRate = upiRepository.getTodaySuccessRate(today);
                channelHealth.put("status", upiSuccessRate > 95 ? "HEALTHY" : 
                                           upiSuccessRate > 85 ? "WARNING" : "CRITICAL");
                channelHealth.put("successRate", upiSuccessRate);
                break;
        }
        
        return channelHealth;
    }

    private String calculateOverallHealth() {
        LocalDate today = LocalDate.now();
        
        Double rnlicRate = trxRepository.getTodaySuccessRate(today);
        Double suvidhaRate = suvidhaRepository.getTodaySuccessRate(today);
        Double upiRate = upiRepository.getTodaySuccessRate(today);
        
        Double overallRate = (rnlicRate + suvidhaRate + upiRate) / 3;
        
        return overallRate > 95 ? "HEALTHY" : 
               overallRate > 85 ? "WARNING" : "CRITICAL";
    }
}