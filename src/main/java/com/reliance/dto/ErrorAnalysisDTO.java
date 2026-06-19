package com.reliance.dto;

import java.util.List;
import java.util.Map;

public class ErrorAnalysisDTO {
    private List<Map<String, Object>> rnlicErrors;
    private List<Map<String, Object>> suvidhaErrors;
    private List<Map<String, Object>> upiErrors;

    // Getters and setters
    public List<Map<String, Object>> getRnlicErrors() { return rnlicErrors; }
    public void setRnlicErrors(List<Map<String, Object>> rnlicErrors) { this.rnlicErrors = rnlicErrors; }
    
    public List<Map<String, Object>> getSuvidhaErrors() { return suvidhaErrors; }
    public void setSuvidhaErrors(List<Map<String, Object>> suvidhaErrors) { this.suvidhaErrors = suvidhaErrors; }
    
    public List<Map<String, Object>> getUpiErrors() { return upiErrors; }
    public void setUpiErrors(List<Map<String, Object>> upiErrors) { this.upiErrors = upiErrors; }
}