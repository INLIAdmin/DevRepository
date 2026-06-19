package com.reliance.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import com.reliance.dto.PaymentModeStatsDTO;
import com.reliance.dto.UpiTransactionDTO;

import java.time.LocalDate;
import java.util.*;

@Repository
public class UpiCollectionsRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate1;

    public Map<String, Object> getOverallStats(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT 
                COUNT(*) as totalCount,
                COUNT(CASE WHEN PAYMENT_STATUS = 'success' THEN 1 END) as successCount,
                SUM(AMOUNT) as totalAmount,
                SUM(CASE WHEN PAYMENT_STATUS = 'success' THEN AMOUNT ELSE 0 END) as successAmount
            FROM WHATSAPP_BOT_TRANSACTIONS 
            WHERE TRUNC(CREATED_DATE) BETWEEN ? AND ?
        """;
        return jdbcTemplate.queryForMap(sql, startDate, endDate);
    }

    public Map<String, Object> getChannelStats(LocalDate startDate, LocalDate endDate) {
        return getOverallStats(startDate, endDate);
    }

    public List<PaymentModeStatsDTO> getPaymentModeStats(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT 
                COALESCE(PAYMENT_METHOD, 'UPI') as paymentMode,
                COUNT(*) as totalTransactions,
                COUNT(CASE WHEN PAYMENT_STATUS = 'success' THEN 1 END) as successfulTransactions,
                SUM(AMOUNT) as totalAmount,
                SUM(CASE WHEN PAYMENT_STATUS = 'success' THEN AMOUNT ELSE 0 END) as successfulAmount
            FROM WHATSAPP_BOT_TRANSACTIONS 
            WHERE TRUNC(CREATED_DATE) BETWEEN ? AND ?
            GROUP BY COALESCE(PAYMENT_METHOD, 'UPI')
            ORDER BY totalTransactions DESC
        """;

        return jdbcTemplate.query(sql, new Object[]{startDate, endDate}, (rs, rowNum) -> {
            PaymentModeStatsDTO stats = new PaymentModeStatsDTO();
            stats.setPaymentMode(rs.getString("paymentMode"));
            stats.setTotalTransactions(rs.getLong("totalTransactions"));
            stats.setSuccessfulTransactions(rs.getLong("successfulTransactions"));
            stats.setTotalAmount(rs.getDouble("totalAmount"));
            stats.setSuccessfulAmount(rs.getDouble("successfulAmount"));
            stats.setSuccessRate(stats.getTotalTransactions() > 0 ?
                    (double) stats.getSuccessfulTransactions() / stats.getTotalTransactions() * 100 : 0);
            return stats;
        });
    }

    public List<Map<String, Object>> getErrorStats(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT 
                'UPI_ERR_001' as errorCode,
                COALESCE(ERROR_DETAILS, 'No details') as errorMessage,
                COUNT(*) as errorCount
            FROM WHATSAPP_BOT_TRANSACTIONS 
            WHERE TRUNC(CREATED_DATE) BETWEEN ? AND ?
            AND PAYMENT_STATUS != 'SUCCESS'
            AND ERROR_DETAILS IS NOT NULL
            GROUP BY ERROR_DETAILS
            ORDER BY errorCount DESC
            FETCH FIRST 10 ROWS ONLY
        """;

        return jdbcTemplate.queryForList(sql, startDate, endDate);
    }

    public Map<String, Object> getSuccessRateByGateway(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT 
                COALESCE(UPI_GATEWAY, 'Unknown') as gatewayCode,
                COUNT(*) as totalTransactions,
                COUNT(CASE WHEN PAYMENT_STATUS = 'success' THEN 1 END) as successfulTransactions,
                ROUND((COUNT(CASE WHEN PAYMENT_STATUS = 'SUCCESS' THEN 1 END) * 100.0 / COUNT(*)), 2) as successRate
            FROM WHATSAPP_BOT_TRANSACTIONS 
            WHERE TRUNC(CREATED_DATE) BETWEEN ? AND ?
            GROUP BY UPI_GATEWAY
            ORDER BY successRate DESC
        """;

        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, startDate, endDate);
        Map<String, Object> response = new HashMap<>();
        response.put("gatewayStats", results);
        return response;
    }

    public List<Map<String, Object>> getHourlyTrends(LocalDate date) {
        String sql = """
            SELECT 
                EXTRACT(HOUR FROM created_date) as hour,
                COUNT(*) as totalTransactions,
                SUM(CASE WHEN PAYMENT_STATUS = 'success' THEN 1 ELSE 0 END) as successfulTransactions,
                SUM(CASE WHEN PAYMENT_STATUS = 'FAILED' THEN 1 ELSE 0 END) as failedTransactions,
                SUM(AMOUNT) as totalAmount,
                SUM(CASE WHEN PAYMENT_STATUS = 'success' THEN AMOUNT ELSE 0 END) as successfulAmount
            FROM WHATSAPP_BOT_TRANSACTIONS 
            WHERE TRUNC(created_date) = ?
            GROUP BY EXTRACT(HOUR FROM created_date)
            ORDER BY hour
        """;

        return jdbcTemplate.queryForList(sql, date);
    }
    
    public List<UpiTransactionDTO> fetchTransactions(Date startDate, Date endDate) {
        String sql =
            "SELECT " +
            " contractnumber AS policyNumber, " +
            " amount, " +
            " upi_id AS customerUPIID, " +
            " txn_key AS transactionKey, " +
            " payment_status AS transactionStatus, " +
            " bankcode AS bankCode, " +
            " Source, " +
            " created_date AS transactionDate " +
            "FROM WHATSAPP_BOT_TRANSACTIONS " +
            "WHERE TRUNC(created_date) BETWEEN :startDate AND :endDate";

        Map<String, Object> params = new HashMap<>();
        params.put("startDate", startDate);
        params.put("endDate", endDate);

        return jdbcTemplate1.query(sql, params, new BeanPropertyRowMapper<>(UpiTransactionDTO.class));
    }

    public List<Map<String, Object>> getDailyTrends(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT 
                TRUNC(created_date) as transactionDate,
                COUNT(*) as totalTransactions,
                SUM(CASE WHEN PAYMENT_STATUS = 'success' THEN 1 ELSE 0 END) as successfulTransactions,
                SUM(AMOUNT) as totalAmount,
                SUM(CASE WHEN PAYMENT_STATUS = 'success' THEN AMOUNT ELSE 0 END) as successfulAmount
            FROM WHATSAPP_BOT_TRANSACTIONS 
            WHERE TRUNC(created_date) BETWEEN ? AND ?
            GROUP BY TRUNC(created_date)
            ORDER BY transactionDate
        """;

        return jdbcTemplate.queryForList(sql, startDate, endDate);
    }

    public List<Map<String, Object>> getErrorTrends(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT 
                TRUNC(created_date) as errorDate,
                'UPI_ERR_001' as error_code,
                COUNT(*) as errorCount,
                ERROR_DETAILS as error_message
            FROM WHATSAPP_BOT_TRANSACTIONS 
            WHERE TRUNC(created_date) BETWEEN ? AND ? 
            AND PAYMENT_STATUS = 'FAILED'
            AND ERROR_DETAILS IS NOT NULL
            GROUP BY TRUNC(created_date), ERROR_DETAILS
            ORDER BY errorDate, errorCount DESC
        """;

        return jdbcTemplate.queryForList(sql, startDate, endDate);
    }

    public Long getErrorCount(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT COUNT(*) 
            FROM WHATSAPP_BOT_TRANSACTIONS 
            WHERE TRUNC(created_date) BETWEEN ? AND ? 
            AND PAYMENT_STATUS = 'FAILED'
        """;

        return jdbcTemplate.queryForObject(sql, Long.class, startDate, endDate);
    }

    public List<Map<String, Object>> getTopErrors(LocalDate startDate, LocalDate endDate, int limit) {
        String sql = """
            SELECT 
                'UPI_ERR_001' as error_code,
                ERROR_DETAILS as error_message,
                COUNT(*) as errorCount,
                'UPI' as channel
            FROM WHATSAPP_BOT_TRANSACTIONS 
            WHERE TRUNC(created_date) BETWEEN ? AND ? 
            AND PAYMENT_STATUS = 'FAILED'
            AND ERROR_DETAILS IS NOT NULL
            GROUP BY ERROR_DETAILS
            ORDER BY errorCount DESC
            FETCH FIRST ? ROWS ONLY
        """;

        return jdbcTemplate.queryForList(sql, startDate, endDate, limit);
    }

    public Double getAverageResponseTime(LocalDate startDate, LocalDate endDate) {
        return 0.0; // Not available for UPI
    }

    public List<Map<String, Object>> getHourlyAggregateData(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT 
                EXTRACT(HOUR FROM created_date) as hour,
                COUNT(*) as transactionCount
            FROM WHATSAPP_BOT_TRANSACTIONS 
            WHERE TRUNC(created_date) BETWEEN ? AND ?
            GROUP BY EXTRACT(HOUR FROM created_date)
            ORDER BY hour
        """;

        return jdbcTemplate.queryForList(sql, startDate, endDate);
    }

    public Map<String, Object> getChannelAnalytics(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT 
                COUNT(*) as totalTransactions,
                SUM(CASE WHEN PAYMENT_STATUS = 'success' THEN 1 ELSE 0 END) as successfulTransactions,
                SUM(CASE WHEN PAYMENT_STATUS = 'FAILED' THEN 1 ELSE 0 END) as failedTransactions,
                SUM(AMOUNT) as totalAmount,
                SUM(CASE WHEN PAYMENT_STATUS = 'success' THEN AMOUNT ELSE 0 END) as successfulAmount,
                NULL as avgResponseTime,
                NULL as maxResponseTime,
                NULL as minResponseTime
            FROM WHATSAPP_BOT_TRANSACTIONS 
            WHERE TRUNC(CREATED_DATE) BETWEEN ? AND ?
        """;

        return jdbcTemplate.queryForMap(sql, startDate, endDate);
    }

    public Long getTotalVolume(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT COUNT(*) 
            FROM WHATSAPP_BOT_TRANSACTIONS 
            WHERE TRUNC(CREATED_DATE) BETWEEN ? AND ?
        """;

        return jdbcTemplate.queryForObject(sql, Long.class, startDate, endDate);
    }

    public Map<String, Object> getCurrentHourMetrics() {
        String sql = """
            SELECT 
                COUNT(*) as totalTransactions,
                SUM(CASE WHEN PAYMENT_STATUS = 'success' THEN 1 ELSE 0 END) as successfulTransactions,
                SUM(CASE WHEN PAYMENT_STATUS = 'FAILED' THEN 1 ELSE 0 END) as failedTransactions,
                SUM(AMOUNT) as totalAmount
            FROM WHATSAPP_BOT_TRANSACTIONS 
            WHERE CREATED_DATE >= TRUNC(SYSDATE, 'HH24')
        """;

        return jdbcTemplate.queryForMap(sql);
    }

    public Long getLiveTransactionCount() {
        String sql = """
            SELECT COUNT(*) 
            FROM WHATSAPP_BOT_TRANSACTIONS 
            WHERE CREATED_DATE >= SYSDATE - (5/1440)
        """;

        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    public Double getTodaySuccessRate(LocalDate date) {
        String sql = """
            SELECT 
                CASE 
                    WHEN COUNT(*) = 0 THEN 0
                    ELSE (SUM(CASE WHEN PAYMENT_STATUS = 'success' THEN 1 ELSE 0 END) * 100.0 / COUNT(*))
                END as successRate
            FROM WHATSAPP_BOT_TRANSACTIONS 
            WHERE TRUNC(CREATED_DATE) = ?
        """;

        return jdbcTemplate.queryForObject(sql, Double.class, date);
    }
}
