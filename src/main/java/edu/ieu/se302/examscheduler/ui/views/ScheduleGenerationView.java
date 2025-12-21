package edu.ieu.se302.examscheduler.ui.views;

import com.examscheduler.entity.*;
import com.examscheduler.scheduler.Scheduler;
import edu.ieu.se302.examscheduler.ui.util.TimeSlotGenerator;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import edu.ieu.se302.examscheduler.ui.controllers.ScheduleController;
import javafx.util.Pair;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

        Button exportCsvBtn = new Button("Generate & Export CSV");
        exportCsvBtn.setOnAction(e -> {
            Schedule schedule = generateSchedule();
            if (schedule != null) {
                exportScheduleToCsv();
            }
        });

        Button bulkGenerateSlotsBtn = new Button("Bulk Generate Slots");
        bulkGenerateSlotsBtn.setOnAction(e -> showBulkGenerateDialog());

        maxExamsPerDaySpinner.setEditable(true);
        maxExamsPerDaySpinner.setId("maxExamsPerDaySpinner");
        HBox buttons = new HBox(8, generateBtn, exportCsvBtn, bulkGenerateSlotsBtn);
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
                exams,
                scheduleSessions,
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
    
    private void showBulkGenerateDialog() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Bulk Time Slot Generation");
        dialog.setHeaderText("Enter parameters to generate time slots automatically.");

        ButtonType generateButtonType = new ButtonType("Generate", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(generateButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        DatePicker startDatePicker = new DatePicker(LocalDate.now());
        DatePicker endDatePicker = new DatePicker(LocalDate.now().plusDays(7));
        Spinner<Integer> dayStartHour = new Spinner<>(0, 23, 9);
        Spinner<Integer> dayEndHour = new Spinner<>(0, 23, 17);
        Spinner<Integer> examDurationSpinner = new Spinner<>(30, 240, 120);
        Spinner<Integer> bufferSpinner = new Spinner<>(0, 120, 15);

        grid.add(new Label("Start Date:"), 0, 0);
        grid.add(startDatePicker, 1, 0);
        grid.add(new Label("End Date:"), 0, 1);
        grid.add(endDatePicker, 1, 1);
        grid.add(new Label("Day Start Hour:"), 0, 2);
        grid.add(dayStartHour, 1, 2);
        grid.add(new Label("Day End Hour:"), 0, 3);
        grid.add(dayEndHour, 1, 3);
        grid.add(new Label("Exam Duration (mins):"), 0, 4);
        grid.add(examDurationSpinner, 1, 4);
        grid.add(new Label("Buffer (mins):"), 0, 5);
        grid.add(bufferSpinner, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == generateButtonType) {
                return new Pair<>("generate", "true");
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(pair -> {
            LocalDate start = startDatePicker.getValue();
            LocalDate end = endDatePicker.getValue();
            LocalTime dayStart = LocalTime.of(dayStartHour.getValue(), 0);
            LocalTime dayEnd = LocalTime.of(dayEndHour.getValue(), 0);
            int duration = examDurationSpinner.getValue();
            int buffer = bufferSpinner.getValue();

            if (start.isAfter(end)) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Input Error");
                alert.setHeaderText(null);
                alert.setContentText("Start date must be before or the same as the end date.");
                alert.showAndWait();
                return;
            }

            List<TimeSlot> generated = TimeSlotGenerator.generateBulkTimeSlots(start, end, dayStart, dayEnd, duration, buffer);

            if (generated.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Generation Warning");
                alert.setHeaderText(null);
                alert.setContentText("Generated 0 time slots. Please check input parameters (e.g., duration vs. day start/end).");
                alert.showAndWait();
                return;
            }
            
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirm Generation");
            confirmation.setHeaderText("Generated " + generated.size() + " time slots.");
            confirmation.setContentText("Do you want to clear existing time slots before adding the new ones?");
            ButtonType clearAndAdd = new ButtonType("Clear and Add");
            ButtonType add = new ButtonType("Add to Existing");
            confirmation.getButtonTypes().setAll(clearAndAdd, add, ButtonType.CANCEL);

            Optional<ButtonType> choice = confirmation.showAndWait();
            boolean slotsAdded = false;
            if (choice.isPresent() && choice.get() == clearAndAdd) {
                timeSlots.setAll(generated);
                slotsAdded = true;
            } else if (choice.isPresent() && choice.get() == add) {
                timeSlots.addAll(generated);
                slotsAdded = true;
            }

            if (slotsAdded) {
                long days = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Generation Complete");
                info.setHeaderText(null);
                info.setContentText("Successfully generated " + generated.size() + " time slots across " + days + " days.");
                info.showAndWait();
            }
        });
    }

    private void setupTable() {
        TableColumn<ExamSession, String> courseCol = new TableColumn<>("Course");
        courseCol.setCellValueFactory(cellData -> {
            ExamSession session = cellData.getValue();
            Exam exam = session != null ? session.getExam() : null;
            Course course = exam != null ? exam.getCourse() : null;
            String code = course != null ? course.getCourseCode() : null;
            return new SimpleStringProperty(code != null && !code.isBlank() ? code : "N/A");
        });

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

        TableColumn<ExamSession, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTimeSlot().getDate()));

        TableColumn<ExamSession, LocalTime> startCol = new TableColumn<>("Start Time");
        startCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTimeSlot().getStartTime()));

        TableColumn<ExamSession, LocalTime> endCol = new TableColumn<>("End Time");
        endCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getActualEndTime()));

        TableColumn<ExamSession, Number> durationCol = new TableColumn<>("Duration (min)");
        durationCol.setCellValueFactory(cellData -> {
            ExamSession session = cellData.getValue();
            Exam exam = session != null ? session.getExam() : null;
            int duration = exam != null ? exam.getDurationMinutes() : 0;
            return new SimpleIntegerProperty(duration);
        });

        TableColumn<ExamSession, Number> enrolledCol = new TableColumn<>("Students");
        enrolledCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getAssignedStudents().size()));

        scheduleTable.getColumns().addAll(courseCol, roomCol, dateCol, startCol, endCol, durationCol, enrolledCol);
        scheduleTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        scheduleTable.setItems(filteredScheduleSessions);
        scheduleTable.setRowFactory(tv -> {
            TableRow<ExamSession> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showSessionStudents(row.getItem());
                }
            });
            return row;
        });
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
        TableColumn<ExamSession, String> infoCol = new TableColumn<>("Upcoming Exam Sessions");
        infoCol.setCellValueFactory(cellData -> new SimpleStringProperty(formatExamSession(cellData.getValue())));
        infoCol.setId("detailsExamInfoColumn");

        detailsExamTable.getColumns().setAll(infoCol);
        detailsExamTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private String formatExamSession(ExamSession session) {
        if (session == null) {
            return "N/A";
        }
        String code = "N/A";
        if (session.getExam() != null &&
            session.getExam().getCourse() != null &&
            session.getExam().getCourse().getCourseCode() != null &&
            !session.getExam().getCourse().getCourseCode().isBlank()) {
            code = session.getExam().getCourse().getCourseCode();
        }

        LocalDate date = session.getTimeSlot() != null ? session.getTimeSlot().getDate() : null;
        LocalTime time = session.getTimeSlot() != null ? session.getTimeSlot().getStartTime() : null;
        String room = session.getRoom() != null ? session.getRoom().getRoomName() : "N/A";

        String datePart = date != null ? date.toString() : "N/A";
        String timePart = time != null ? time.toString() : "N/A";

        return String.format("%s - %s - %s - %s", code, datePart, timePart, room);
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
        if (students.isEmpty() || courses.isEmpty() || rooms.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Cannot Generate Schedule");
            alert.setHeaderText(null);
            alert.setContentText("Please add students, courses, and rooms before generating a schedule.");
            alert.showAndWait();
            return null;
        }

        // Auto-generate time slots if none exist
        if (timeSlots.isEmpty()) {
            if (!autoGenerateTimeSlots()) {
                return null; // User cancelled or error occurred
            }
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

        // CRITICAL FIX: Clear all previous schedule state before regenerating
        // This prevents old ExamSession references from causing conflicts
        clearPreviousScheduleState();

        // 1. Configure and run the scheduler (exams auto-generated from courses)
        int maxExamsPerDay = maxExamsPerDaySpinner.getValue();
        Scheduler scheduler = new Scheduler(new ArrayList<>(rooms), new ArrayList<>(timeSlots), maxExamsPerDay);
        Schedule schedule = scheduler.generateSchedule(new ArrayList<>(courses), new ArrayList<>(exams));

        // 2. Display the results
        scheduleSessions.setAll(schedule.getExamSessions());

        // 3. Show violations in an alert
        List<String> violations = schedule.validate();
        List<String> notes = schedule.getSchedulingNotes();

        if (!violations.isEmpty()) {
            scheduleSessions.clear();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Schedule Generation Failed");
            alert.setHeaderText("A valid schedule could not be created due to conflicts.");

            TextArea textArea = new TextArea(String.join("\n- ", violations));
            textArea.setEditable(false);
            textArea.setWrapText(true);

            alert.getDialogPane().setContent(textArea);
            alert.showAndWait();
            return null;
        } else if (!notes.isEmpty()) {
            // Calculate scheduling recommendations
            int failedExams = notes.size();
            int totalExams = exams.size();
            int scheduledExams = totalExams - failedExams;
            int currentTimeSlots = timeSlots.size();
            int currentRooms = rooms.size();

            // Analyze failure types from notes
            int capacityFailures = 0;
            int conflictFailures = 0;
            for (String note : notes) {
                if (note.contains("INSUFFICIENT_CAPACITY")) {
                    capacityFailures++;
                } else if (note.contains("STUDENT_CONFLICT") || note.contains("MAX_EXAMS_PER_DAY")) {
                    conflictFailures++;
                }
            }

            // Calculate room capacity statistics
            int totalRoomCapacity = rooms.stream().mapToInt(Room::getCapacity).sum();
            int maxRoomCapacity = rooms.stream().mapToInt(Room::getCapacity).max().orElse(0);
            int largestExamSize = exams.stream()
                .mapToInt(e -> e.getCourse() != null ? e.getCourse().getEnrollments().size() : 0)
                .max()
                .orElse(0);

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Incomplete Schedule Generated");
            alert.setHeaderText(String.format("Only %d of %d exams were scheduled successfully.", scheduledExams, totalExams));

            StringBuilder message = new StringBuilder();
            message.append(String.format("Failed to schedule %d exams.\n\n", failedExams));

            // Diagnose primary issue
            message.append("DIAGNOSIS:\n\n");
            message.append(String.format("• Time Slots: %d available\n", currentTimeSlots));
            message.append(String.format("• Rooms: %d available (total capacity: %d)\n", currentRooms, totalRoomCapacity));
            message.append(String.format("• Largest room capacity: %d\n", maxRoomCapacity));
            message.append(String.format("• Largest exam size: %d students\n", largestExamSize));
            message.append(String.format("• Max exams/day: %d\n\n", maxExamsPerDay));

            message.append("FAILURE ANALYSIS:\n");
            if (conflictFailures > 0) {
                message.append(String.format("• %d exams failed due to TIME/STUDENT conflicts\n", conflictFailures));
            }
            if (capacityFailures > 0) {
                message.append(String.format("• %d exams failed due to INSUFFICIENT ROOM CAPACITY\n", capacityFailures));
            }
            message.append("\n");

            // Provide general recommendations
            message.append("RECOMMENDATIONS:\n\n");

            if (conflictFailures > capacityFailures && conflictFailures > 0) {
                // Primary issue: not enough time slots
                message.append("⚠ PRIMARY ISSUE: Not enough time slots or scheduling constraints\n\n");
                message.append("POSSIBLE SOLUTIONS:\n");
                message.append("• Generate more time slots using 'Bulk Generate Slots'\n");
                message.append("• Increase 'Max Exams/Day' setting to allow more concurrent exams\n");
                if (capacityFailures > 0) {
                    message.append("• Add more rooms or increase room capacities\n");
                }
            } else if (capacityFailures > 0) {
                // Primary issue: insufficient room capacity
                message.append("⚠ PRIMARY ISSUE: Insufficient room capacity\n\n");
                if (largestExamSize > maxRoomCapacity) {
                    message.append(String.format("• Your largest exam (%d students) exceeds largest room (%d capacity)\n",
                                                largestExamSize, maxRoomCapacity));
                }
                message.append("\nPOSSIBLE SOLUTIONS:\n");
                message.append("• Import or add rooms with higher capacity\n");
                message.append("• Add more rooms to the system\n");
                if (conflictFailures > 0) {
                    message.append("• Generate more time slots to spread exams across more days\n");
                }
            }

            message.append(String.format("\n%d exam(s) could not be scheduled.\n", failedExams));
            message.append("Please review the recommendations above and adjust your settings accordingly.");

            TextArea textArea = new TextArea(message.toString());
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefRowCount(20);

            alert.getDialogPane().setContent(textArea);

            // Add button to directly open slot generator
            ButtonType generateSlotsButton = new ButtonType("Generate More Slots", ButtonBar.ButtonData.OK_DONE);
            alert.getButtonTypes().setAll(generateSlotsButton, ButtonType.CANCEL);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == generateSlotsButton) {
                showBulkGenerateDialog();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Schedule Generated");
            alert.setHeaderText(null);
            alert.setContentText("Schedule generated successfully with no violations.");
            alert.showAndWait();
        }

        return schedule;
    }

    /**
     * Clears all previous schedule state to prevent conflicts when regenerating.
     * This is critical because students and exams maintain bidirectional references to ExamSessions.
     * Without this cleanup, old ExamSession objects remain in students' and exams' lists,
     * causing false conflicts during scheduling and duplicate sessions in the UI.
     */
    private void clearPreviousScheduleState() {
        // Clear all exam session assignments from all students
        for (Student student : students) {
            if (student != null) {
                // Clear the student's assigned sessions list
                // This prevents old ExamSession references from interfering with new schedule generation
                student.setAssignedSessions(new ArrayList<>());
            }
        }

        // Clear all exam sessions from all exams
        for (Exam exam : exams) {
            if (exam != null) {
                // Clear the exam's session list
                // This prevents duplicate sessions from accumulating across regenerations
                exam.setExamSessions(new ArrayList<>());
            }
        }

        // Clear the schedule sessions observable list
        // This removes old sessions from the UI
        scheduleSessions.clear();
    }

    /**
     * Automatically generates time slots when none exist.
     * Shows a dialog to get exam period and working hours from user.
     *
     * @return true if time slots were generated, false if user cancelled
     */
    private boolean autoGenerateTimeSlots() {
        // Create dialog
        Dialog<Pair<LocalDate, LocalDate>> dialog = new Dialog<>();
        dialog.setTitle("Auto-Generate Time Slots");
        dialog.setHeaderText("No time slots found. Let's create them automatically!");
        dialog.setContentText("Specify the exam period and working hours:");

        // Create form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Input fields
        DatePicker startDatePicker = new DatePicker(LocalDate.now().plusDays(7));
        DatePicker endDatePicker = new DatePicker(LocalDate.now().plusDays(14));

        Spinner<Integer> startHourSpinner = new Spinner<>(0, 23, 9);
        Spinner<Integer> startMinuteSpinner = new Spinner<>(0, 59, 0);
        Spinner<Integer> endHourSpinner = new Spinner<>(0, 23, 17);
        Spinner<Integer> endMinuteSpinner = new Spinner<>(0, 59, 0);
        Spinner<Integer> durationSpinner = new Spinner<>(30, 300, 120, 30);
        Spinner<Integer> bufferSpinner = new Spinner<>(0, 60, 15, 5);

        startHourSpinner.setEditable(true);
        startMinuteSpinner.setEditable(true);
        endHourSpinner.setEditable(true);
        endMinuteSpinner.setEditable(true);
        durationSpinner.setEditable(true);
        bufferSpinner.setEditable(true);

        // Layout
        grid.add(new Label("Exam Period:"), 0, 0);
        grid.add(new Label("Start Date:"), 0, 1);
        grid.add(startDatePicker, 1, 1);
        grid.add(new Label("End Date:"), 0, 2);
        grid.add(endDatePicker, 1, 2);

        grid.add(new Label("Working Hours:"), 0, 3);
        grid.add(new Label("Start Time:"), 0, 4);
        HBox startTimeBox = new HBox(5, startHourSpinner, new Label(":"), startMinuteSpinner);
        grid.add(startTimeBox, 1, 4);

        grid.add(new Label("End Time:"), 0, 5);
        HBox endTimeBox = new HBox(5, endHourSpinner, new Label(":"), endMinuteSpinner);
        grid.add(endTimeBox, 1, 5);

        grid.add(new Label("Exam Duration (min):"), 0, 6);
        grid.add(durationSpinner, 1, 6);

        grid.add(new Label("Buffer Time (min):"), 0, 7);
        grid.add(bufferSpinner, 1, 7);

        dialog.getDialogPane().setContent(grid);

        // Buttons
        ButtonType generateButtonType = new ButtonType("Generate", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(generateButtonType, ButtonType.CANCEL);

        // Result converter
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == generateButtonType) {
                return new Pair<>(startDatePicker.getValue(), endDatePicker.getValue());
            }
            return null;
        });

        // Show dialog and process result
        Optional<Pair<LocalDate, LocalDate>> result = dialog.showAndWait();

        if (result.isPresent() && result.get() != null) {
            LocalDate startDate = result.get().getKey();
            LocalDate endDate = result.get().getValue();

            if (startDate == null || endDate == null) {
                showError("Invalid Input", "Please select both start and end dates.");
                return false;
            }

            if (endDate.isBefore(startDate)) {
                showError("Invalid Input", "End date must be after start date.");
                return false;
            }

            LocalTime startTime = LocalTime.of(startHourSpinner.getValue(), startMinuteSpinner.getValue());
            LocalTime endTime = LocalTime.of(endHourSpinner.getValue(), endMinuteSpinner.getValue());
            int duration = durationSpinner.getValue();
            int buffer = bufferSpinner.getValue();

            // Generate time slots
            List<TimeSlot> generatedSlots = TimeSlotGenerator.generateBulkTimeSlots(
                startDate, endDate, startTime, endTime, duration, buffer
            );

            if (generatedSlots.isEmpty()) {
                showError("Generation Failed", "No time slots could be generated with the given parameters.");
                return false;
            }

            // Add to time slots list
            timeSlots.addAll(generatedSlots);

            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Time Slots Generated");
            alert.setHeaderText(null);
            alert.setContentText("Successfully generated " + generatedSlots.size() + " time slots!\n\n" +
                "Period: " + startDate + " to " + endDate + "\n" +
                "Hours: " + startTime + " - " + endTime);
            alert.showAndWait();

            return true;
        }

        return false; // User cancelled
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void exportScheduleToCsv() {
        if (scheduleTable.getItems() == null || scheduleTable.getItems().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export Schedule");
            alert.setHeaderText(null);
            alert.setContentText("There is no schedule to export. Generate a schedule first.");
            alert.showAndWait();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Schedule to CSV");
        fileChooser.setInitialFileName("schedule_" + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        java.io.File file = fileChooser.showSaveDialog(root.getScene().getWindow());
        if (file == null) {
            return; // User cancelled
        }

        try (java.io.PrintWriter writer = new java.io.PrintWriter(file, java.nio.charset.StandardCharsets.UTF_8)) {
            // Write header
            writer.println("Course,Room,Date,Start Time,End Time,Duration (min),Students");

            // Write data rows
            for (ExamSession session : scheduleTable.getItems()) {
                String course = "N/A";
                if (session.getExam() != null && session.getExam().getCourse() != null) {
                    course = session.getExam().getCourse().getCourseCode();
                    if (course == null || course.isBlank()) {
                        course = "N/A";
                    }
                }

                String room = "N/A";
                if (session.getRoom() != null) {
                    // Prefer roomName, fallback to roomId
                    String roomName = session.getRoom().getRoomName();
                    if (roomName != null && !roomName.isBlank()) {
                        room = roomName;
                    } else {
                        String roomId = session.getRoom().getRoomId();
                        if (roomId != null && !roomId.isBlank()) {
                            room = roomId;
                        }
                    }
                }

                String date = session.getTimeSlot() != null ? session.getTimeSlot().getDate().toString() : "N/A";
                String startTime = session.getTimeSlot() != null ? session.getTimeSlot().getStartTime().toString() : "N/A";
                String endTime = session.getActualEndTime() != null ? session.getActualEndTime().toString() : "N/A";
                int duration = session.getExam() != null ? session.getExam().getDurationMinutes() : 0;
                int students = session.getAssignedStudents().size();

                writer.printf("%s,%s,%s,%s,%s,%d,%d%n",
                    escapeCsv(course),
                    escapeCsv(room),
                    date,
                    startTime,
                    endTime,
                    duration,
                    students);
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export Successful");
            alert.setHeaderText(null);
            alert.setContentText("Schedule exported successfully to:\n" + file.getAbsolutePath());
            alert.showAndWait();

        } catch (java.io.IOException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export Failed");
            alert.setHeaderText(null);
            alert.setContentText("Failed to export schedule: " + ex.getMessage());
            alert.showAndWait();
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void showSessionStudents(ExamSession session) {
        if (session == null) {
            return;
        }
        List<Student> assigned = session.getAssignedStudents();
        StringBuilder sb = new StringBuilder();
        if (assigned.isEmpty()) {
            sb.append("No students assigned.");
        } else {
            for (Student s : assigned) {
                sb.append(s.getStudentId()).append(" - ").append(s.getFullName()).append("\n");
            }
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Assigned Students");
        alert.setHeaderText("Exam Session: " + session.getSessionId());
        TextArea textArea = new TextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }
}
