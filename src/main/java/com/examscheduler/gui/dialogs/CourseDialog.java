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
        idField.setPromptText("e.g. 501");
        TextField codeField = new TextField();
        TextField nameField = new TextField();
        TextField creditsField = new TextField();

        if (course != null) {
            // Course artık int courseId kullanıyor
            idField.setText(String.valueOf(course.getCourseId()));
            codeField.setText(course.getCourseCode());
            nameField.setText(course.getCourseName());
            creditsField.setText(String.valueOf(course.getCredits()));

            idField.setDisable(true); // ID değiştirilemez
        }

        Button save = new Button("Save");
        save.setOnAction(e -> {
            String idStr = idField.getText().trim();
            String code = codeField.getText().trim();
            String name = nameField.getText().trim();
            String creditsStr = creditsField.getText().trim();

            if (idStr.isEmpty() || code.isEmpty() || name.isEmpty() || creditsStr.isEmpty()) {
                alert("All fields are required!");
                return;
            }

            int courseId;
            int credits;

            // ID'yi int'e çevir
            try {
                courseId = Integer.parseInt(idStr);
            } catch (NumberFormatException ex) {
                alert("Course ID must be a valid number!");
                return;
            }

            // Credits'i int'e çevir
            try {
                credits = Integer.parseInt(creditsStr);
                if (credits <= 0) {
                    alert("Credits must be a positive number!");
                    return;
                }
            } catch (NumberFormatException ex) {
                alert("Credits must be a valid number!");
                return;
            }

            // Yeni eklemede duplicate ID kontrolü
            if (course == null && DataStore.getCourses().stream()
                    .anyMatch(c -> c.getCourseId() == courseId)) {
                alert("Course ID already exists!");
                return;
            }

            if (course == null) {
                // Dökümana göre constructor sırası: courseId, courseCode, courseName, credits
                Course newCourse = new Course(courseId, code, name, credits);
                DataStore.getCourses().add(newCourse);
            } else {
                // Edit modunda sadece değiştirilebilir alanlar güncellenir
                course.setCourseCode(code);
                course.setCourseName(name);
                course.setCredits(credits);
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
        sizeToScene();
    }

    private void alert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}