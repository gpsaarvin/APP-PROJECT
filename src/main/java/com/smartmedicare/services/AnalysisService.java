package com.smartmedicare.services;

import com.smartmedicare.models.DiseaseAnalysis;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class AnalysisService {
    private final MongoDatabase database;
    private static final double THRESHOLD = 0.2; // 20% minimum probability threshold
    
    public AnalysisService() {
        this.database = DatabaseService.getInstance().getDatabase();
    }
    
    public List<DiseaseAnalysis> analyzeSymptoms(List<String> symptoms) {
        List<DiseaseAnalysis> results = new ArrayList<>();
        Map<String, Integer> diseaseMatches = new HashMap<>();
        Map<String, String> diseaseSeverity = new HashMap<>();
        
        MongoCollection<Document> symptomsCollection = database.getCollection("disease_symptoms");
        try (MongoCursor<Document> cursor = symptomsCollection.find().iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                String disease = doc.getString("disease");
                List<String> diseaseSymptoms = doc.getList("symptoms", String.class);
                String severity = doc.getString("severity");
                
                int matches = 0;
                for (String symptom : symptoms) {
                    if (diseaseSymptoms.contains(symptom.toLowerCase())) {
                        matches++;
                    }
                }
                
                if (matches > 0) {
                    double probability = (double) matches / diseaseSymptoms.size();
                    if (probability >= THRESHOLD) {
                        diseaseMatches.put(disease, matches);
                        diseaseSeverity.put(disease, severity);
                    }
                }
            }
        }
        
        // Convert matches to probabilities
        int totalMatches = diseaseMatches.values().stream().mapToInt(Integer::intValue).sum();
        
        diseaseMatches.forEach((disease, matches) -> {
            double probability = (double) matches / totalMatches;
            results.add(new DiseaseAnalysis(
                disease,
                probability,
                diseaseSeverity.get(disease)
            ));
        });
        
        // Sort by probability in descending order
        results.sort((a, b) -> Double.compare(b.getProbability(), a.getProbability()));
        
        return results;
    }
}