package edu.ieu.se302.examscheduler.ui.views;

import com.examscheduler.entity.Student;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.util.Optional;

public class StudentManagementView {

    private final BorderPane root = new BorderPane();
    private final ObservableList<Student> students = FXCollections.observableArrayList();
    private final ListView<Student> studentList = new ListView<>(students);

    public StudentManagementView() {
        // Sample Data
        students.add(new Student("101", "Ali", "Veli", "ali@mail.com"));
        students.add(new Student("102", "Ayşe", "Fatma", "ayse@mail.com"));

        root.setPadding(new Insets(10));

        Label title = new Label("Student Management");
        title.getStyleClass().add("view-title");

        studentList.setCellFactory(new Callback<ListView<Student>, ListCell<Student>>() {
            @Override
            public ListCell<Student> call(ListView<Student> param) {
                return new ListCell<Student>() {
                    @Override
                    protected void updateItem(Student item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getStudentId() + " - " + item.getFullName() + " (" + item.getEmail() + ")");
                        }
                    }
                };
            }
        });

        Button addBtn = new Button("Add");
        addBtn.setOnAction(e -> showStudentDialog(null));

        Button editBtn = new Button("Edit");
        editBtn.setOnAction(e -> {
            Student selectedStudent = studentList.getSelectionModel().getSelectedItem();
            if (selectedStudent != null) {
                showStudentDialog(selectedStudent);
            } else {
                info("No student selected.");
            }
        });

        Button deleteBtn = new Button("Delete");
        deleteBtn.setOnAction(e -> {
            Student selectedStudent = studentList.getSelectionModel().getSelectedItem();
            if (selectedStudent != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Delete Student");
                alert.setHeaderText("Are you sure you want to delete " + selectedStudent.getFullName() + "?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    students.remove(selectedStudent);
                }
            } else {
                info("No student selected.");
            }
        });

        HBox buttons = new HBox(8, addBtn, editBtn, deleteBtn);
        VBox left = new VBox(10, title, new Label("Students"), studentList, buttons);
        left.setPadding(new Insets(0, 10, 0, 0));

        root.setLeft(left);
    }

    public ObservableList<Student> getStudents() {
        return students;
    }

    public Parent getView() {
        return root;
    }

    private void showStudentDialog(Student student) {
        Dialog<Student> dialog = new Dialog<>();
        dialog.setTitle(student == null ? "Add Student" : "Edit Student");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField idField = new TextField();
        idField.setPromptText("Student ID");
        if (student != null) {
            idField.setText(student.getStudentId());
            idField.setEditable(false);
        }

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");
        if (student != null) {
            firstNameField.setText(student.getFirstName());
        }

        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");
        if (student != null) {
            lastNameField.setText(student.getLastName());
        }
        
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        if (student != null) {
            emailField.setText(student.getEmail());
        }

        grid.add(new Label("Student ID:"), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(new Label("First Name:"), 0, 1);
        grid.add(firstNameField, 1, 1);
        grid.add(new Label("Last Name:"), 0, 2);
        grid.add(lastNameField, 1, 2);
        grid.add(new Label("Email:"), 0, 3);
        grid.add(emailField, 1, 3);
        

        dialog.getDialogPane().setContent(grid);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        final Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(
                javafx.event.ActionEvent.ACTION,
                event -> {
                    if (!validateStudent(idField.getText(), firstNameField.getText(), lastNameField.getText(), emailField.getText())) {
                        event.consume();
                    }
                }
        );

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (student == null) {
                    return new Student(idField.getText(), firstNameField.getText(), lastNameField.getText(), emailField.getText());
                } else {
                    student.setFirstName(firstNameField.getText());
                    student.setLastName(lastNameField.getText());
                    student.setEmail(emailField.getText());
                    return student;
                }
            }
            return null;
        });

        Optional<Student> result = dialog.showAndWait();
        result.ifPresent(s -> {
            if (student == null) {
                students.add(s);
            } else {
                studentList.refresh();
            }
        });
    }

    private boolean validateStudent(String id, String firstName, String lastName, String email) {
        String errorMessage = "";
        if (id == null || id.trim().isEmpty()) {
            errorMessage += "Student ID cannot be empty.\n";
        }
        if (firstName == null || firstName.trim().isEmpty()) {
            errorMessage += "First name cannot be empty.\n";
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            errorMessage += "Last name cannot be empty.\n";
        }
        if (email == null || !email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) {
            errorMessage += "Invalid email address.\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            error(errorMessage);
            return false;
        }
    }

    private void info(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void error(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Invalid Input");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
