package edu.ieu.se302.examscheduler.ui;

import edu.ieu.se302.examscheduler.ui.views.CourseManagementView;
import edu.ieu.se302.examscheduler.ui.views.EnrollmentManagementView;
import edu.ieu.se302.examscheduler.ui.views.ExamManagementView;
import edu.ieu.se302.examscheduler.ui.views.RoomManagementView;
import edu.ieu.se302.examscheduler.ui.views.ScheduleGenerationView;
import edu.ieu.se302.examscheduler.ui.views.StudentManagementView;
import edu.ieu.se302.examscheduler.ui.views.TimeSlotManagementView;
import edu.ieu.se302.examscheduler.ui.util.CsvImportService;
import edu.ieu.se302.examscheduler.ui.util.I18n;
import com.examscheduler.entity.Enrollment;
import com.examscheduler.entity.Exam;
import com.examscheduler.entity.ExamSession;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * Main window layout (menu + panels + view container) as required in Meeting Report 4, Task 3.
 * Navigation swaps the center content between placeholder views.
 */
public class MainWindow {

    private final BorderPane root = new BorderPane();
    private final StackPane contentPane = new StackPane();
    private final ObservableList<Enrollment> enrollments = FXCollections.observableArrayList();
    private final ObservableList<Exam> exams = FXCollections.observableArrayList();
    private final ObservableList<ExamSession> scheduleSessions = FXCollections.observableArrayList();
    private Path lastStudentsPath;
    private Path lastCoursesPath;
    private Path lastRoomsPath;
    private Path lastTimeSlotsPath;
    private Path lastExamsPath;
    private MenuItem reimportItem;

    private final StudentManagementView studentView = new StudentManagementView(enrollments);
    private final CourseManagementView courseView = new CourseManagementView(scheduleSessions, enrollments);
    private final RoomManagementView roomView = new RoomManagementView(scheduleSessions);
    private final TimeSlotManagementView timeSlotView = new TimeSlotManagementView();
    private final ExamManagementView examView = new ExamManagementView(exams, courseView.getCourses());
    private final EnrollmentManagementView enrollmentView = new EnrollmentManagementView(enrollments, studentView.getStudents(), courseView.getCourses());
    private final ScheduleGenerationView scheduleView;

    public MainWindow() {
        scheduleView = new ScheduleGenerationView(
                studentView.getStudents(),
                courseView.getCourses(),
                roomView.getRooms(),
                timeSlotView.getTimeSlots(),
                exams,
                scheduleSessions
        );
        root.setTop(buildMenuBar());
        root.setCenter(contentPane);
        root.setBottom(buildStatusBar());
        BorderPane.setMargin(contentPane, new Insets(10));

        // Default view
        show(studentView.getView());
    }

    public Parent getRoot() {
        return root;
    }

    private MenuBar buildMenuBar() {
        MenuBar menuBar = new MenuBar();

        Menu file = new Menu(I18n.get("menu.file"));
        MenuItem importItem = new MenuItem(I18n.get("menu.file.import"));
        importItem.setOnAction(e -> importDataFiles());
        reimportItem = new MenuItem(I18n.get("menu.file.reimport"));
        reimportItem.setDisable(true);
        reimportItem.setOnAction(e -> reimportLastFiles());
        MenuItem exit = new MenuItem(I18n.get("menu.file.exit"));
        exit.setOnAction(e -> root.getScene().getWindow().hide());
        file.getItems().addAll(importItem, reimportItem, exit);

        Menu data = new Menu(I18n.get("menu.data"));
        MenuItem students = new MenuItem(I18n.get("menu.data.students"));
        students.setOnAction(e -> show(studentView.getView()));
        MenuItem courses = new MenuItem(I18n.get("menu.data.courses"));
        courses.setOnAction(e -> show(courseView.getView()));
        MenuItem rooms = new MenuItem(I18n.get("menu.data.rooms"));
        rooms.setOnAction(e -> show(roomView.getView()));
        MenuItem timeSlots = new MenuItem(I18n.get("menu.data.timeslots"));
        timeSlots.setOnAction(e -> show(timeSlotView.getView()));
        MenuItem examsItem = new MenuItem(I18n.get("menu.data.exams"));
        examsItem.setOnAction(e -> show(examView.getView()));
        MenuItem enrollmentsItem = new MenuItem(I18n.get("menu.data.enrollments"));
        enrollmentsItem.setOnAction(e -> show(enrollmentView.getView()));
        data.getItems().addAll(students, courses, rooms, timeSlots, examsItem, enrollmentsItem);

        Menu schedule = new Menu(I18n.get("menu.schedule"));
        MenuItem generate = new MenuItem(I18n.get("menu.schedule.generate"));
        generate.setOnAction(e -> show(scheduleView.getView()));
        schedule.getItems().add(generate);

        Menu help = new Menu(I18n.get("menu.help"));
        MenuItem about = new MenuItem(I18n.get("menu.help.about"));
        about.setOnAction(e -> showAboutDialog());
        MenuItem howTo = new MenuItem(I18n.get("menu.help.howto"));
        howTo.setOnAction(e -> showHelpDialog());
        help.getItems().addAll(howTo, about);

        menuBar.getMenus().addAll(file, data, schedule, help);
        return menuBar;
    }

