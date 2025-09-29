package com.smartmedicare;

import java.io.IOException;

import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import com.smartmedicare.services.DatabaseService;
import com.smartmedicare.utils.DialogUtils;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private DatabaseService dbService;
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize database connection
            initializeDatabase();

            // Load and show main window
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/landing.fxml"));
            primaryStage.setTitle("Smart Medicare");
            // Use a generous default size for the landing layout and set a sensible minimum
            Scene scene = new Scene(root, 1200, 800);
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1024);
            primaryStage.setMinHeight(700);
            primaryStage.centerOnScreen();
            primaryStage.show();

        } catch (IOException e) {
            DialogUtils.showError("Resource Error", "Failed to load application resources", 
                "Error: " + e.getMessage());
            Platform.exit();
        } catch (RuntimeException e) {
            DialogUtils.showError("Startup Error", "Failed to initialize application", 
                "Error: " + e.getMessage());
            Platform.exit();
        }
    }

    private void initializeDatabase() {
        try {
            dbService = DatabaseService.getInstance();
            MongoDatabase database = dbService.getDatabase();
            
            // Test connection by running a simple command with timeout
            database.runCommand(new org.bson.Document("ping", 1)
                .append("maxTimeMS", 5000));  // 5 second timeout
            System.out.println("Successfully connected to MongoDB");

        } catch (MongoException e) {
            String errorMessage = "Failed to connect to database. Please ensure MongoDB is running on localhost:27017";
            System.err.println(errorMessage + "\nError details: " + e.getMessage());
            
            // Show error dialog but don't throw exception - allow app to start without DB
            DialogUtils.showError(
                "Database Connection Error",
                "Could not connect to MongoDB",
                errorMessage + "\n\nThe application will start in offline mode."
            );
        } catch (Exception e) {
            String errorMessage = "Unexpected error while initializing database";
            System.err.println(errorMessage + "\nError details: " + e.getMessage());
            
            DialogUtils.showError(
                "Database Error",
                "Database initialization failed",
                errorMessage + "\n\nThe application will start in offline mode."
            );
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}