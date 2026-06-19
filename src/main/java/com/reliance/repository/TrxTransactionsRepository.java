package com.reliance.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import com.reliance.dto.PaymentModeStatsDTO;
import com.reliance.dto.TransactionDTO;

import java.time.LocalDate;
import java.util.*;

@Repository
public class TrxTransactionsRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate1;

    public Map<String, Object> getOverallStats(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT 
                COUNT(*) as totalCount,
                COUNT(CASE WHEN TRXN_TGTW_PAID = '1' THEN 1 END) as successCount,
                SUM(TRXN_AMOUNT) as totalAmount,
                SUM(CASE WHEN TRXN_TGTW_PAID = '1' THEN TRXN_AMOUNT ELSE 0 END) as successAmount
            FROM trx_transactions 
            WHERE TRUNC(CREATED_DATE) BETWEEN ? AND ?
            AND DELETED = 0
            """;

        return jdbcTemplate.queryForMap(sql, startDate, endDate);
    }

    public Map<String, Object> getChannelStats(LocalDate startDate, LocalDate endDate) {
        return getOverallStats(startDate, endDate);
    }

    public List<PaymentModeStatsDTO> getPaymentModeStats(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT 
                COALESCE(TRXN_PAY_OPTION, 'Unknown') as paymentMode,
                COUNT(*) as totalTransactions,
                COUNT(CASE WHEN TRXN_TGTW_PAID = '1' THEN 1 END) as successfulTransactions,
                SUM(TRXN_AMOUNT) as totalAmount,
                SUM(CASE WHEN TRXN_TGTW_PAID = '1' THEN TRXN_AMOUNT ELSE 0 END) as successfulAmount
            FROM trx_transactions 
            WHERE TRUNC(CREATED_DATE) BETWEEN ? AND ?
            AND DELETED = 0
            GROUP BY TRXN_PAY_OPTION
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
                COALESCE(ERROR_CODE, 'Unknown') as errorCode,
                COALESCE(ERROR_MESSAGE, 'No message') as errorMessage,
                COUNT(*) as errorCount
            FROM trx_transactions 
            WHERE TRUNC(CREATED_DATE) BETWEEN ? AND ?
            AND DELETED = 0
            AND TRXN_STATUS != 'SUCCESS'
            AND ERROR_CODE IS NOT NULL
            GROUP BY ERROR_CODE, ERROR_MESSAGE
            ORDER BY errorCount DESC
            FETCH FIRST 10 ROWS ONLY
            """;

        return jdbcTemplate.queryForList(sql, startDate, endDate);
    }

    public Map<String, Object> getSuccessRateByGateway(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT 
                COALESCE(TRXN_TGTW_CODE, 'Unknown') as gatewayCode,
                COUNT(*) as totalTransactions,
                COUNT(CASE WHEN TRXN_TGTW_PAID = '1' THEN 1 END) as successfulTransactions,
                ROUND((COUNT(CASE WHEN TRXN_TGTW_PAID = '1' THEN 1 END) * 100.0 / COUNT(*)), 2) as successRate
            FROM trx_transactions 
            WHERE TRUNC(CREATED_DATE) BETWEEN ? AND ?
            AND DELETED = 0
            GROUP BY TRXN_TGTW_CODE
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
                SUM(CASE WHEN TRXN_TGTW_PAID = '1' THEN 1 ELSE 0 END) as successfulTransactions,
                SUM(CASE WHEN TRXN_TGTW_PAID = '2' THEN 1 ELSE 0 END) as failedTransactions,
                SUM(TRXN_AMOUNT) as totalAmount,
                SUM(CASE WHEN TRXN_TGTW_PAID = '1' THEN TRXN_AMOUNT ELSE 0 END) as successfulAmount
            FROM trx_transactions 
            WHERE TRUNC(created_date) = ?
            GROUP BY EXTRACT(HOUR FROM created_date)
            ORDER BY hour
        """;

        return jdbcTemplate.queryForList(sql, date);
    }

    public List<Map<String, Object>> getDailyTrends(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT 
                TRUNC(created_date) as transactionDate,
                COUNT(*) as totalTransactions,
                SUM(CASE WHEN TRXN_TGTW_PAID = '1' THEN 1 ELSE 0 END) as successfulTransactions,
                SUM(TRXN_AMOUNT) as totalAmount,
                SUM(CASE WHEN TRXN_TGTW_PAID = '1' THEN TRXN_AMOUNT ELSE 0 END) as successfulAmount
            FROM trx_transactions 
            WHERE TRUNC(created_date) BETWEEN ? AND ?
            GROUP BY TRUNC(created_date)
            ORDER BY transactionDate
        """;

        return jdbcTemplate.queryForList(sql, startDate, endDate);
    }

    public List<TransactionDTO> exportTransactions(Date startDate, Date endDate, String status, String paymentMode) {
            StringBuilder sql = new StringBuilder(
                "SELECT trxn_code AS PolicyNumber, trxn_customer_name AS CustomerName, trxn_amount AS Amount, " +
                "Mobile_Number AS MobileNumber, Email_ID AS EmailID, trxn_key AS TransactionKey, " +
                "trxn_code AS PaymentGateway, trxn_pay_option AS PaymentOption, trxn_tgtw_paid AS TransactionStatus, " +
                "name_on_card AS NameOnCard, relation_with_assured AS RelationWithAssured, " +
                "trxn_convenience_confirmation AS ConvenienceApplied, trxn_convenience_amt AS ConvenienceAmount, " +
                "total_amt_deducted AS AmountDeductedAfterConvenience, created_date AS TransactionDate " +
                "FROM trx_transactions WHERE TRUNC(created_date) BETWEEN :startDate AND :endDate"
            );

            Map<String, Object> params = new HashMap<>();
            params.put("startDate", startDate);
            params.put("endDate", endDate);

            if (!"all".equalsIgnoreCase(status)) {
                sql.append(" AND trxn_tgtw_paid = :status");
                params.put("status", switch (status.toLowerCase()) {
                    case "success" -> 1;
                    case "failed" -> 2;
                    case "awaiting" -> 0;
                    default -> throw new IllegalArgumentException("Invalid status: " + status);
                });
            }

            if (!"all".equalsIgnoreCase(paymentMode)) {
                sql.append(" AND trxn_pay_option = :paymentMode");
                params.put("paymentMode", paymentMode);
            }

            return jdbcTemplate1.query(sql.toString(), params, new BeanPropertyRowMapper<>(TransactionDTO.class));
        }
    

    
    public List<Map<String, Object>> getErrorTrends(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT 
                TRUNC(created_date) as errorDate,
                error_code,
                COUNT(*) as errorCount,
                error_message
            FROM trx_transactions 
            WHERE TRUNC(created_date) BETWEEN ? AND ?
            AND TRXN_TGTW_PAID = '2'
            AND error_code IS NOT NULL
            GROUP BY TRUNC(created_date), error_code, error_message
            ORDER BY errorDate, errorCount DESC
        """;

        return jdbcTemplate.queryForList(sql, startDate, endDate);
    }

    public Long getErrorCount(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT COUNT(*) 
            FROM trx_transactions 
            WHERE TRUNC(created_date) BETWEEN ? AND ?
            AND TRXN_TGTW_PAID = '2'
        """;

        return jdbcTemplate.queryForObject(sql, Long.class, startDate, endDate);
    }

    public List<Map<String, Object>> getTopErrors(LocalDate startDate, LocalDate endDate, int limit) {
        String sql = """
            SELECT 
                error_code,
                error_message,
                COUNT(*) as errorCount,
                'RNLIC' as channel
            FROM trx_transactions 
            WHERE TRUNC(created_date) BETWEEN ? AND ?
            AND TRXN_TGTW_PAID = '2'
            AND error_code IS NOT NULL
            GROUP BY error_code, error_message
            ORDER BY errorCount DESC
            FETCH FIRST ? ROWS ONLY
        """;

        return jdbcTemplate.queryForList(sql, startDate, endDate, limit);
    }

    public Double getAverageResponseTime(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT AVG(
                CASE 
                    WHEN created_date IS NOT NULL AND modified_date IS NOT NULL 
                    THEN (CAST(modified_date AS DATE) - CAST(created_date AS DATE)) * 86400000
                    ELSE NULL 
                END
            ) as avg_response_time_ms
            FROM trx_transactions 
            WHERE TRUNC(created_date) BETWEEN ? AND ?
            AND created_date IS NOT NULL 
            AND modified_date IS NOT NULL
            AND modified_date > created_date
            AND DELETED = 0
        """;

        try {
            Double result = jdbcTemplate.queryForObject(sql, Double.class, startDate, endDate);
            return result != null ? result : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    public List<Map<String, Object>> getHourlyAggregateData(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT 
                EXTRACT(HOUR FROM created_date) as hour,
                COUNT(*) as transactionCount
            FROM trx_transactions 
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
                SUM(CASE WHEN TRXN_TGTW_PAID = '1' THEN 1 ELSE 0 END) as successfulTransactions,
                SUM(CASE WHEN TRXN_TGTW_PAID = '2' THEN 1 ELSE 0 END) as failedTransactions,
                SUM(TRXN_AMOUNT) as totalAmount,
                SUM(CASE WHEN TRXN_TGTW_PAID = '1' THEN TRXN_AMOUNT ELSE 0 END) as successfulAmount,
                AVG(
                    CASE 
                        WHEN created_date IS NOT NULL AND modified_date IS NOT NULL 
                        THEN (CAST(modified_date AS DATE) - CAST(created_date AS DATE)) * 86400000
                        ELSE NULL 
                    END
                ) as avgResponseTime,
                MAX(
                    CASE 
                        WHEN created_date IS NOT NULL AND modified_date IS NOT NULL 
                        THEN (CAST(modified_date AS DATE) - CAST(created_date AS DATE)) * 86400000
                        ELSE NULL 
                    END
                ) as maxResponseTime,
                MIN(
                    CASE 
                        WHEN created_date IS NOT NULL AND modified_date IS NOT NULL 
                        THEN (CAST(modified_date AS DATE) - CAST(created_date AS DATE)) * 86400000
                        ELSE NULL 
                    END
                ) as minResponseTime
            FROM trx_transactions 
            WHERE TRUNC(created_date) BETWEEN ? AND ?
            AND created_date IS NOT NULL 
            AND modified_date IS NOT NULL
            AND modified_date > created_date
            AND DELETED = 0
        """;

        return jdbcTemplate.queryForMap(sql, startDate, endDate);
    }

    public Long getTotalVolume(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT COUNT(*) 
            FROM trx_transactions 
            WHERE TRUNC(created_date) BETWEEN ? AND ?
        """;

        return jdbcTemplate.queryForObject(sql, Long.class, startDate, endDate);
    }

    public Map<String, Object> getCurrentHourMetrics() {
        String sql = """
            SELECT 
                COUNT(*) as totalTransactions,
                SUM(CASE WHEN TRXN_TGTW_PAID = '1' THEN 1 ELSE 0 END) as successfulTransactions,
                SUM(CASE WHEN TRXN_TGTW_PAID = '2' THEN 1 ELSE 0 END) as failedTransactions,
                SUM(TRXN_AMOUNT) as totalAmount
            FROM trx_transactions 
            WHERE created_date >= TRUNC(SYSDATE, 'HH24')
        """;

        return jdbcTemplate.queryForMap(sql);
    }

    public Long getLiveTransactionCount() {
        String sql = """
            SELECT COUNT(*) 
            FROM trx_transactions 
            WHERE created_date >= SYSDATE - (5/1440)
        """;

        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    public Double getTodaySuccessRate(LocalDate date) {
        String sql = """
            SELECT 
                CASE 
                    WHEN COUNT(*) = 0 THEN 0
                    ELSE (SUM(CASE WHEN TRXN_TGTW_PAID = '1' THEN 1 ELSE 0 END) * 100.0 / COUNT(*))
                END as successRate
            FROM trx_transactions 
            WHERE TRUNC(created_date) = ?
        """;

        return jdbcTemplate.queryForObject(sql, Double.class, date);
    }
    
    
    
}
