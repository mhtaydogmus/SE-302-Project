package com.examscheduler.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private static MainView mainView;

    @Override
    public void start(Stage primaryStage) {
        mainView = new MainView();
        Scene scene = new Scene(mainView, 1200, 800);

        mainView.setStage(primaryStage);

        primaryStage.setTitle("Exam Scheduling System");
        primaryStage.setScene(scene);
        primaryStage.show();

        // runAutomatedTest();   ← bunu sil veya yorum yap
    }

    public static void refreshTables() {
        if (mainView != null) {
            mainView.refreshTables();
        }
    }

    // runAutomatedTest metodunu tamamen kaldır (veya yorum satırına al)

    public static void main(String[] args) {
        launch(args);
    }
}