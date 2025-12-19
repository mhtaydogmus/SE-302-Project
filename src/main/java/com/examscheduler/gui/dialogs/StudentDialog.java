package com.examscheduler.gui.dialogs;

import com.examscheduler.data.DataStore;
import com.examscheduler.entity.Student;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class StudentDialog extends Stage {

    public StudentDialog(Student student) {
        setTitle(student == null ? "Add Student" : "Edit Student");
        initModality(Modality.APPLICATION_MODAL);

        TextField idField = new TextField();
        idField.setPromptText("e.g. 1001");
        TextField firstNameField = new TextField();
        TextField lastNameField = new TextField();
        TextField emailField = new TextField();

        if (student != null) {
            idField.setText(String.valueOf(student.getStudentId()));
            idField.setDisable(true); // ID değiştirilemez
            firstNameField.setText(student.getFirstName());
            lastNameField.setText(student.getLastName());
            emailField.setText(student.getEmail());
        }

        Button save = new Button("Save");
        Button cancel = new Button("Cancel");

        save.setOnAction(e -> {
            String error = validate(
                    idField.getText().trim(),
                    firstNameField.getText().trim(),
                    lastNameField.getText().trim(),
                    emailField.getText().trim(),
                    student
            );

            if (error != null) {
                showError(error);
                return;
            }

            try {
                int studentId = Integer.parseInt(idField.getText().trim());

                if (student == null) {
                    // Yeni öğrenci ekle
                    Student newStudent = new Student(
                            studentId,
                            firstNameField.getText().trim(),
                            lastNameField.getText().trim(),
                            emailField.getText().trim()
                    );
                    DataStore.getStudents().add(newStudent);
                } else {
                    // Mevcut öğrenciyi güncelle
                    student.setFirstName(firstNameField.getText().trim());
                    student.setLastName(lastNameField.getText().trim());
                    student.setEmail(emailField.getText().trim());
                }
                close();
            } catch (NumberFormatException ex) {
                showError("Student ID must be a valid number");
            }
        });

        cancel.setOnAction(e -> close());

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(15);
        grid.setVgap(12);

        grid.addRow(0, new Label("Student ID:"), idField);
        grid.addRow(1, new Label("First Name:"), firstNameField);
        grid.addRow(2, new Label("Last Name:"), lastNameField);
        grid.addRow(3, new Label("Email:"), emailField);
        grid.addRow(4, save, cancel);

        setScene(new Scene(grid, 400, 280));
        setResizable(false);
    }

    private String validate(String idStr, String first, String last, String email, Student existingStudent) {
        if (idStr.isBlank()) return "Student ID cannot be empty";
        if (first.isBlank()) return "First name cannot be empty";
        if (last.isBlank()) return "Last name cannot be empty";
        if (email.isBlank()) return "Email cannot be empty";
        if (!email.contains("@") || !email.contains(".")) return "Invalid email address";

        try {
            int id = Integer.parseInt(idStr);

            // Sadece yeni öğrenci eklerken duplicate ID kontrolü
            if (existingStudent == null) {
                boolean exists = DataStore.getStudents().stream()
                        .anyMatch(s -> s.getStudentId() == id);
                if (exists) return "Student ID already exists";
            }
        } catch (NumberFormatException e) {
            return "Student ID must be a number";
        }

        return null;
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.initOwner(this);
        alert.showAndWait();
    }
}