    private void importDataFiles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        fileChooser.setTitle("Select Students CSV");
        File studentsFile = fileChooser.showOpenDialog(root.getScene().getWindow());
        if (studentsFile == null) {
            return;
        }

        fileChooser.setTitle("Select Courses CSV");
        File coursesFile = fileChooser.showOpenDialog(root.getScene().getWindow());
        if (coursesFile == null) {
            return;
        }

        fileChooser.setTitle("Select Rooms CSV");
        File roomsFile = fileChooser.showOpenDialog(root.getScene().getWindow());
        if (roomsFile == null) {
            return;
        }

        fileChooser.setTitle("Select Time Slots CSV");
        File timeSlotsFile = fileChooser.showOpenDialog(root.getScene().getWindow());
        if (timeSlotsFile == null) {
            return;
        }

        fileChooser.setTitle("Select Exams CSV");
        File examsFile = fileChooser.showOpenDialog(root.getScene().getWindow());
        if (examsFile == null) {
            return;
        }

        try {
            importFromPaths(studentsFile.toPath(), coursesFile.toPath(), roomsFile.toPath(), timeSlotsFile.toPath(), examsFile.toPath());
        } catch (IllegalArgumentException ex) {
            showError(I18n.get("dialog.error.import.title"), ex.getMessage());
        } catch (Exception ex) {
            showError(I18n.get("dialog.error.import.title"), I18n.get("dialog.error.import.generic"));
        }
    }

    private void reimportLastFiles() {
        if (lastStudentsPath == null || lastCoursesPath == null || lastRoomsPath == null ||
            lastTimeSlotsPath == null || lastExamsPath == null) {
            showError(I18n.get("dialog.error.reimport.title"), I18n.get("dialog.error.reimport.missing"));
            return;
        }
        try {
            importFromPaths(lastStudentsPath, lastCoursesPath, lastRoomsPath, lastTimeSlotsPath, lastExamsPath);
        } catch (IllegalArgumentException ex) {
            showError(I18n.get("dialog.error.import.title"), ex.getMessage());
        } catch (Exception ex) {
            showError(I18n.get("dialog.error.import.title"), I18n.get("dialog.error.reimport.generic"));
        }
    }

    private void importFromPaths(Path studentsPath, Path coursesPath, Path roomsPath, Path timeSlotsPath, Path examsPath) throws Exception {
        List<com.examscheduler.entity.Student> importedStudents = CsvImportService.importStudents(studentsPath);
        List<com.examscheduler.entity.Course> importedCourses = CsvImportService.importCourses(coursesPath);
        List<com.examscheduler.entity.Room> importedRooms = CsvImportService.importRooms(roomsPath);
        List<com.examscheduler.entity.TimeSlot> importedTimeSlots = CsvImportService.importTimeSlots(timeSlotsPath);
        List<com.examscheduler.entity.Exam> importedExams = CsvImportService.importExams(examsPath, importedCourses);

        studentView.getStudents().setAll(importedStudents);
        courseView.getCourses().setAll(importedCourses);
        roomView.getRooms().setAll(importedRooms);
        timeSlotView.getTimeSlots().setAll(importedTimeSlots);
        exams.setAll(importedExams);
        enrollments.clear();

        lastStudentsPath = studentsPath;
        lastCoursesPath = coursesPath;
        lastRoomsPath = roomsPath;
        lastTimeSlotsPath = timeSlotsPath;
        lastExamsPath = examsPath;
        reimportItem.setDisable(false);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private HBox buildStatusBar() {
        Label statusLabel = new Label();
        statusLabel.setId("statusStudentCountLabel");
        statusLabel.textProperty().bind(
                Bindings.createStringBinding(
                        () -> I18n.format("status.totalStudents", studentView.getStudents().size()),
                        studentView.getStudents()
                )
        );

        HBox statusBar = new HBox(statusLabel);
        statusBar.setId("statusBar");
        statusBar.setPadding(new Insets(6, 10, 6, 10));
        statusBar.setStyle("-fx-border-color: #d0d0d0; -fx-border-width: 1 0 0 0;");
        return statusBar;
    }

    private void show(Parent view) {
        contentPane.getChildren().setAll(view);
    }

    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(I18n.get("dialog.about.title"));
        alert.setHeaderText(I18n.get("dialog.about.header"));
        alert.setContentText(I18n.get("dialog.about.content"));
        alert.showAndWait();
    }

    private void showHelpDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(I18n.get("dialog.help.title"));
        alert.setHeaderText(I18n.get("dialog.help.header"));
        alert.setContentText(I18n.get("dialog.help.content"));
        alert.showAndWait();
    }
}
