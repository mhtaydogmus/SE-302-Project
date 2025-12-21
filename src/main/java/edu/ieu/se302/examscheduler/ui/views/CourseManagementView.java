package edu.ieu.se302.examscheduler.ui.views;

import com.examscheduler.entity.Course;
import com.examscheduler.entity.Enrollment;
import com.examscheduler.entity.ExamSession;
import com.examscheduler.entity.Room;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.stream.Collectors;

public class CourseManagementView {

    private final BorderPane root = new BorderPane();
    private final ObservableList<Course> courses = FXCollections.observableArrayList();
    private final ListView<Course> courseList = new ListView<>(courses);
    private final ObservableList<ExamSession> scheduleSessions;
    private final ObservableList<Enrollment> enrollments;
    private final TableView<ExamSession> courseScheduleTable = new TableView<>();
    private Label courseScheduleTitle;

    public CourseManagementView(ObservableList<ExamSession> scheduleSessions, ObservableList<Enrollment> enrollments) {
        this.scheduleSessions = scheduleSessions;
        this.enrollments = enrollments;

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
                            String courseCode = item.getCourseCode() != null && !item.getCourseCode().isBlank() ? item.getCourseCode() : item.getCourseId();
                            String courseName = item.getCourseName() != null && !item.getCourseName().isBlank() ? item.getCourseName() : "No Name";
                            setText(courseCode + " - " + courseName + " (" + item.getCredits() + " credits)");
                        }
                    }
                };
            }
        });
        courseList.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            updateCourseSchedule(newSelection);
        });
        scheduleSessions.addListener((ListChangeListener<ExamSession>) change -> {
            updateCourseSchedule(courseList.getSelectionModel().getSelectedItem());
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
                    removeEnrollmentsForCourse(selectedCourse);
                }
            } else {
                info("No course selected.");
            }
        });

        HBox buttons = new HBox(8, addBtn, editBtn, deleteBtn);
        VBox left = new VBox(10, title, new Label("Courses"), courseList, buttons);
        left.setPadding(new Insets(0, 10, 0, 0));

        root.setLeft(left);
        root.setCenter(buildCourseSchedulePanel());
    }

    public ObservableList<Course> getCourses() {
        return courses;
    }

    public Parent getView() {
        return root;
    }

    private VBox buildCourseSchedulePanel() {
        courseScheduleTitle = new Label("Course Schedule");
        courseScheduleTitle.getStyleClass().add("view-subtitle");

        TableColumn<ExamSession, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTimeSlot().getDate()));

        TableColumn<ExamSession, LocalTime> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTimeSlot().getStartTime()));

        TableColumn<ExamSession, String> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(cellData -> {
            ExamSession session = cellData.getValue();
            Room room = session != null ? session.getRoom() : null;
            if (room == null) {
                return new SimpleStringProperty("N/A");
            }
            // Prefer roomName, fallback to roomId if name is empty
            String roomName = room.getRoomName();
            if (roomName != null && !roomName.isBlank()) {
                return new SimpleStringProperty(roomName);
            }
            String roomId = room.getRoomId();
            return new SimpleStringProperty(roomId != null && !roomId.isBlank() ? roomId : "N/A");
        });

        courseScheduleTable.getColumns().addAll(dateCol, timeCol, roomCol);
        courseScheduleTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        courseScheduleTable.setPlaceholder(new Label("Select a course to view its schedule."));

        VBox panel = new VBox(10, courseScheduleTitle, courseScheduleTable);
        panel.setPadding(new Insets(0, 0, 0, 10));
        return panel;
    }

    private void updateCourseSchedule(Course course) {
        if (course == null) {
            courseScheduleTitle.setText("Course Schedule");
            courseScheduleTable.setItems(FXCollections.observableArrayList());
            return;
        }

        courseScheduleTitle.setText("Course Schedule - " + course.getCourseName());
        courseScheduleTable.setItems(
                FXCollections.observableArrayList(
                        scheduleSessions.stream()
                                .filter(session -> session.getExam() != null && course.equals(session.getExam().getCourse()))
                                .collect(Collectors.toList())
                )
        );
    }

    private void removeEnrollmentsForCourse(Course course) {
        if (enrollments == null || course == null) {
            return;
        }
        enrollments.removeIf(enrollment -> {
            if (!course.equals(enrollment.getCourse())) {
                return false;
            }
            if (enrollment.getStudent() != null) {
                enrollment.getStudent().removeEnrollment(enrollment);
            }
            if (enrollment.getCourse() != null) {
                enrollment.getCourse().removeEnrollment(enrollment);
            }
            return true;
        });
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
        // Require at least courseId OR courseCode (matches CSV import behavior)
        boolean hasId = id != null && !id.trim().isEmpty();
        boolean hasCode = code != null && !code.trim().isEmpty();

        if (!hasId && !hasCode) {
            errorMessage += "Either Course ID or Course Code must be provided.\n";
        }
        // Course name is optional (matches CSV import behavior)

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
