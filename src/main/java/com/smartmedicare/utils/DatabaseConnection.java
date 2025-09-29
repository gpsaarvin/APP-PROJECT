package com.smartmedicare.utils;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private final MongoClient mongoClient;
    private final MongoDatabase database;

    private DatabaseConnection() {
        // Set up the codec registry to automatically handle POJO (Plain Old Java Object) mapping
        CodecRegistry pojoCodecRegistry = fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(),
            fromProviders(PojoCodecProvider.builder().automatic(true).build())
        );

        // Configure MongoDB client settings
        MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString(DatabaseConfig.getConnectionString()))
            .codecRegistry(pojoCodecRegistry)
            .build();

        // Create MongoDB client and get database
        try {
            this.mongoClient = MongoClients.create(settings);
            this.database = mongoClient.getDatabase(DatabaseConfig.getDatabaseName());
            System.out.println("Successfully connected to MongoDB");
        } catch (Exception e) {
            System.err.println("Failed to connect to MongoDB: " + e.getMessage());
            throw e;
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public void close() {
        if (mongoClient != null) {
            try {
                mongoClient.close();
                System.out.println("MongoDB connection closed");
            } catch (Exception e) {
                System.err.println("Error closing MongoDB connection: " + e.getMessage());
            }
        }
    }
}