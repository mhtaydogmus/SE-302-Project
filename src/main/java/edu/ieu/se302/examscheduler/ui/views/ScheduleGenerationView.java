package edu.ieu.se302.examscheduler.ui.views;

import com.examscheduler.entity.*;
import com.examscheduler.scheduler.Scheduler;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.print.PrinterJob;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import edu.ieu.se302.examscheduler.ui.controllers.ScheduleController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ScheduleGenerationView {

    private final BorderPane root = new BorderPane();
    private final ObservableList<Student> students;
    private final ObservableList<Course> courses;
    private final ObservableList<Room> rooms;
    private final ObservableList<TimeSlot> timeSlots;
    private final ObservableList<Exam> exams;
    private final ObservableList<ExamSession> scheduleSessions;
    private final FilteredList<ExamSession> filteredScheduleSessions;
    private final TableView<ExamSession> scheduleTable = new TableView<>();
    private final ListView<Student> studentList;
    private final DatePicker scheduleDatePicker = new DatePicker();
    private final Button clearDateFilterButton = new Button("Show All Dates");
    private final Spinner<Integer> maxExamsPerDaySpinner = new Spinner<>(1, 10, 2);

    // Student Details Panel
    private VBox detailsPanel;
    private Label detailsNameLabel;
    private Label detailsIdLabel;
    private Label detailsEmailLabel;
    private ListView<String> detailsCourseList;
    private TableView<ExamSession> detailsExamTable;
    private Button editStudentButton;
    private Button deleteStudentButton;
    private Button closeDetailsButton;
    private ScheduleController scheduleController;


    public ScheduleGenerationView(ObservableList<Student> students, ObservableList<Course> courses, ObservableList<Room> rooms, ObservableList<TimeSlot> timeSlots, ObservableList<Exam> exams, ObservableList<ExamSession> scheduleSessions) {
        this.students = students;
        this.courses = courses;
        this.rooms = rooms;
        this.timeSlots = timeSlots;
        this.exams = exams;
        this.scheduleSessions = scheduleSessions;
        this.filteredScheduleSessions = new FilteredList<>(scheduleSessions, session -> true);
        this.studentList = new ListView<>(students);

        root.setPadding(new Insets(10));

        Label title = new Label("Schedule Generation");
        title.getStyleClass().add("view-title");

        setupTable();
        setupStudentDetailsPanel();

        Button generateBtn = new Button("Generate Schedule");
        generateBtn.setOnAction(e -> generateSchedule());

        Button generatePrintBtn = new Button("Generate & Print");
        generatePrintBtn.setOnAction(e -> {
            Schedule schedule = generateSchedule();
            if (schedule != null) {
                printSchedule();
            }
        });

        maxExamsPerDaySpinner.setEditable(true);
        maxExamsPerDaySpinner.setId("maxExamsPerDaySpinner");
        HBox buttons = new HBox(8, generateBtn, generatePrintBtn);
        HBox dateFilterRow = new HBox(8, new Label("Filter Date:"), scheduleDatePicker, clearDateFilterButton);
        HBox maxExamsRow = new HBox(8, new Label("Max Exams/Day:"), maxExamsPerDaySpinner);
        scheduleDatePicker.setId("scheduleDatePicker");
        clearDateFilterButton.setId("scheduleDateClearButton");
        scheduleDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> applyDateFilter(newDate));
        clearDateFilterButton.setOnAction(e -> scheduleDatePicker.setValue(null));
        Label studentsLabel = new Label("Students");
        studentList.setId("scheduleStudentList");
        studentList.setPrefHeight(200);
        VBox.setVgrow(studentList, Priority.ALWAYS);

        VBox controls = new VBox(10, title, buttons, maxExamsRow, dateFilterRow, studentsLabel, studentList);
        controls.setPadding(new Insets(0, 10, 0, 0));

        root.setLeft(controls);
        root.setCenter(scheduleTable);
        scheduleController = new ScheduleController(
                students,
                studentList,
                detailsPanel,
                detailsNameLabel,
                detailsIdLabel,
                detailsEmailLabel,
                detailsCourseList,
                detailsExamTable,
                editStudentButton,
                deleteStudentButton,
                closeDetailsButton
        );
    }

    private void setupTable() {
        TableColumn<ExamSession, String> courseCol = new TableColumn<>("Course");
        courseCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getExam().getCourse().getCourseName()));

        TableColumn<ExamSession, String> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRoom().getRoomName()));

        TableColumn<ExamSession, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTimeSlot().getDate()));

        TableColumn<ExamSession, LocalTime> startCol = new TableColumn<>("Start Time");
        startCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTimeSlot().getStartTime()));

        TableColumn<ExamSession, LocalTime> endCol = new TableColumn<>("End Time");
        endCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTimeSlot().getEndTime()));

        TableColumn<ExamSession, Number> enrolledCol = new TableColumn<>("Students");
        enrolledCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getAssignedStudents().size()));

        scheduleTable.getColumns().addAll(courseCol, roomCol, dateCol, startCol, endCol, enrolledCol);
        scheduleTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        scheduleTable.setItems(filteredScheduleSessions);
    }

    private void setupStudentDetailsPanel() {
        detailsPanel = new VBox(10);
        detailsPanel.setPadding(new Insets(10));
        detailsPanel.setStyle("-fx-border-color: lightgray; -fx-border-width: 1;");
        detailsPanel.setMinWidth(250);
        detailsPanel.setId("detailsPanel");
        detailsPanel.setVisible(false);
        detailsPanel.setManaged(false);

        Label detailsTitle = new Label("Student Details");
        detailsTitle.getStyleClass().add("view-subtitle");
        detailsTitle.setId("detailsTitleLabel");

        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(10);
        detailsGrid.setVgap(5);
        detailsGrid.setId("detailsGrid");

        Label nameLabel = new Label("Name:");
        nameLabel.setId("detailsNameTitleLabel");
        detailsNameLabel = new Label("N/A");
        detailsNameLabel.setId("detailsNameLabel");

        Label idLabel = new Label("Student ID:");
        idLabel.setId("detailsIdTitleLabel");
        detailsIdLabel = new Label("N/A");
        detailsIdLabel.setId("detailsIdLabel");

        Label emailLabel = new Label("Email:");
        emailLabel.setId("detailsEmailTitleLabel");
        detailsEmailLabel = new Label("N/A");
        detailsEmailLabel.setId("detailsEmailLabel");

        detailsGrid.addRow(0, nameLabel, detailsNameLabel);
        detailsGrid.addRow(1, idLabel, detailsIdLabel);
        detailsGrid.addRow(2, emailLabel, detailsEmailLabel);

        Label enrolledCoursesTitle = new Label("Enrolled Courses");
        enrolledCoursesTitle.setId("detailsCoursesTitleLabel");
        detailsCourseList = new ListView<>();
        detailsCourseList.setPrefHeight(150);
        detailsCourseList.setId("detailsCourseList");

        Label upcomingExamsTitle = new Label("Upcoming Exam Sessions");
        upcomingExamsTitle.setId("detailsExamsTitleLabel");
        detailsExamTable = new TableView<>();
        detailsExamTable.setPrefHeight(150);
        detailsExamTable.setId("detailsExamTable");
        setupDetailsExamTable();

        Region detailsSpacer = new Region();
        detailsSpacer.setId("detailsSpacer");
        VBox.setVgrow(detailsSpacer, Priority.ALWAYS);

        HBox actionButtons = new HBox(10,
                editStudentButton = new Button("Edit Student"),
                deleteStudentButton = new Button("Delete Student"),
                closeDetailsButton = new Button("Close")
        );
        actionButtons.setId("detailsActionButtons");
        editStudentButton.setId("detailsEditButton");
        deleteStudentButton.setId("detailsDeleteButton");
        closeDetailsButton.setId("detailsCloseButton");

        detailsPanel.getChildren().addAll(
                detailsTitle,
                detailsGrid,
                enrolledCoursesTitle,
                detailsCourseList,
                upcomingExamsTitle,
                detailsExamTable,
                detailsSpacer,
                actionButtons
        );

        root.setRight(detailsPanel);
    }

    private void setupDetailsExamTable() {
        TableColumn<ExamSession, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTimeSlot().getDate()));
        dateCol.setId("detailsExamDateColumn");

        TableColumn<ExamSession, LocalTime> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTimeSlot().getStartTime()));
        timeCol.setId("detailsExamTimeColumn");

        TableColumn<ExamSession, String> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRoom().getRoomName()));
        roomCol.setId("detailsExamRoomColumn");

        detailsExamTable.getColumns().addAll(dateCol, timeCol, roomCol);
        detailsExamTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }


    public Parent getView() {
        return root;
    }

    private void applyDateFilter(LocalDate date) {
        if (date == null) {
            filteredScheduleSessions.setPredicate(session -> true);
            return;
        }
        filteredScheduleSessions.setPredicate(session -> session.getTimeSlot() != null
                && date.equals(session.getTimeSlot().getDate()));
    }

    private Schedule generateSchedule() {
        if (students.isEmpty() || courses.isEmpty() || rooms.isEmpty() || timeSlots.isEmpty() || exams.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Cannot Generate Schedule");
            alert.setHeaderText(null);
            alert.setContentText("Please add students, courses, rooms, time slots, and exams before generating a schedule.");
            alert.showAndWait();
            return null;
        }

        boolean hasEnrollments = courses.stream().anyMatch(course -> !course.getEnrollments().isEmpty());
        if (!hasEnrollments) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Cannot Generate Schedule");
            alert.setHeaderText(null);
            alert.setContentText("Please enroll students in courses before generating a schedule.");
            alert.showAndWait();
            return null;
        }

        // 1. Configure and run the scheduler
        int maxExamsPerDay = maxExamsPerDaySpinner.getValue();
        Scheduler scheduler = new Scheduler(new ArrayList<>(rooms), new ArrayList<>(timeSlots), maxExamsPerDay);
        Schedule schedule = scheduler.generateSchedule(new ArrayList<>(courses), new ArrayList<>(exams));

        // 2. Display the results
        scheduleSessions.setAll(schedule.getExamSessions());

        // 3. Show violations in an alert
        List<String> violations = schedule.validate();
        if (!violations.isEmpty()) {
            scheduleSessions.clear();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Schedule Generation Failed");
            alert.setHeaderText("A valid schedule could not be generated.");

            TextArea textArea = new TextArea(String.join("\n- ", violations));
            textArea.setEditable(false);
            textArea.setWrapText(true);

            alert.getDialogPane().setContent(textArea);
            alert.showAndWait();
            return null;
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Schedule Generated");
            alert.setHeaderText(null);
            alert.setContentText("Schedule generated successfully with no violations.");
            alert.showAndWait();
        }

        return schedule;
    }

    private void printSchedule() {
        if (scheduleTable.getItems() == null || scheduleTable.getItems().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Print Schedule");
            alert.setHeaderText(null);
            alert.setContentText("There is no schedule to print. Generate a schedule first.");
            alert.showAndWait();
            return;
        }

        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Print Schedule");
            alert.setHeaderText(null);
            alert.setContentText("No printer is available.");
            alert.showAndWait();
            return;
        }

        boolean proceed = job.showPrintDialog(root.getScene().getWindow());
        if (!proceed) {
            return;
        }

        boolean success = job.printPage(scheduleTable);
        if (success) {
            job.endJob();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Print Schedule");
            alert.setHeaderText(null);
            alert.setContentText("Failed to print the schedule.");
            alert.showAndWait();
        }
    }
}
