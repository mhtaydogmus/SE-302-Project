package edu.ieu.se302.examscheduler.ui.views;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Placeholder view for Course Management (Meeting Report 4 - Task 3).
 * This view is intentionally minimal and will be wired to entity classes later.
 */
public class CourseManagementView {

    private final BorderPane root = new BorderPane();

    public CourseManagementView() {
        root.setPadding(new Insets(10));

        Label title = new Label("Course Management");
        title.getStyleClass().add("view-title");

        ListView<String> courseList = new ListView<>();
        courseList.getItems().addAll(
                "CS101 - Intro to Programming",
                "MATH201 - Calculus II"
        );

        TextField codeField = new TextField();
        codeField.setPromptText("Course Code (e.g., CS101)");

        TextField nameField = new TextField();
        nameField.setPromptText("Course Title");

        Button addBtn = new Button("Add");
        Button editBtn = new Button("Edit");
        Button deleteBtn = new Button("Delete");

        addBtn.setOnAction(e -> info("Add action is a placeholder."));
        editBtn.setOnAction(e -> info("Edit action is a placeholder."));
        deleteBtn.setOnAction(e -> info("Delete action is a placeholder."));

        HBox buttons = new HBox(8, addBtn, editBtn, deleteBtn);
        VBox form = new VBox(8, new Label("Create / Edit Course (Placeholder)"),
                codeField, nameField, buttons);
        form.setPadding(new Insets(10));
        form.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 6; -fx-background-radius: 6;");

        VBox left = new VBox(10, title, new Label("Courses"), courseList);
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
