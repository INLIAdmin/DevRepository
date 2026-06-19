package com.reliance.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.reliance.dto.SuvidhaTransactionDTO;

import java.time.LocalDate;
import java.util.*;

@Repository
public class SuvidhaTxnRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate1;

    public Map<String, Object> getOverallStats(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT 
                COUNT(*) as totalCount,
                COUNT(CASE WHEN SUVIDHASTATUS = 'TRUE' THEN 1 END) as successCount,
                SUM(PREMIUMDUE) as totalAmount,
                SUM(CASE WHEN SUVIDHASTATUS = 'TRUE' THEN PREMIUMDUE ELSE 0 END) as successAmount
            FROM suvidha_txn 
            WHERE TRUNC(CREATEDATE) BETWEEN ? AND ?
            """;

        return jdbcTemplate.queryForMap(sql, startDate, endDate);
    }

    public Map<String, Object> getChannelStats(LocalDate startDate, LocalDate endDate) {
        return getOverallStats(startDate, endDate);
    }

    public List<Map<String, Object>> getErrorStats(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT 
                COALESCE(ERRORCODE, 0) as errorCode,
                COALESCE(ERRORMESSAGE, 'No message') as errorMessage,
                COUNT(*) as errorCount
            FROM suvidha_txn 
            WHERE TRUNC(CREATEDATE) BETWEEN ? AND ?
            AND SUVIDHASTATUS != 'SUCCESS'
            AND ERRORCODE IS NOT NULL
            GROUP BY ERRORCODE, ERRORMESSAGE
            ORDER BY errorCount DESC
            FETCH FIRST 10 ROWS ONLY
            """;

        return jdbcTemplate.queryForList(sql, startDate, endDate);
    }

    public List<Map<String, Object>> getHourlyTrends(LocalDate date) {
        String sql = """
            SELECT 
                EXTRACT(HOUR FROM CREATEDATE) as hour,
                COUNT(*) as totalTransactions,
                SUM(CASE WHEN SUVIDHASTATUS = 'TRUE' THEN 1 ELSE 0 END) as successfulTransactions,
                SUM(CASE WHEN SUVIDHASTATUS = 'FALSE' THEN 1 ELSE 0 END) as failedTransactions,
                SUM(PREMIUMDUE) as totalAmount,
                SUM(CASE WHEN SUVIDHASTATUS = 'TRUE' THEN PREMIUMDUE ELSE 0 END) as successfulAmount
            FROM suvidha_txn 
            WHERE TRUNC(CREATEDATE) = ?
            GROUP BY EXTRACT(HOUR FROM CREATEDATE)
            ORDER BY hour
        """;

        return jdbcTemplate.queryForList(sql, date);
    }

    public List<Map<String, Object>> getDailyTrends(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT 
                TRUNC(CREATEDATE) as transactionDate,
                COUNT(*) as totalTransactions,
                SUM(CASE WHEN SUVIDHASTATUS = 'TRUE' THEN 1 ELSE 0 END) as successfulTransactions,
                SUM(PREMIUMDUE) as totalAmount,
                SUM(CASE WHEN SUVIDHASTATUS = 'TRUE' THEN PREMIUMDUE ELSE 0 END) as successfulAmount
            FROM suvidha_txn 
            WHERE TRUNC(CREATEDATE) BETWEEN ? AND ?
            GROUP BY TRUNC(CREATEDATE)
            ORDER BY transactionDate
        """;

        return jdbcTemplate.queryForList(sql, startDate, endDate);
    }

    public List<Map<String, Object>> getErrorTrends(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT 
                TRUNC(CREATEDATE) as errorDate,
                ERRORCODE as error_code,
                COUNT(*) as errorCount,
                ERRORMESSAGE as error_description
            FROM suvidha_txn 
            WHERE TRUNC(CREATEDATE) BETWEEN ? AND ?
            AND SUVIDHASTATUS = 'FALSE'
            AND ERRORCODE IS NOT NULL
            GROUP BY TRUNC(CREATEDATE), ERRORCODE, ERRORMESSAGE
            ORDER BY errorDate, errorCount DESC
        """;

        return jdbcTemplate.queryForList(sql, startDate, endDate);
    }

    public Long getErrorCount(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT COUNT(*) 
            FROM suvidha_txn 
            WHERE TRUNC(CREATEDATE) BETWEEN ? AND ?
            AND SUVIDHASTATUS = 'FALSE'
        """;

        return jdbcTemplate.queryForObject(sql, Long.class, startDate, endDate);
    }

    public List<Map<String, Object>> getTopErrors(LocalDate startDate, LocalDate endDate, int limit) {
        String sql = """
            SELECT 
                ERRORCODE as error_code,
                ERRORMESSAGE as error_description,
                COUNT(*) as errorCount,
                'Suvidha' as channel
            FROM suvidha_txn 
            WHERE TRUNC(CREATEDATE) BETWEEN ? AND ?
            AND SUVIDHASTATUS = 'FALSE'
            AND ERRORCODE IS NOT NULL
            GROUP BY ERRORCODE, ERRORMESSAGE
            ORDER BY errorCount DESC
            FETCH FIRST ? ROWS ONLY
        """;

        return jdbcTemplate.queryForList(sql, startDate, endDate, limit);
    }

    public Double getAverageResponseTime(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT AVG(
                CASE 
                    WHEN CREATEDATE IS NOT NULL AND MODIFYDATE IS NOT NULL 
                    THEN (CAST(MODIFYDATE AS DATE) - CAST(CREATEDATE AS DATE)) * 86400000
                    ELSE NULL 
                END
            ) as avg_response_time_ms
            FROM suvidha_txn 
            WHERE TRUNC(CREATEDATE) BETWEEN ? AND ?
            AND CREATEDATE IS NOT NULL 
            AND MODIFYDATE IS NOT NULL
            AND MODIFYDATE > CREATEDATE
        """;

        try {
            Double result = jdbcTemplate.queryForObject(sql, Double.class, startDate, endDate);
            return result != null ? result : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    public List<SuvidhaTransactionDTO> fetchTransactions(Date startDate, Date endDate) {
        String sql = 
            "SELECT " +
            "    st.contactnumber AS policyNumber, " +
            "    se.policyholdername AS customerName, " +
            "    se.premiumdue AS premiumAmount, " +
            "    se.policyname AS policyName, " +
            "    st.consumer AS Consumer, " +
            "    st.errorcode AS transactionStatus, " +
            "    st.createdate AS transactionDate " +
            "FROM suvidha_txn st " +
            "JOIN suvidha_enquiry se ON st.vendortxnid = se.vendortxnid " +
            "WHERE TRUNC(st.createdate) BETWEEN :startDate AND :endDate";

        Map<String, Object> params = new HashMap<>();
        params.put("startDate", startDate);
        params.put("endDate", endDate);

        return jdbcTemplate1.query(sql, params, new BeanPropertyRowMapper<>(SuvidhaTransactionDTO.class));
    }

    public List<Map<String, Object>> getHourlyAggregateData(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT 
                EXTRACT(HOUR FROM CREATEDATE) as hour,
                COUNT(*) as transactionCount
            FROM suvidha_txn 
            WHERE TRUNC(CREATEDATE) BETWEEN ? AND ?
            GROUP BY EXTRACT(HOUR FROM CREATEDATE)
            ORDER BY hour
        """;

        return jdbcTemplate.queryForList(sql, startDate, endDate);
    }

    public Map<String, Object> getChannelAnalytics(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT 
                COUNT(*) as totalTransactions,
                SUM(CASE WHEN SUVIDHASTATUS = 'TRUE' THEN 1 ELSE 0 END) as successfulTransactions,
                SUM(CASE WHEN SUVIDHASTATUS = 'FALSE' THEN 1 ELSE 0 END) as failedTransactions,
                SUM(PREMIUMDUE) as totalAmount,
                SUM(CASE WHEN SUVIDHASTATUS = 'TRUE' THEN PREMIUMDUE ELSE 0 END) as successfulAmount,
                AVG(
                    CASE 
                        WHEN CREATEDATE IS NOT NULL AND MODIFYDATE IS NOT NULL 
                        THEN (CAST(MODIFYDATE AS DATE) - CAST(CREATEDATE AS DATE)) * 86400000
                        ELSE NULL 
                    END
                ) as avgResponseTime,
                MAX(
                    CASE 
                        WHEN CREATEDATE IS NOT NULL AND MODIFYDATE IS NOT NULL 
                        THEN (CAST(MODIFYDATE AS DATE) - CAST(CREATEDATE AS DATE)) * 86400000
                        ELSE NULL 
                    END
                ) as maxResponseTime,
                MIN(
                    CASE 
                        WHEN CREATEDATE IS NOT NULL AND MODIFYDATE IS NOT NULL 
                        THEN (CAST(MODIFYDATE AS DATE) - CAST(CREATEDATE AS DATE)) * 86400000
                        ELSE NULL 
                    END
                ) as minResponseTime
            FROM suvidha_txn 
            WHERE TRUNC(CREATEDATE) BETWEEN ? AND ?
        """;

        return jdbcTemplate.queryForMap(sql, startDate, endDate);
    }

    public Long getTotalVolume(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT COUNT(*) 
            FROM suvidha_txn 
            WHERE TRUNC(CREATEDATE) BETWEEN ? AND ?
        """;

        return jdbcTemplate.queryForObject(sql, Long.class, startDate, endDate);
    }

    public Map<String, Object> getCurrentHourMetrics() {
        String sql = """
            SELECT 
                COUNT(*) as totalTransactions,
                SUM(CASE WHEN SUVIDHASTATUS = 'TRUE' THEN 1 ELSE 0 END) as successfulTransactions,
                SUM(CASE WHEN SUVIDHASTATUS = 'FALSE' THEN 1 ELSE 0 END) as failedTransactions,
                SUM(PREMIUMDUE) as totalAmount
            FROM suvidha_txn 
            WHERE CREATEDATE >= TRUNC(SYSDATE, 'HH24')
        """;

        return jdbcTemplate.queryForMap(sql);
    }

    public Long getLiveTransactionCount() {
        String sql = """
            SELECT COUNT(*) 
            FROM suvidha_txn 
            WHERE CREATEDATE >= SYSDATE - (5/1440)
        """;

        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    public Double getTodaySuccessRate(LocalDate date) {
        String sql = """
            SELECT 
                CASE 
                    WHEN COUNT(*) = 0 THEN 0
                    ELSE (SUM(CASE WHEN SUVIDHASTATUS = 'TRUE' THEN 1 ELSE 0 END) * 100.0 / COUNT(*))
                END as successRate
            FROM suvidha_txn 
            WHERE TRUNC(CREATEDATE) = ?
        """;

        return jdbcTemplate.queryForObject(sql, Double.class, date);
    }
}
