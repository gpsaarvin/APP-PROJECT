package com.smartmedicare.services;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class DatabaseService {
    private static DatabaseService instance;
    private MongoClient mongoClient;
    private MongoDatabase database;

    private DatabaseService() {
        try {
            // Configure MongoDB client settings
            com.mongodb.ConnectionString connectionString = 
                new com.mongodb.ConnectionString("mongodb://localhost:27017");
            
            com.mongodb.MongoClientSettings settings = com.mongodb.MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .applyToSocketSettings(builder -> 
                    builder.connectTimeout(5000, java.util.concurrent.TimeUnit.MILLISECONDS))
                .applyToServerSettings(builder ->
                    builder.heartbeatFrequency(10000, java.util.concurrent.TimeUnit.MILLISECONDS))
                .retryWrites(true)
                .build();

            // Initialize MongoDB connection
            mongoClient = MongoClients.create(settings);
            database = mongoClient.getDatabase("smart_medicare");
        } catch (Exception e) {
            System.err.println("Error initializing database service: " + e.getMessage());
            // Don't throw exception - allow null database for offline mode
            mongoClient = null;
            database = null;
        }
    }

    public static DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}