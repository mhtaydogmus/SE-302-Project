package edu.ieu.se302.examscheduler.ui.views;

import com.examscheduler.entity.Course;
import com.examscheduler.entity.Enrollment;
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

import java.util.Optional;
import java.util.UUID;

public class EnrollmentManagementView {

    private final BorderPane root = new BorderPane();
    private final ObservableList<Enrollment> enrollments;
    private final ObservableList<Student> students;
    private final ObservableList<Course> courses;
    private final ListView<Enrollment> enrollmentList;

    public EnrollmentManagementView(ObservableList<Enrollment> enrollments,
                                    ObservableList<Student> students,
                                    ObservableList<Course> courses) {
        this.enrollments = enrollments;
        this.students = students;
        this.courses = courses;
        this.enrollmentList = new ListView<>(enrollments);

        root.setPadding(new Insets(10));

        Label title = new Label("Enrollment Management");
        title.getStyleClass().add("view-title");

        enrollmentList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Enrollment item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String studentLabel = item.getStudent() != null ? item.getStudent().getStudentId() : "N/A";
                    String courseLabel = item.getCourse() != null ? item.getCourse().getCourseCode() : "N/A";
                    setText(studentLabel + " -> " + courseLabel);
                }
            }
        });

        Button addBtn = new Button("Add");
        addBtn.setOnAction(e -> showEnrollmentDialog(null));

        Button deleteBtn = new Button("Delete");
        deleteBtn.setOnAction(e -> {
            Enrollment selectedEnrollment = enrollmentList.getSelectionModel().getSelectedItem();
            if (selectedEnrollment != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Delete Enrollment");
                alert.setHeaderText("Are you sure you want to delete this enrollment?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    removeEnrollment(selectedEnrollment);
                }
            } else {
                info("No enrollment selected.");
            }
        });

        HBox buttons = new HBox(8, addBtn, deleteBtn);
        VBox left = new VBox(10, title, new Label("Enrollments"), enrollmentList, buttons);
        left.setPadding(new Insets(0, 10, 0, 0));

        root.setLeft(left);
    }

    public Parent getView() {
        return root;
    }

    private void showEnrollmentDialog(Enrollment enrollment) {
        Dialog<Enrollment> dialog = new Dialog<>();
        dialog.setTitle(enrollment == null ? "Add Enrollment" : "Edit Enrollment");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<Student> studentBox = new ComboBox<>(students);
        studentBox.setPromptText("Student");
        studentBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Student item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getStudentId() + " - " + item.getFullName());
                }
            }
        });
        studentBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Student item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getStudentId() + " - " + item.getFullName());
                }
            }
        });

        ComboBox<Course> courseBox = new ComboBox<>(courses);
        courseBox.setPromptText("Course");
        courseBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Course item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getCourseCode() + " - " + item.getCourseName());
                }
            }
        });
        courseBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Course item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getCourseCode() + " - " + item.getCourseName());
                }
            }
        });

        if (enrollment != null) {
            studentBox.setValue(enrollment.getStudent());
            courseBox.setValue(enrollment.getCourse());
        }

        grid.add(new Label("Student:"), 0, 0);
        grid.add(studentBox, 1, 0);
        grid.add(new Label("Course:"), 0, 1);
        grid.add(courseBox, 1, 1);

        dialog.getDialogPane().setContent(grid);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        final Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(
                javafx.event.ActionEvent.ACTION,
                event -> {
                    if (!validateEnrollment(studentBox.getValue(), courseBox.getValue(), enrollment)) {
                        event.consume();
                    }
                }
        );

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Student student = studentBox.getValue();
                Course course = courseBox.getValue();
                if (enrollment == null) {
                    return new Enrollment(UUID.randomUUID().toString(), student, course);
                } else {
                    Student oldStudent = enrollment.getStudent();
                    Course oldCourse = enrollment.getCourse();
                    if (oldStudent != null && !oldStudent.equals(student)) {
                        oldStudent.removeEnrollment(enrollment);
                    }
                    if (oldCourse != null && !oldCourse.equals(course)) {
                        oldCourse.removeEnrollment(enrollment);
                    }
                    enrollment.setStudent(student);
                    enrollment.setCourse(course);
                    if (student != null) {
                        student.addEnrollment(enrollment);
                    }
                    if (course != null) {
                        course.addEnrollment(enrollment);
                    }
                    return enrollment;
                }
            }
            return null;
        });

        Optional<Enrollment> result = dialog.showAndWait();
        result.ifPresent(newEnrollment -> {
            if (enrollment == null) {
                addEnrollment(newEnrollment);
            } else {
                enrollmentList.refresh();
            }
        });
    }

    private boolean validateEnrollment(Student student, Course course, Enrollment editing) {
        String errorMessage = "";
        if (student == null) {
            errorMessage += "Student must be selected.\n";
        }
        if (course == null) {
            errorMessage += "Course must be selected.\n";
        }
        if (student != null && course != null) {
            boolean exists = enrollments.stream()
                    .anyMatch(existing -> existing != editing &&
                            student.equals(existing.getStudent()) &&
                            course.equals(existing.getCourse()));
            if (exists) {
                errorMessage += "This student is already enrolled in the selected course.\n";
            }
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            error(errorMessage);
            return false;
        }
    }

    private void addEnrollment(Enrollment enrollment) {
        enrollments.add(enrollment);
        if (enrollment.getStudent() != null) {
            enrollment.getStudent().addEnrollment(enrollment);
        }
        if (enrollment.getCourse() != null) {
            enrollment.getCourse().addEnrollment(enrollment);
        }
    }

    private void removeEnrollment(Enrollment enrollment) {
        enrollments.remove(enrollment);
        if (enrollment.getStudent() != null) {
            enrollment.getStudent().removeEnrollment(enrollment);
        }
        if (enrollment.getCourse() != null) {
            enrollment.getCourse().removeEnrollment(enrollment);
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
