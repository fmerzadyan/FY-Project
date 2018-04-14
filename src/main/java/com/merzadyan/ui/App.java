package com.merzadyan.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Demonstrates the bare minimum code required for the JavaFX view, FXMLLoader, etc to work
 * as in displaying view correctly.
 */
public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Parent root = FXMLLoader.load(getClass().getResource("/com/merzadyan/ui/app.fxml"));
        Parent root = FXMLLoader.load(getClass().getResource("/layout/app.fxml"));
    
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("App");
        primaryStage.show();
    }
    
    public void deleteCrawlDataFile() {}
    
    public void deleteConsoleText() {}
    
    public void startCrawlers() {}
    
    public void stopCrawlers() {}
    
    public void addSeedUrl() {}
    
    public void addSoi() {}
    
    public void resetConfigs() {}
    
    public void saveConfigs() {}
}
