package com.examscheduler.gui;

import com.examscheduler.data.DataStore;
import com.examscheduler.entity.*;
import com.examscheduler.gui.dialogs.*;
import com.examscheduler.scheduler.Scheduler;
import com.examscheduler.service.ImportExportService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class MainView extends BorderPane {

    private TabPane tabPane;
    private Stage primaryStage;

    public MainView() {
        // MENU BAR
        MenuBar menuBar = new MenuBar();

        // File Menu
        Menu fileMenu = new Menu("File");

        Menu importMenu = new Menu("Import");
        MenuItem importStudents = new MenuItem("Students...");
        MenuItem importCourses = new MenuItem("Courses...");
        MenuItem importRooms = new MenuItem("Rooms...");
        MenuItem importTimeSlots = new MenuItem("Time Slots...");
        MenuItem importExams = new MenuItem("Exams...");
        MenuItem importSample = new MenuItem("Sample Data...");

        importMenu.getItems().addAll(
                importStudents, importCourses, importRooms,
                importTimeSlots, importExams, new SeparatorMenuItem(), importSample
        );

        importStudents.setOnAction(e -> ImportExportService.importEntity(primaryStage, "Students"));
        importCourses.setOnAction(e -> ImportExportService.importEntity(primaryStage, "Courses"));
        importRooms.setOnAction(e -> ImportExportService.importEntity(primaryStage, "Rooms"));
        importTimeSlots.setOnAction(e -> ImportExportService.importEntity(primaryStage, "Time Slots"));
        importExams.setOnAction(e -> ImportExportService.importEntity(primaryStage, "Exams"));
        importSample.setOnAction(e -> ImportExportService.importSampleData(primaryStage));

        MenuItem exportMenuItem = new MenuItem("Export All Data...");
        exportMenuItem.setOnAction(e -> ImportExportService.exportAllData(primaryStage));

        MenuItem exitMenu = new MenuItem("Exit");
        exitMenu.setOnAction(e -> System.exit(0));

        fileMenu.getItems().addAll(importMenu, exportMenuItem, new SeparatorMenuItem(), exitMenu);

        // Help Menu
        Menu helpMenu = new Menu("Help");
        MenuItem usageMenu = new MenuItem("How to Use");
        MenuItem aboutMenu = new MenuItem("About");

        usageMenu.setOnAction(e -> showUsageDialog());
        aboutMenu.setOnAction(e -> showAboutDialog());

        helpMenu.getItems().addAll(usageMenu, new SeparatorMenuItem(), aboutMenu);

        menuBar.getMenus().addAll(fileMenu, helpMenu);
        setTop(menuBar);

        // Ana içerik
        tabPane = new TabPane();

        tabPane.getTabs().add(createEntityTab("Students", DataStore.getStudents(), Student.class));
        tabPane.getTabs().add(createEntityTab("Courses", DataStore.getCourses(), Course.class));
        tabPane.getTabs().add(createEntityTab("Rooms", DataStore.getRooms(), Room.class));
        tabPane.getTabs().add(createEntityTab("Time Slots", DataStore.getTimeSlots(), TimeSlot.class));
        tabPane.getTabs().add(createEntityTab("Exams", DataStore.getExams(), Exam.class));
        tabPane.getTabs().add(createEntityTab("Enrollments", DataStore.getEnrollments(), Enrollment.class));

        // Schedule Tab
        Tab scheduleTab = new Tab("Schedule");
        scheduleTab.setClosable(false);

        TableView<ExamSession> scheduleTable = new TableView<>();

        TableColumn<ExamSession, String> courseCol = new TableColumn<>("Course");
        courseCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getExam().getCourse().getCourseName()));

        TableColumn<ExamSession, String> examNameCol = new TableColumn<>("Exam");
        examNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getExam().getName()));

        TableColumn<ExamSession, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTimeSlot().getDate().toString()));

        TableColumn<ExamSession, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getTimeSlot().getStartTime() + " - " + data.getValue().getTimeSlot().getEndTime()));

        TableColumn<ExamSession, String> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRoom().getRoomName()));

        TableColumn<ExamSession, String> studentsCol = new TableColumn<>("Students");
        studentsCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getAssignedStudents().size() + "/" + data.getValue().getMaxCapacity()));

        scheduleTable.getColumns().addAll(courseCol, examNameCol, dateCol, timeCol, roomCol, studentsCol);

        Button generateButton = new Button("Generate Schedule");
        generateButton.setStyle("-fx-font-size: 16px; -fx-padding: 10 20; -fx-background-color: #0078d7; -fx-text-fill: white;");
        generateButton.setOnAction(e -> {
            if (DataStore.getExams().isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Please create exams first!").show();
                return;
            }

            Scheduler scheduler = new Scheduler();
            scheduler.setAvailableRooms(DataStore.getRooms());
            scheduler.setAvailableTimeSlots(DataStore.getTimeSlots());
            scheduler.setMaxExamsPerDay(2);

            Schedule generatedSchedule = scheduler.generateSchedule(DataStore.getCourses(), DataStore.getExams());

            scheduleTable.getItems().clear();
            scheduleTable.getItems().addAll(generatedSchedule.getExamSessions());

            // Validation artık Scheduler'da yapılıyor
            List<String> violations = scheduler.validateSchedule(generatedSchedule);

            if (!violations.isEmpty()) {
                StringBuilder msg = new StringBuilder("Conflicts found (" + violations.size() + "):\n\n");
                for (String v : violations) {
                    msg.append("• ").append(v).append("\n");
                }
                new Alert(Alert.AlertType.WARNING, msg.toString(), ButtonType.OK).show();
            } else {
                new Alert(Alert.AlertType.INFORMATION, "Schedule generated successfully! No conflicts.", ButtonType.OK).show();
            }
        });

        VBox scheduleContent = new VBox(20, generateButton, scheduleTable);
        scheduleContent.setStyle("-fx-padding: 20;");
        scheduleTab.setContent(scheduleContent);

        tabPane.getTabs().add(scheduleTab);

        setCenter(tabPane);

        // === OTOMATİK TEST VERİLERİ (Teslimden önce bu bloğu yorum satırına al veya sil!) ===
        if (DataStore.getStudents().isEmpty()) {
            // Rooms - int roomId (Room artık int alıyor)
            DataStore.getRooms().addAll(List.of(
                    new Room(101, "Amfi A", 120),
                    new Room(102, "Amfi B", 80),
                    new Room(103, "Sınıf C101", 50),
                    new Room(104, "Lab 201", 30)
            ));

            // Time Slots
            DataStore.getTimeSlots().addAll(List.of(
                    new TimeSlot(LocalDate.of(2025, 12, 20), LocalTime.of(9, 0), LocalTime.of(11, 0)),
                    new TimeSlot(LocalDate.of(2025, 12, 20), LocalTime.of(13, 0), LocalTime.of(15, 0)),
                    new TimeSlot(LocalDate.of(2025, 12, 21), LocalTime.of(9, 0), LocalTime.of(11, 0)),
                    new TimeSlot(LocalDate.of(2025, 12, 21), LocalTime.of(13, 0), LocalTime.of(15, 0)),
                    new TimeSlot(LocalDate.of(2025, 12, 22), LocalTime.of(9, 0), LocalTime.of(11, 0))
            ));

            // Students
            DataStore.getStudents().addAll(List.of(
                    new Student(1001, "Ahmet", "Yılmaz", "ahmet@ieu.edu.tr"),
                    new Student(1002, "Ayşe", "Kaya", "ayse@ieu.edu.tr"),
                    new Student(1003, "Mehmet", "Özdemir", "mehmet@ieu.edu.tr"),
                    new Student(1004, "Fatma", "Çelik", "fatma@ieu.edu.tr"),
                    new Student(1005, "Ali", "Demir", "ali@ieu.edu.tr"),
                    new Student(1006, "Zeynep", "Şahin", "zeynep@ieu.edu.tr"),
                    new Student(1007, "Emre", "Doğan", "emre@ieu.edu.tr"),
                    new Student(1008, "Elif", "Arslan", "elif@ieu.edu.tr"),
                    new Student(1009, "Can", "Koç", "can@ieu.edu.tr"),
                    new Student(1010, "Deniz", "Yıldız", "deniz@ieu.edu.tr")
            ));

            // Courses
            Course cs101 = new Course(501, "CS101", "Introduction to Programming", 4);
            Course math101 = new Course(502, "MATH101", "Calculus I", 4);
            Course se302 = new Course(503, "SE302", "Software Engineering", 4);
            Course eng101 = new Course(504, "ENG101", "Academic English", 3);
            Course phys101 = new Course(505, "PHYS101", "Physics I", 4);

            DataStore.getCourses().addAll(List.of(cs101, math101, se302, eng101, phys101));

            // Enrollments
            Enrollment e1 = new Enrollment(1, DataStore.getStudents().get(0), cs101);
            Enrollment e2 = new Enrollment(2, DataStore.getStudents().get(0), math101);
            Enrollment e3 = new Enrollment(3, DataStore.getStudents().get(1), cs101);
            Enrollment e4 = new Enrollment(4, DataStore.getStudents().get(2), cs101);
            Enrollment e5 = new Enrollment(5, DataStore.getStudents().get(3), math101);
            Enrollment e6 = new Enrollment(6, DataStore.getStudents().get(4), se302);
            Enrollment e7 = new Enrollment(7, DataStore.getStudents().get(5), eng101);
            Enrollment e8 = new Enrollment(8, DataStore.getStudents().get(6), phys101);
            Enrollment e9 = new Enrollment(9, DataStore.getStudents().get(7), cs101);
            Enrollment e10 = new Enrollment(10, DataStore.getStudents().get(8), se302);

            DataStore.getEnrollments().addAll(List.of(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10));

            cs101.getEnrollments().addAll(List.of(e1, e3, e4, e9));
            math101.getEnrollments().addAll(List.of(e2, e5));
            se302.getEnrollments().addAll(List.of(e6, e10));
            eng101.getEnrollments().add(e7);
            phys101.getEnrollments().add(e8);

            // Exams
            DataStore.getExams().addAll(List.of(
                    new Exam(1, cs101, "Final Exam", 120),
                    new Exam(2, math101, "Final Exam", 120),
                    new Exam(3, se302, "Final Exam", 150),
                    new Exam(4, eng101, "Final Exam", 90),
                    new Exam(5, phys101, "Final Exam", 120)
            ));

            System.out.println("Otomatik test verileri yüklendi! Schedule sekmesinde 'Generate Schedule' butonuna basarak test edebilirsin.");
        }
        // === TEST VERİLERİ BİTİŞ ===


    }

    public void setStage(Stage stage) {
        this.primaryStage = stage;
    }

    public void refreshTables() {
        refreshAllTables();
    }

    private void refreshAllTables() {
        tabPane.getTabs().forEach(tab -> {
            if (tab.getContent() instanceof VBox vBox) {
                vBox.getChildren().stream()
                        .filter(node -> node instanceof TableView)
                        .map(node -> (TableView<?>) node)
                        .forEach(table -> {
                            table.refresh();
                            table.getItems().setAll(table.getItems());
                        });
            }
        });
    }

    // ====================== HELP DIALOGS ======================

    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Exam Scheduling System");
        alert.setContentText(
                "Izmir University of Economics\n" +
                        "Faculty of Computer Engineering - SE 302\n\n" +
                        "Team 13\n" +
                        "• Alperen Şen - 20230602061\n" +
                        "• Barış Tuna - 20220602073\n" +
                        "• Mehmet Aydoğmuş - 20230602101\n\n" +
                        "Version 1.0 • December 2025"
        );
        alert.showAndWait();
    }

    private void showUsageDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("How to Use");
        alert.setHeaderText("Exam Scheduling System - User Guide");
        alert.setContentText(
                "1. Enter data for Students, Courses, Rooms, Time Slots, and Exams.\n\n" +
                        "2. Register students to courses from the Enrollments tab.\n\n" +
                        "3. Click the 'Generate Schedule' button in the Schedule tab.\n\n" +
                        "4. Review the generated schedule. You will be warned if there are any conflicts.\n\n" +
                        "• File → Export All Data: Save all data as CSV files.\n" +
                        "• File → Import: Load previously saved data from CSV files or sample data.\n\n" +
                        "Note: Double-click on any row in the tables to edit the entry."
        );
        alert.showAndWait();
    }

    // ====================== TAB VE DIALOG METODLARI ======================

    private <T> Tab createEntityTab(String title, java.util.List<T> list, Class<T> type) {
        Tab tab = new Tab(title);
        tab.setClosable(false);

        TableView<T> table = new TableView<>();
        // Doğrudan DataStore'daki ObservableList'e bağla
        table.setItems((ObservableList<T>) list);

        // Sütunlar
        if (type == Student.class) {
            addColumn(table, "ID", s -> String.valueOf(((Student) s).getStudentId()));
            addColumn(table, "Full Name", s -> ((Student) s).getFullName());
            addColumn(table, "Email", s -> ((Student) s).getEmail());
            addColumn(table, "Enrollments", s -> String.valueOf(((Student) s).getEnrollments().size()));
        } else if (type == Course.class) {
            addColumn(table, "ID", c -> String.valueOf(((Course) c).getCourseId()));
            addColumn(table, "Code", c -> ((Course) c).getCourseCode());
            addColumn(table, "Name", c -> ((Course) c).getCourseName());
            addColumn(table, "Credits", c -> String.valueOf(((Course) c).getCredits()));
            addColumn(table, "Students", c -> String.valueOf(((Course) c).getEnrollmentCount()));
        } else if (type == Room.class) {
            addColumn(table, "ID", r -> String.valueOf(((Room) r).getRoomId()));
            addColumn(table, "Name", r -> ((Room) r).getRoomName());
            addColumn(table, "Capacity", r -> String.valueOf(((Room) r).getCapacity()));
        } else if (type == TimeSlot.class) {
            addColumn(table, "Date", t -> ((TimeSlot) t).getDate().toString());
            addColumn(table, "Start", t -> ((TimeSlot) t).getStartTime().toString());
            addColumn(table, "End", t -> ((TimeSlot) t).getEndTime().toString());
        } else if (type == Exam.class) {
            addColumn(table, "ID", e -> String.valueOf(((Exam) e).getExamId()));
            addColumn(table, "Course", e -> ((Exam) e).getCourse().getCourseName());
            addColumn(table, "Name", e -> ((Exam) e).getName());
            addColumn(table, "Duration (min)", e -> String.valueOf(((Exam) e).getDurationMinutes()));
        } else if (type == Enrollment.class) {
            addColumn(table, "Student", en -> ((Enrollment) en).getStudent().getFullName());
            addColumn(table, "Course", en -> ((Enrollment) en).getCourse().getCourseName());
        }

        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        table.setRowFactory(tv -> {
            TableRow<T> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    openDialog(type, row.getItem(), table);
                }
            });
            return row;
        });

        // Butonlar
        Button add = new Button("Add");
        Button edit = new Button("Edit");
        Button delete = new Button("Delete");

        add.setOnAction(e -> openDialog(type, null, table));
        edit.setOnAction(e -> {
            T selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                openDialog(type, selected, table);
            } else {
                new Alert(Alert.AlertType.WARNING, "Please select an item to edit.").show();
            }
        });
        delete.setOnAction(e -> {
            T selected = table.getSelectionModel().getSelectedItem();
            if (selected != null && confirm("Are you sure you want to delete this item?")) {
                list.remove(selected);
                table.refresh();
            } else if (selected == null) {
                new Alert(Alert.AlertType.WARNING, "Please select an item to delete.").show();
            }
        });

        // buttonsBox'ı butonlar tanımlandıktan sonra oluştur
        VBox buttonsBox = new VBox(5, add, edit, delete);

        // İçerik
        VBox content = new VBox(10, table, buttonsBox);
        content.setStyle("-fx-padding: 15;");
        tab.setContent(content);
        return tab;
    }

    private <T> void addColumn(TableView<T> table, String title, java.util.function.Function<T, String> func) {
        TableColumn<T, String> col = new TableColumn<>(title);
        col.setCellValueFactory(data -> new SimpleStringProperty(func.apply(data.getValue())));
        col.setMinWidth(120);
        table.getColumns().add(col);
    }

    private <T> void openDialog(Class<T> type, T item, TableView<T> table) {
        Stage dialog = null;
        if (type == Student.class) dialog = new StudentDialog((Student) item);
        else if (type == Course.class) dialog = new CourseDialog((Course) item);
        else if (type == Room.class) dialog = new RoomDialog((Room) item);
        else if (type == TimeSlot.class) dialog = new TimeSlotDialog((TimeSlot) item);
        else if (type == Exam.class) dialog = new ExamDialog((Exam) item);

        if (dialog != null) {
            dialog.showAndWait();
            table.refresh();
            if (item == null && !table.getItems().isEmpty()) {
                table.getSelectionModel().selectLast();
                table.scrollTo(table.getItems().size() - 1);
            }
        }
    }

    private boolean confirm(String msg) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        return alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }
}