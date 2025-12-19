package com.examscheduler.gui.dialogs;

import com.examscheduler.data.DataStore;
import com.examscheduler.entity.Course;
import com.examscheduler.entity.Exam;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ExamDialog extends Stage {

    public ExamDialog(Exam exam) {
        initModality(Modality.APPLICATION_MODAL);
        setTitle(exam == null ? "Add Exam" : "Edit Exam");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        ComboBox<Course> courseCombo = new ComboBox<>();
        courseCombo.getItems().addAll(DataStore.getCourses());
        courseCombo.setPromptText("Select Course");

        TextField nameField = new TextField();
        TextField durationField = new TextField();
        durationField.setPromptText("Duration in minutes");

        if (exam != null) {
            courseCombo.setValue(exam.getCourse());
            nameField.setText(exam.getName());
            durationField.setText(String.valueOf(exam.getDurationMinutes()));
            courseCombo.setDisable(true); // Course değiştirilemez
        }

        grid.add(new Label("Course:"), 0, 0);
        grid.add(courseCombo, 1, 0);
        grid.add(new Label("Exam Name:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Duration (min):"), 0, 2);
        grid.add(durationField, 1, 2);

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            Course selectedCourse = courseCombo.getValue();
            String name = nameField.getText().trim();
            String durationStr = durationField.getText().trim();

            if (selectedCourse == null || name.isEmpty() || durationStr.isEmpty()) {
                alert("All fields are required!");
                return;
            }

            int duration;
            try {
                duration = Integer.parseInt(durationStr);
                if (duration <= 0) {
                    alert("Duration must be a positive number!");
                    return;
                }
            } catch (NumberFormatException ex) {
                alert("Duration must be a valid number!");
                return;
            }

            if (exam == null) {
                // Yeni Exam ekle – examId otomatik artsın (int)
                int newExamId = DataStore.getExams().size() + 1;
                Exam newExam = new Exam(newExamId, selectedCourse, name, duration);
                DataStore.getExams().add(newExam);
            } else {
                // Edit modunda sadece değiştirilebilir alanlar güncellenir
                exam.setName(name);
                exam.setDurationMinutes(duration);
            }

            close();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> close());

        grid.add(saveButton, 0, 3);
        grid.add(cancelButton, 1, 3);

        setScene(new Scene(grid));
        sizeToScene();
        setResizable(false);
    }

    private void alert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}