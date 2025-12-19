package edu.ieu.se302.examscheduler.ui.views;

import com.examscheduler.entity.Course;
import com.examscheduler.entity.Exam;
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

public class ExamManagementView {

    private final BorderPane root = new BorderPane();
    private final ObservableList<Exam> exams;
    private final ObservableList<Course> courses;
    private final ListView<Exam> examList;

    public ExamManagementView(ObservableList<Exam> exams, ObservableList<Course> courses) {
        this.exams = exams;
        this.courses = courses;
        this.examList = new ListView<>(exams);

        root.setPadding(new Insets(10));

        Label title = new Label("Exam Management");
        title.getStyleClass().add("view-title");

        examList.setCellFactory(new Callback<ListView<Exam>, ListCell<Exam>>() {
            @Override
            public ListCell<Exam> call(ListView<Exam> param) {
                return new ListCell<Exam>() {
                    @Override
                    protected void updateItem(Exam item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            String courseLabel = item.getCourse() != null ? item.getCourse().getCourseCode() : "N/A";
                            setText(item.getExamId() + " - " + courseLabel + " (" + item.getExamType() + ")");
                        }
                    }
                };
            }
        });

        Button addBtn = new Button("Add");
        addBtn.setOnAction(e -> showExamDialog(null));

        Button editBtn = new Button("Edit");
        editBtn.setOnAction(e -> {
            Exam selectedExam = examList.getSelectionModel().getSelectedItem();
            if (selectedExam != null) {
                showExamDialog(selectedExam);
            } else {
                info("No exam selected.");
            }
        });

        Button deleteBtn = new Button("Delete");
        deleteBtn.setOnAction(e -> {
            Exam selectedExam = examList.getSelectionModel().getSelectedItem();
            if (selectedExam != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Delete Exam");
                alert.setHeaderText("Are you sure you want to delete exam " + selectedExam.getExamId() + "?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    exams.remove(selectedExam);
                }
            } else {
                info("No exam selected.");
            }
        });

        HBox buttons = new HBox(8, addBtn, editBtn, deleteBtn);
        VBox left = new VBox(10, title, new Label("Exams"), examList, buttons);
        left.setPadding(new Insets(0, 10, 0, 0));

        root.setLeft(left);
    }

    public ObservableList<Exam> getExams() {
        return exams;
    }

    public Parent getView() {
        return root;
    }

    private void showExamDialog(Exam exam) {
        Dialog<Exam> dialog = new Dialog<>();
        dialog.setTitle(exam == null ? "Add Exam" : "Edit Exam");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField idField = new TextField();
        idField.setPromptText("Exam ID");
        if (exam != null) {
            idField.setText(exam.getExamId());
            idField.setEditable(false);
        }

        ComboBox<Course> courseBox = new ComboBox<>(courses);
        courseBox.setPromptText("Course");
        courseBox.setCellFactory(param -> new ListCell<Course>() {
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
        courseBox.setButtonCell(new ListCell<Course>() {
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

        TextField typeField = new TextField();
        typeField.setPromptText("Exam Type (e.g., Final)");

        Spinner<Integer> durationSpinner = new Spinner<>(30, 300, 120, 10);

        if (exam != null) {
            courseBox.setValue(exam.getCourse());
            typeField.setText(exam.getExamType());
            durationSpinner.getValueFactory().setValue(exam.getDurationMinutes());
        }

        grid.add(new Label("Exam ID:"), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(new Label("Course:"), 0, 1);
        grid.add(courseBox, 1, 1);
        grid.add(new Label("Exam Type:"), 0, 2);
        grid.add(typeField, 1, 2);
        grid.add(new Label("Duration (min):"), 0, 3);
        grid.add(durationSpinner, 1, 3);

        dialog.getDialogPane().setContent(grid);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        final Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(
                javafx.event.ActionEvent.ACTION,
                event -> {
                    if (!validateExam(idField.getText(), courseBox.getValue(), typeField.getText(), durationSpinner.getValue(), exam)) {
                        event.consume();
                    }
                }
        );

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (exam == null) {
                    return new Exam(idField.getText(), courseBox.getValue(), typeField.getText(), durationSpinner.getValue());
                } else {
                    exam.setCourse(courseBox.getValue());
                    exam.setExamType(typeField.getText());
                    exam.setDurationMinutes(durationSpinner.getValue());
                    return exam;
                }
            }
            return null;
        });

        Optional<Exam> result = dialog.showAndWait();
        result.ifPresent(examResult -> {
            if (exam == null) {
                exams.add(examResult);
            } else {
                examList.refresh();
            }
        });
    }

    private boolean validateExam(String id, Course course, String type, Integer duration, Exam editingExam) {
        String errorMessage = "";
        if (id == null || id.trim().isEmpty()) {
            errorMessage += "Exam ID cannot be empty.\n";
        } else if (editingExam == null && exams.stream().anyMatch(existing -> id.equals(existing.getExamId()))) {
            errorMessage += "Exam ID already exists.\n";
        }
        if (course == null) {
            errorMessage += "Course must be selected.\n";
        }
        if (type == null || type.trim().isEmpty()) {
            errorMessage += "Exam type cannot be empty.\n";
        }
        if (duration == null || duration <= 0) {
            errorMessage += "Duration must be greater than 0.\n";
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
