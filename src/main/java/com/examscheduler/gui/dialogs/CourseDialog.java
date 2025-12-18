package com.examscheduler.gui.dialogs;

import com.examscheduler.data.DataStore;
import com.examscheduler.entity.Course;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class CourseDialog extends Stage {

    public CourseDialog(Course course) {
        setTitle(course == null ? "Add Course" : "Edit Course");
        initModality(Modality.APPLICATION_MODAL);

        TextField idField = new TextField();
        TextField codeField = new TextField();
        TextField nameField = new TextField();
        TextField creditsField = new TextField();

        if (course != null) {
            idField.setText(course.getCourseId());
            codeField.setText(course.getCourseCode());
            nameField.setText(course.getCourseName());
            creditsField.setText(String.valueOf(course.getCredits()));
            idField.setDisable(true); // ID değiştirilemez
        }

        Button save = new Button("Save");
        save.setOnAction(e -> {
            if (idField.getText().isBlank() || codeField.getText().isBlank() || nameField.getText().isBlank() || creditsField.getText().isBlank()) {
                alert("All fields are required!");
                return;
            }

            try {
                Integer.parseInt(creditsField.getText());
            } catch (NumberFormatException ex) {
                alert("Credits must be a number!");
                return;
            }

            boolean idExists = DataStore.courses.stream().anyMatch(c -> c.getCourseId().equals(idField.getText()));
            if (course == null && idExists) {
                alert("Course ID already exists!");
                return;
            }

            if (course == null) {
                Course newCourse = new Course(
                        idField.getText(),
                        nameField.getText(),
                        codeField.getText(),
                        Integer.parseInt(creditsField.getText())
                );
                DataStore.courses.add(newCourse);
            } else {
                course.setCourseCode(codeField.getText());
                course.setCourseName(nameField.getText());
                course.setCredits(Integer.parseInt(creditsField.getText()));
            }
            close();
        });

        Button cancel = new Button("Cancel");
        cancel.setOnAction(e -> close());

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Course ID:"), idField);
        grid.addRow(1, new Label("Course Code:"), codeField);
        grid.addRow(2, new Label("Course Name:"), nameField);
        grid.addRow(3, new Label("Credits:"), creditsField);
        grid.addRow(4, save, cancel);

        setScene(new Scene(grid));
    }

    private void alert(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }
}