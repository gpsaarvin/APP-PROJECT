package com.smartmedicare.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class SceneNavigator {
    private static SceneNavigator instance;
    private Stage primaryStage;
    private Map<String, Scene> sceneCache;
    private List<Consumer<Scene>> sceneChangeListeners;

    private SceneNavigator() {
        sceneCache = new HashMap<>();
        sceneChangeListeners = new ArrayList<>();
    }

    public static SceneNavigator getInstance() {
        if (instance == null) {
            instance = new SceneNavigator();
        }
        return instance;
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    public void navigateTo(String fxmlPath) {
        try {
            Scene scene = sceneCache.get(fxmlPath);
            if (scene == null) {
                Parent root = FXMLLoader.load(getClass().getResource("/fxml/" + fxmlPath));
                scene = new Scene(root);
                sceneCache.put(fxmlPath, scene);
            }
            primaryStage.setScene(scene);
            notifySceneChangeListeners(scene);
        } catch (IOException e) {
            System.err.println("Error navigating to " + fxmlPath + ": " + e.getMessage());
            // TODO: Show error dialog
        }
    }

    public void onSceneChange(Consumer<Scene> listener) {
        sceneChangeListeners.add(listener);
    }

    public void removeSceneChangeListener(Consumer<Scene> listener) {
        sceneChangeListeners.remove(listener);
    }

    private void notifySceneChangeListeners(Scene scene) {
        sceneChangeListeners.forEach(listener -> listener.accept(scene));
    }

    public void clearCache() {
        sceneCache.clear();
    }

    public void removeFromCache(String fxmlPath) {
        sceneCache.remove(fxmlPath);
    }
}