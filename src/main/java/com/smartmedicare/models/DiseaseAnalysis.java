package com.smartmedicare.models;

public class DiseaseAnalysis {
    private String condition;
    private double probability;
    private String severity;
    
    public DiseaseAnalysis(String condition, double probability, String severity) {
        this.condition = condition;
        this.probability = probability;
        this.severity = severity;
    }
    
    public String getCondition() {
        return condition;
    }
    
    public void setCondition(String condition) {
        this.condition = condition;
    }
    
    public double getProbability() {
        return probability;
    }
    
    public void setProbability(double probability) {
        this.probability = probability;
    }
    
    public String getSeverity() {
        return severity;
    }
    
    public void setSeverity(String severity) {
        this.severity = severity;
    }
}