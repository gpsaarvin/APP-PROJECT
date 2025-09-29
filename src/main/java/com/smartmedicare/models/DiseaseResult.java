package com.smartmedicare.models;

public class DiseaseResult {
    private String disease;
    private double probability;
    private String recommendation;

    public DiseaseResult(String disease, double probability) {
        this.disease = disease;
        this.probability = probability;
    }

    public String getDisease() { return disease; }
    public void setDisease(String disease) { this.disease = disease; }

    public double getProbability() { return probability; }
    public void setProbability(double probability) { this.probability = probability; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
}