package com.reliance.contoller;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.reliance.dto.PaymentAnalyticsDTO;
import com.reliance.services.EnhancedPaymentAnalyticsService;
import com.reliance.services.PaymentAnalyticsService;

import org.springframework.ui.Model;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

@Controller
@RequestMapping("/analytics")
public class PaymentAnalyticsController {

    @Autowired
    @Qualifier("paymentAnalyticsService")
    private PaymentAnalyticsService analyticsService;
    
    @Autowired
    private EnhancedPaymentAnalyticsService service;

    @GetMapping("/dashboard")
    public String dashboard(Model model,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        // Set default dates if not provided
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();
        
        PaymentAnalyticsDTO analytics = analyticsService.getPaymentAnalytics(startDate, endDate);
        
        model.addAttribute("analytics", analytics);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        
        return "dashboard";
    }

    @GetMapping("/api/payment-trends")
    @ResponseBody
    public Map<String, Object> getPaymentTrends(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return analyticsService.getPaymentTrends(startDate, endDate);
    }

    @GetMapping("/api/success-rate-by-gateway")
    @ResponseBody
    public Map<String, Object> getSuccessRateByGateway(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return analyticsService.getSuccessRateByGateway(startDate, endDate);
    }
    
    @GetMapping("/error-analysis")
    public ResponseEntity<Map<String, Object>> getErrorAnalysis(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        return ResponseEntity.ok(service.getDetailedErrorAnalysis(startDate, endDate));
    }

    @GetMapping("/performance-metrics")
    public ResponseEntity<Map<String, Object>> getPerformanceMetrics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        return ResponseEntity.ok(service.getPerformanceMetrics(startDate, endDate));
    }

    @GetMapping("/realtime")
    public ResponseEntity<Map<String, Object>> getRealtimeMetrics() {
        return ResponseEntity.ok(service.getRealtimeMetrics());
    }
    
    
    @GetMapping("/export")
    public ResponseEntity<String> exportData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        String csvContent = service.exportToCSV(startDate, endDate);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=payment_report.csv")
                .body(csvContent);
    }
    
    @GetMapping("/OnlinePaymentTransactionData")
    public ResponseEntity<String> exportTrxTransactionData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam String status,
            @RequestParam String paymentMode) {

        String csvContent = service.exportOnlinePaymentTransaction(startDate, endDate, status, paymentMode);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=OnlinePaymentTransactionData.csv")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                .body(csvContent);
    }
    
    @GetMapping("/ThridPartyTransactionData")   
    public ResponseEntity<String> exportSuvidhaTransactions(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        String csv = service.exportThirdPartyTransactionsToCsv(startDate, endDate);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=SuvidhaTransactions.csv")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                .body(csv);
    }
    
    @GetMapping("/UPICollectionTransactionData")
    public ResponseEntity<String> exportUPICollectionData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
    	String csv = service.exportUPICollectionDataToCsv(startDate, endDate);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=UPICollections.csv")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                .body(csv);
    }
 
    
    @GetMapping("/summary")
    public ResponseEntity<String> summary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(service.exportToCSV(startDate,endDate));
    } 
}