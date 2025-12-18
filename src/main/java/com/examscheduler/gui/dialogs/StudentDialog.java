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
        TextField firstNameField = new TextField();
        TextField lastNameField = new TextField();
        TextField emailField = new TextField();

        if (student != null) {
            idField.setText(student.getStudentId());
            idField.setDisable(true); // ID editlenmesin
            firstNameField.setText(student.getFirstName());
            lastNameField.setText(student.getLastName());
            emailField.setText(student.getEmail());
        }

        Button save = new Button("Save");
        Button cancel = new Button("Cancel");

        save.setOnAction(e -> {
            String error = validate(
                    idField.getText(),
                    firstNameField.getText(),
                    lastNameField.getText(),
                    emailField.getText()
            );

            if (error != null) {
                showError(error);
                return;
            }

            if (student == null) {
                Student s = new Student(
                        idField.getText(),
                        firstNameField.getText(),
                        lastNameField.getText(),
                        emailField.getText()
                );
                DataStore.students.add(s);
            } else {
                student.setFirstName(firstNameField.getText());
                student.setLastName(lastNameField.getText());
                student.setEmail(emailField.getText());
            }
            close();
        });

        cancel.setOnAction(e -> close());

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15));
        grid.setHgap(10);
        grid.setVgap(10);

        grid.addRow(0, new Label("Student ID"), idField);
        grid.addRow(1, new Label("First Name"), firstNameField);
        grid.addRow(2, new Label("Last Name"), lastNameField);
        grid.addRow(3, new Label("Email"), emailField);
        grid.addRow(4, save, cancel);

        setScene(new Scene(grid));
    }

    private String validate(String id, String first, String last, String email) {
        if (id.isBlank()) return "Student ID cannot be empty";
        if (first.isBlank()) return "First name cannot be empty";
        if (last.isBlank()) return "Last name cannot be empty";
        if (!email.contains("@")) return "Invalid email address";

        boolean exists = DataStore.students.stream()
                .anyMatch(s -> s.getStudentId().equals(id));

        if (exists) return "Student ID already exists";

        return null;
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }
}
