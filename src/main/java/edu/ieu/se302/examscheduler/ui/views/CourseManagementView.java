package edu.ieu.se302.examscheduler.ui.views;

import com.examscheduler.entity.Course;
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

public class CourseManagementView {

    private final BorderPane root = new BorderPane();
    private final ObservableList<Course> courses = FXCollections.observableArrayList();
    private final ListView<Course> courseList = new ListView<>(courses);

    public CourseManagementView() {
        // Sample Data
        courses.add(new Course("CS101", "Intro to Programming", "CS101", 6));
        courses.add(new Course("MATH201", "Calculus II", "MATH201", 4));


        root.setPadding(new Insets(10));

        Label title = new Label("Course Management");
        title.getStyleClass().add("view-title");

        courseList.setCellFactory(new Callback<ListView<Course>, ListCell<Course>>() {
            @Override
            public ListCell<Course> call(ListView<Course> param) {
                return new ListCell<Course>() {
                    @Override
                    protected void updateItem(Course item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getCourseCode() + " - " + item.getCourseName());
                        }
                    }
                };
            }
        });

        Button addBtn = new Button("Add");
        addBtn.setOnAction(e -> showCourseDialog(null));

        Button editBtn = new Button("Edit");
        editBtn.setOnAction(e -> {
            Course selectedCourse = courseList.getSelectionModel().getSelectedItem();
            if (selectedCourse != null) {
                showCourseDialog(selectedCourse);
            } else {
                info("No course selected.");
            }
        });

        Button deleteBtn = new Button("Delete");
        deleteBtn.setOnAction(e -> {
            Course selectedCourse = courseList.getSelectionModel().getSelectedItem();
            if (selectedCourse != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Delete Course");
                alert.setHeaderText("Are you sure you want to delete " + selectedCourse.getCourseName() + "?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    courses.remove(selectedCourse);
                }
            } else {
                info("No course selected.");
            }
        });

        HBox buttons = new HBox(8, addBtn, editBtn, deleteBtn);
        VBox left = new VBox(10, title, new Label("Courses"), courseList, buttons);
        left.setPadding(new Insets(0, 10, 0, 0));

        root.setLeft(left);
    }

    public ObservableList<Course> getCourses() {
        return courses;
    }

    public Parent getView() {
        return root;
    }

    private void showCourseDialog(Course course) {
        Dialog<Course> dialog = new Dialog<>();
        dialog.setTitle(course == null ? "Add Course" : "Edit Course");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField idField = new TextField();
        idField.setPromptText("Course ID");
        if (course != null) {
            idField.setText(course.getCourseId());
            idField.setEditable(false);
        }
        
        TextField nameField = new TextField();
        nameField.setPromptText("Course Name");
        if (course != null) {
            nameField.setText(course.getCourseName());
        }

        TextField codeField = new TextField();
        codeField.setPromptText("Course Code");
        if (course != null) {
            codeField.setText(course.getCourseCode());
        }

        Spinner<Integer> creditsSpinner = new Spinner<>(1, 10, 3);
        if (course != null) {
            creditsSpinner.getValueFactory().setValue(course.getCredits());
        }


        grid.add(new Label("Course ID:"), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(new Label("Course Name:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Course Code:"), 0, 2);
        grid.add(codeField, 1, 2);
        grid.add(new Label("Credits:"), 0, 3);
        grid.add(creditsSpinner, 1, 3);

        dialog.getDialogPane().setContent(grid);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        final Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(
                javafx.event.ActionEvent.ACTION,
                event -> {
                    if (!validateCourse(idField.getText(), nameField.getText(), codeField.getText())) {
                        event.consume();
                    }
                }
        );

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (course == null) {
                    return new Course(idField.getText(), nameField.getText(), codeField.getText(), creditsSpinner.getValue());
                } else {
                    course.setCourseName(nameField.getText());
                    course.setCourseCode(codeField.getText());
                    course.setCredits(creditsSpinner.getValue());
                    return course;
                }
            }
            return null;
        });

        Optional<Course> result = dialog.showAndWait();
        result.ifPresent(c -> {
            if (course == null) {
                courses.add(c);
            } else {
                courseList.refresh();
            }
        });
    }
    
    private boolean validateCourse(String id, String name, String code) {
        String errorMessage = "";
        if (id == null || id.trim().isEmpty()) {
            errorMessage += "Course ID cannot be empty.\n";
        }
        if (name == null || name.trim().isEmpty()) {
            errorMessage += "Course name cannot be empty.\n";
        }
        if (code == null || code.trim().isEmpty()) {
            errorMessage += "Course code cannot be empty.\n";
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
