package com.examscheduler.service;

import com.examscheduler.data.DataStore;
import com.examscheduler.entity.*;
import com.examscheduler.gui.MainApp;  // refreshTables() için gerekli
import com.examscheduler.util.CsvService;
import javafx.application.Platform;     // <<< JavaFX thread zorlama için EKLENDİ
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ImportExportService {

    // ====================== EXPORT ======================
    public static void exportAllData(Stage ownerStage) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Folder to Export CSV Files");
        File folder = chooser.showDialog(ownerStage);

        if (folder == null) return;

        try {
            exportToCsv(new File(folder, "students.csv"), DataStore.getStudents(), ImportExportService::studentToCsv);
            exportToCsv(new File(folder, "courses.csv"), DataStore.getCourses(), ImportExportService::courseToCsv);
            exportToCsv(new File(folder, "rooms.csv"), DataStore.getRooms(), ImportExportService::roomToCsv);
            exportToCsv(new File(folder, "timeslots.csv"), DataStore.getTimeSlots(), ImportExportService::timeSlotToCsv);
            exportToCsv(new File(folder, "exams.csv"), DataStore.getExams(), ImportExportService::examToCsv);
            exportToCsv(new File(folder, "enrollments.csv"), DataStore.getEnrollments(), ImportExportService::enrollmentToCsv);

            showAlert(Alert.AlertType.INFORMATION, "Export Successful",
                    "All data exported successfully to:\n" + folder.getAbsolutePath());
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Export Failed", "Error: " + ex.getMessage());
        }
    }

    // ====================== STANDARD IMPORT (CSV) ======================
    public static void importEntity(Stage ownerStage, String entityType) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select CSV File - " + entityType);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = chooser.showOpenDialog(ownerStage);

        if (file == null) return;

        new Thread(() -> {
            try {
                Path path = file.toPath();
                final List<?> importedData;  // final kopya

                switch (entityType) {
                    case "Students" -> importedData = CsvService.importStudents(path);
                    case "Courses" -> importedData = CsvService.importCourses(path);
                    case "Rooms" -> importedData = CsvService.importRooms(path);
                    case "Time Slots" -> importedData = CsvService.importTimeSlots(path);
                    case "Exams" -> importedData = CsvService.importExams(path, DataStore.getCourses());
                    default -> {
                        Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Import Failed", "Unsupported entity."));
                        return;
                    }
                }

                // final kopyayı lambda içinde kullan
                Platform.runLater(() -> {
                    switch (entityType) {
                        case "Students" -> {
                            DataStore.getStudents().clear();
                            DataStore.getStudents().addAll((List<Student>) importedData);
                        }
                        case "Courses" -> {
                            DataStore.getCourses().clear();
                            DataStore.getCourses().addAll((List<Course>) importedData);
                        }
                        case "Rooms" -> {
                            DataStore.getRooms().clear();
                            DataStore.getRooms().addAll((List<Room>) importedData);
                        }
                        case "Time Slots" -> {
                            DataStore.getTimeSlots().clear();
                            DataStore.getTimeSlots().addAll((List<TimeSlot>) importedData);
                        }
                        case "Exams" -> {
                            DataStore.getExams().clear();
                            DataStore.getExams().addAll((List<Exam>) importedData);
                        }
                    }

                    MainApp.refreshTables();
                    showAlert(Alert.AlertType.INFORMATION, "Import Successful", entityType + " imported successfully.");
                });

            } catch (Exception ex) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Import Failed", "Error: " + ex.getMessage()));
            }
        }).start();
    }

    // ====================== SAMPLE DATA IMPORT ======================
    public static void importSampleData(Stage ownerStage) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Folder Containing Sample Data Files");
        File folder = chooser.showDialog(ownerStage);

        if (folder == null) return;

        try {
            Path basePath = folder.toPath();

            Path studentsPath = basePath.resolve("sampleData_AllStudents.csv");
            Path coursesPath = basePath.resolve("sampleData_AllCourses.csv");
            Path roomsPath = basePath.resolve("sampleData_AllClassroomsAndTheirCapacities.csv");
            Path attendancePath = basePath.resolve("sampleData_AllAttendanceLists.csv");

            if (Files.exists(studentsPath)) importSampleStudents(studentsPath);
            if (Files.exists(coursesPath)) importSampleCourses(coursesPath);
            if (Files.exists(roomsPath)) importSampleRooms(roomsPath);
            if (Files.exists(attendancePath)) importSampleEnrollments(attendancePath);

            showAlert(Alert.AlertType.INFORMATION, "Import Successful", "Sample data imported successfully.");

            // Sample import sonrası da JavaFX thread'inde yenile
            Platform.runLater(() -> MainApp.refreshTables());

        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Import Failed", "Error: " + ex.getMessage());
        }
    }

    // ====================== SAMPLE IMPORT HELPERS ======================
    private static void importSampleStudents(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path);
        List<Student> tempList = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String idStr = lines.get(i).trim();
            if (idStr.isEmpty()) continue;
            try {
                int id = Integer.parseInt(idStr);
                Student s = new Student(id, "Student", idStr, idStr + "@student.ieu.edu.tr");
                tempList.add(s);
            } catch (NumberFormatException ignored) {}
        }
        Platform.runLater(() -> {
            DataStore.getStudents().clear();
            DataStore.getStudents().addAll(tempList);
        });
    }

    private static void importSampleCourses(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path);
        List<Course> tempList = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String code = lines.get(i).trim();
            if (code.isEmpty()) continue;
            int courseId = Math.abs(code.hashCode());
            Course c = new Course(courseId, code, "Course " + code, 4);
            tempList.add(c);
        }
        Platform.runLater(() -> {
            DataStore.getCourses().clear();
            DataStore.getCourses().addAll(tempList);
        });
    }

    private static void importSampleRooms(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path);
        List<Room> tempList = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split(";", -1);
            if (parts.length < 2) continue;
            try {
                int roomId = Integer.parseInt(parts[0].trim());
                int capacity = Integer.parseInt(parts[1].trim());
                Room r = new Room(roomId, parts[0].trim(), capacity);
                tempList.add(r);
            } catch (NumberFormatException ignored) {}
        }
        Platform.runLater(() -> {
            DataStore.getRooms().clear();
            DataStore.getRooms().addAll(tempList);
        });
    }

    private static void importSampleEnrollments(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path);
        List<Enrollment> tempList = new ArrayList<>();
        String currentCourseCode = null;
        int counter = 1;

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) continue;

            if (line.startsWith("CourseCode_")) {
                currentCourseCode = line;
                continue;
            }

            if (line.startsWith("['") && currentCourseCode != null) {
                String content = line.substring(2, line.length() - 2);
                String[] stdIdParts = content.split("', '");

                final String courseCodeToUse = currentCourseCode;

                Course course = DataStore.getCourses().stream()
                        .filter(c -> c.getCourseCode().equals(courseCodeToUse))
                        .findFirst()
                        .orElse(null);

                if (course == null) continue;

                for (String part : stdIdParts) {
                    String stdIdRaw = part.trim();
                    if (stdIdRaw.startsWith("'")) stdIdRaw = stdIdRaw.substring(1);
                    if (stdIdRaw.endsWith("'")) stdIdRaw = stdIdRaw.substring(0, stdIdRaw.length() - 1);

                    try {
                        int studentId = Integer.parseInt(stdIdRaw);
                        Student student = DataStore.getStudents().stream()
                                .filter(s -> s.getStudentId() == studentId)
                                .findFirst()
                                .orElse(null);

                        if (student == null) continue;

                        Enrollment enrollment = new Enrollment(counter++, student, course);
                        tempList.add(enrollment);
                        student.addEnrollment(enrollment);
                        course.addEnrollment(enrollment);
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        Platform.runLater(() -> {
            DataStore.getEnrollments().clear();
            DataStore.getEnrollments().addAll(tempList);
        });
    }

    // ====================== UTILS ======================
    private static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private static <T> void exportToCsv(File file, List<T> list, Function<T, String> mapper) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
            for (T item : list) {
                writer.write(mapper.apply(item));
                writer.newLine();
            }
        }
    }

    // CSV mapping metodları
    private static String studentToCsv(Student s) {
        return s.getStudentId() + "," + s.getFirstName() + "," + s.getLastName() + "," + s.getEmail();
    }

    private static String courseToCsv(Course c) {
        return c.getCourseId() + "," + c.getCourseCode() + "," + c.getCourseName() + "," + c.getCredits();
    }

    private static String roomToCsv(Room r) {
        return r.getRoomId() + "," + r.getRoomName() + "," + r.getCapacity();
    }

    private static String timeSlotToCsv(TimeSlot ts) {
        return ts.getDate() + "," + ts.getStartTime() + "," + ts.getEndTime();
    }

    private static String examToCsv(Exam e) {
        return e.getExamId() + "," + e.getCourse().getCourseId() + "," + e.getName() + "," + e.getDurationMinutes();
    }

    private static String enrollmentToCsv(Enrollment en) {
        return en.getEnrollmentId() + "," + en.getStudent().getStudentId() + "," + en.getCourse().getCourseId();
    }
}