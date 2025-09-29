package com.smartmedicare.services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import com.smartmedicare.models.DiseaseResult;
import com.smartmedicare.models.Symptom;

public class AIDiseaseDetectionService {
    private static final Map<String, Double> DISEASE_WEIGHTS = new HashMap<>();
    private static final Map<String, List<String>> DISEASE_SYMPTOMS = new HashMap<>();
    
    static {
        // Initialize disease weights and symptom mappings
        initializeDiseaseData();
    }

    private static void initializeDiseaseData() {
        // Common diseases and their associated symptoms with weights
        DISEASE_WEIGHTS.put("Common Cold", 0.3);
        DISEASE_WEIGHTS.put("Influenza", 0.4);
        DISEASE_WEIGHTS.put("COVID-19", 0.5);
        DISEASE_WEIGHTS.put("Pneumonia", 0.6);
        DISEASE_WEIGHTS.put("Bronchitis", 0.5);
        DISEASE_WEIGHTS.put("Asthma", 0.4);
        DISEASE_WEIGHTS.put("Diabetes", 0.7);
        DISEASE_WEIGHTS.put("Hypertension", 0.6);
        DISEASE_WEIGHTS.put("Migraine", 0.4);
        DISEASE_WEIGHTS.put("Gastritis", 0.5);

        // Map symptoms to diseases
        DISEASE_SYMPTOMS.put("Common Cold", Arrays.asList(
            "Runny nose", "Sore throat", "Cough", "Sneezing", "Fatigue"
        ));
        DISEASE_SYMPTOMS.put("Influenza", Arrays.asList(
            "High fever", "Body aches", "Fatigue", "Cough", "Headache"
        ));
        DISEASE_SYMPTOMS.put("COVID-19", Arrays.asList(
            "Fever", "Dry cough", "Fatigue", "Loss of taste/smell", "Shortness of breath"
        ));
        DISEASE_SYMPTOMS.put("Pneumonia", Arrays.asList(
            "High fever", "Severe cough", "Shortness of breath", "Chest pain", "Fatigue"
        ));
        DISEASE_SYMPTOMS.put("Bronchitis", Arrays.asList(
            "Persistent cough", "Wheezing", "Chest discomfort", "Fatigue", "Mucus production"
        ));
        DISEASE_SYMPTOMS.put("Asthma", Arrays.asList(
            "Wheezing", "Shortness of breath", "Chest tightness", "Coughing", "Difficulty breathing"
        ));
        DISEASE_SYMPTOMS.put("Diabetes", Arrays.asList(
            "Frequent urination", "Excessive thirst", "Unexplained weight loss", "Fatigue", "Blurred vision"
        ));
        DISEASE_SYMPTOMS.put("Hypertension", Arrays.asList(
            "Headache", "Shortness of breath", "Chest pain", "Dizziness", "Vision problems"
        ));
        DISEASE_SYMPTOMS.put("Migraine", Arrays.asList(
            "Severe headache", "Nausea", "Light sensitivity", "Sound sensitivity", "Visual disturbances"
        ));
        DISEASE_SYMPTOMS.put("Gastritis", Arrays.asList(
            "Abdominal pain", "Nausea", "Bloating", "Loss of appetite", "Indigestion"
        ));
    }

    public List<DiseaseResult> analyzeSymptoms(List<Symptom> symptoms) {
        Map<String, Double> diseaseProbabilities = new HashMap<>();

        // Convert symptoms to string list
        List<String> symptomNames = symptoms.stream()
            .map(Symptom::getName)
            .collect(Collectors.toList());

        // Calculate probability for each disease
        for (Map.Entry<String, List<String>> entry : DISEASE_SYMPTOMS.entrySet()) {
            String disease = entry.getKey();
            List<String> diseaseSymptoms = entry.getValue();
            
            // Count matching symptoms
            long matchingSymptoms = symptomNames.stream()
                .filter(diseaseSymptoms::contains)
                .count();
            
            // Calculate probability based on matching symptoms and disease weight
            if (matchingSymptoms > 0) {
                double probability = calculateProbability(
                    matchingSymptoms, 
                    diseaseSymptoms.size(), 
                    DISEASE_WEIGHTS.get(disease)
                );
                diseaseProbabilities.put(disease, probability);
            }
        }

        // Convert to sorted list of results
        return diseaseProbabilities.entrySet().stream()
            .map(e -> new DiseaseResult(e.getKey(), e.getValue()))
            .sorted((r1, r2) -> Double.compare(r2.getProbability(), r1.getProbability()))
            .collect(Collectors.toList());
    }

    private double calculateProbability(long matchingSymptoms, int totalSymptoms, double diseaseWeight) {
        // Base probability from matching symptoms ratio
        double baseProbability = (double) matchingSymptoms / totalSymptoms;
        
        // Apply disease weight and normalize
        double weightedProbability = baseProbability * diseaseWeight;
        
        // Add some randomness to simulate AI uncertainty
        Random random = new Random();
        double uncertainty = (random.nextDouble() * 0.1) - 0.05; // ±5% uncertainty
        
        return Math.min(1.0, Math.max(0.0, weightedProbability + uncertainty));
    }

    public String getAIRecommendation(List<DiseaseResult> results, List<Symptom> symptoms) {
        if (results.isEmpty()) {
            return "No significant health concerns detected based on the provided symptoms.";
        }

        DiseaseResult topResult = results.get(0);
        StringBuilder recommendation = new StringBuilder();

        if (topResult.getProbability() > 0.7) {
            recommendation.append(String.format(
                "High probability (%.1f%%) of %s detected. Immediate medical consultation is recommended.",
                topResult.getProbability() * 100,
                topResult.getDisease()
            ));
        } else if (topResult.getProbability() > 0.4) {
            recommendation.append(String.format(
                "Moderate probability (%.1f%%) of %s detected. Consider scheduling a medical check-up.",
                topResult.getProbability() * 100,
                topResult.getDisease()
            ));
        } else {
            recommendation.append(String.format(
                "Low probability (%.1f%%) of %s detected. Monitor symptoms and seek medical advice if they worsen.",
                topResult.getProbability() * 100,
                topResult.getDisease()
            ));
        }

        // Add general advice
        recommendation.append("\n\nGeneral recommendations:\n");
        recommendation.append("- Rest and maintain good hydration\n");
        recommendation.append("- Monitor symptoms for any changes\n");
        recommendation.append("- Follow proper hygiene practices\n");
        
        if (topResult.getProbability() > 0.3) {
            recommendation.append("- Consider telehealth consultation\n");
        }

        return recommendation.toString();
    }
}