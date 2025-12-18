package edu.ieu.se302.examscheduler.ui.views;

import com.examscheduler.entity.*;
import com.examscheduler.scheduler.Scheduler;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ScheduleGenerationView {

    private final BorderPane root = new BorderPane();
    private final ObservableList<Student> students;
    private final ObservableList<Course> courses;
    private final ObservableList<Room> rooms;
    private final ObservableList<TimeSlot> timeSlots;
    private final TableView<ExamSession> scheduleTable = new TableView<>();

    public ScheduleGenerationView(ObservableList<Student> students, ObservableList<Course> courses, ObservableList<Room> rooms, ObservableList<TimeSlot> timeSlots) {
        this.students = students;
        this.courses = courses;
        this.rooms = rooms;
        this.timeSlots = timeSlots;

        root.setPadding(new Insets(10));

        Label title = new Label("Schedule Generation");
        title.getStyleClass().add("view-title");

        setupTable();

        Button generateBtn = new Button("Generate Schedule");

        generateBtn.setOnAction(e -> {
            if (students.isEmpty() || courses.isEmpty() || rooms.isEmpty() || timeSlots.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Cannot Generate Schedule");
                alert.setHeaderText(null);
                alert.setContentText("Please add students, courses, rooms, and time slots before generating a schedule.");
                alert.showAndWait();
                return;
            }

            // 1. Create dummy enrollments
            for (Student student : students) {
                for (Course course : courses) {
                    Enrollment enrollment = new Enrollment(UUID.randomUUID().toString(), student, course);
                    student.addEnrollment(enrollment);
                    course.addEnrollment(enrollment);
                }
            }

            // 2. Create exams for each course
            List<Exam> exams = new ArrayList<>();
            for (Course course : courses) {
                exams.add(new Exam(UUID.randomUUID().toString(), course, "Final", 120));
            }

            // 3. Configure and run the scheduler
            Scheduler scheduler = new Scheduler(new ArrayList<>(rooms), new ArrayList<>(timeSlots), 2);
            Schedule schedule = scheduler.generateSchedule(new ArrayList<>(courses), exams);
            
            // 4. Display the results
            scheduleTable.setItems(FXCollections.observableArrayList(schedule.getExamSessions()));

            // 5. Show violations in an alert
            List<String> violations = schedule.validate();
            if (!violations.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Scheduling Violations");
                alert.setHeaderText("The generated schedule has conflicts.");
                
                TextArea textArea = new TextArea(String.join("\n- ", violations));
                textArea.setEditable(false);
                textArea.setWrapText(true);
                
                alert.getDialogPane().setContent(textArea);
                alert.showAndWait();
            } else {
                 Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Schedule Generated");
                alert.setHeaderText(null);
                alert.setContentText("Schedule generated successfully with no violations.");
                alert.showAndWait();
            }
        });

        HBox buttons = new HBox(8, generateBtn);

        VBox controls = new VBox(10, title, buttons);
        controls.setPadding(new Insets(0, 10, 0, 0));

        root.setLeft(controls);
        root.setCenter(scheduleTable);
    }

    private void setupTable() {
        TableColumn<ExamSession, String> courseCol = new TableColumn<>("Course");
        courseCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getExam().getCourse().getCourseName()));

        TableColumn<ExamSession, String> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRoom().getRoomName()));

        TableColumn<ExamSession, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getTimeSlot().getDate()));

        TableColumn<ExamSession, LocalTime> startCol = new TableColumn<>("Start Time");
        startCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getTimeSlot().getStartTime()));

        TableColumn<ExamSession, LocalTime> endCol = new TableColumn<>("End Time");
        endCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getTimeSlot().getEndTime()));

        TableColumn<ExamSession, Number> enrolledCol = new TableColumn<>("Students");
        enrolledCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getAssignedStudents().size()));
        
        scheduleTable.getColumns().addAll(courseCol, roomCol, dateCol, startCol, endCol, enrolledCol);
        scheduleTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    public Parent getView() {
        return root;
    }
}
