package edu.ieu.se302.examscheduler.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX entry point for the Student Exam Scheduling System (GUI Skeleton).
 * This module only provides the base navigation and placeholder views.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        MainWindow window = new MainWindow();

        Scene scene = new Scene(window.getRoot(), 1100, 700);
        primaryStage.setTitle("Student Exam Scheduling System");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
