package edu.ieu.se302.examscheduler.ui.views;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Placeholder view for Student Management (Meeting Report 4 - Task 3).
 * This view is intentionally minimal and will be wired to entity classes later.
 */
public class StudentManagementView {

    private final BorderPane root = new BorderPane();

    public StudentManagementView() {
        root.setPadding(new Insets(10));

        Label title = new Label("Student Management");
        title.getStyleClass().add("view-title");

        // Placeholder list
        ListView<String> studentList = new ListView<>();
        studentList.getItems().addAll(
                "Student #101 - Ali (ali@mail.com)",
                "Student #102 - Ayşe (ayse@mail.com)"
        );

        // Placeholder form controls
        TextField idField = new TextField();
        idField.setPromptText("Student ID");

        TextField nameField = new TextField();
        nameField.setPromptText("Name");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        Button addBtn = new Button("Add");
        Button editBtn = new Button("Edit");
        Button deleteBtn = new Button("Delete");

        // Skeleton actions (no persistence yet)
        addBtn.setOnAction(e -> info("Add action is a placeholder."));
        editBtn.setOnAction(e -> info("Edit action is a placeholder."));
        deleteBtn.setOnAction(e -> info("Delete action is a placeholder."));

        HBox buttons = new HBox(8, addBtn, editBtn, deleteBtn);
        VBox form = new VBox(8, new Label("Create / Edit Student (Placeholder)"),
                idField, nameField, emailField, buttons);
        form.setPadding(new Insets(10));
        form.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 6; -fx-background-radius: 6;");

        VBox left = new VBox(10, title, new Label("Students"), studentList);
        left.setPadding(new Insets(0, 10, 0, 0));

        root.setLeft(left);
        root.setCenter(form);
    }

    public Parent getView() {
        return root;
    }

    private void info(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
