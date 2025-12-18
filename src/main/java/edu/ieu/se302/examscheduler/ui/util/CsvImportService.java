package edu.ieu.se302.examscheduler.ui.util;

import com.examscheduler.entity.Course;
import com.examscheduler.entity.Exam;
import com.examscheduler.entity.Room;
import com.examscheduler.entity.Student;
import com.examscheduler.entity.TimeSlot;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CsvImportService {
    private CsvImportService() {
    }

    public static List<Student> importStudents(Path path) throws IOException {
        List<List<String>> rows = readCsv(path);
        Map<String, Integer> headers = mapHeaders(rows, path);

        requireHeader(headers, "studentid", path);
        requireHeader(headers, "firstname", path);
        requireHeader(headers, "lastname", path);
        requireHeader(headers, "email", path);
        requireHeader(headers, "gender", path);

        List<Student> students = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();

        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            if (row.isEmpty() || isBlankRow(row)) {
                continue;
            }

            String id = getField(headers, row, "studentid");
            String firstName = getField(headers, row, "firstname");
            String lastName = getField(headers, row, "lastname");
            String email = getField(headers, row, "email");
            String gender = getField(headers, row, "gender");

            if (id.isBlank()) {
                throw new IllegalArgumentException("Missing Student ID at line " + (i + 1) + " in " + path.getFileName());
            }
            if (!seenIds.add(id)) {
                throw new IllegalArgumentException("Duplicate Student ID '" + id + "' at line " + (i + 1) + " in " + path.getFileName());
            }

            students.add(new Student(id, firstName, lastName, email, gender));
        }

        return students;
    }

    public static List<Course> importCourses(Path path) throws IOException {
        List<List<String>> rows = readCsv(path);
        Map<String, Integer> headers = mapHeaders(rows, path);

        requireHeader(headers, "courseid", path);
        requireHeader(headers, "coursename", path);
        requireHeader(headers, "coursecode", path);
        requireHeader(headers, "credits", path);

        List<Course> courses = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();

        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            if (row.isEmpty() || isBlankRow(row)) {
                continue;
            }

            String id = getField(headers, row, "courseid");
            String name = getField(headers, row, "coursename");
            String code = getField(headers, row, "coursecode");
            int credits = parseInt(getField(headers, row, "credits"), "credits", i + 1, path);

            if (id.isBlank()) {
                throw new IllegalArgumentException("Missing Course ID at line " + (i + 1) + " in " + path.getFileName());
            }
            if (!seenIds.add(id)) {
                throw new IllegalArgumentException("Duplicate Course ID '" + id + "' at line " + (i + 1) + " in " + path.getFileName());
            }

            courses.add(new Course(id, name, code, credits));
        }

        return courses;
    }

    public static List<Room> importRooms(Path path) throws IOException {
        List<List<String>> rows = readCsv(path);
        Map<String, Integer> headers = mapHeaders(rows, path);

        requireHeader(headers, "roomid", path);
        requireHeader(headers, "roomname", path);
        requireHeader(headers, "capacity", path);

        List<Room> rooms = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();

        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            if (row.isEmpty() || isBlankRow(row)) {
                continue;
            }

            String id = getField(headers, row, "roomid");
            String name = getField(headers, row, "roomname");
            int capacity = parseInt(getField(headers, row, "capacity"), "capacity", i + 1, path);

            if (id.isBlank()) {
                throw new IllegalArgumentException("Missing Room ID at line " + (i + 1) + " in " + path.getFileName());
            }
            if (!seenIds.add(id)) {
                throw new IllegalArgumentException("Duplicate Room ID '" + id + "' at line " + (i + 1) + " in " + path.getFileName());
            }

            rooms.add(new Room(id, name, capacity));
        }

        return rooms;
    }

    public static List<TimeSlot> importTimeSlots(Path path) throws IOException {
        List<List<String>> rows = readCsv(path);
        Map<String, Integer> headers = mapHeaders(rows, path);

        requireHeader(headers, "date", path);
        requireHeader(headers, "starttime", path);
        requireHeader(headers, "endtime", path);

        List<TimeSlot> timeSlots = new ArrayList<>();
        Set<String> seenKeys = new HashSet<>();

        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            if (row.isEmpty() || isBlankRow(row)) {
                continue;
            }

            LocalDate date = parseDate(getField(headers, row, "date"), i + 1, path);
            LocalTime start = parseTime(getField(headers, row, "starttime"), "start time", i + 1, path);
            LocalTime end = parseTime(getField(headers, row, "endtime"), "end time", i + 1, path);

            String key = date + "|" + start + "|" + end;
            if (!seenKeys.add(key)) {
                throw new IllegalArgumentException("Duplicate time slot at line " + (i + 1) + " in " + path.getFileName());
            }

            timeSlots.add(new TimeSlot(date, start, end));
        }

        return timeSlots;
    }

    public static List<Exam> importExams(Path path, List<Course> courses) throws IOException {
        List<List<String>> rows = readCsv(path);
        Map<String, Integer> headers = mapHeaders(rows, path);

        requireHeader(headers, "examid", path);
        requireHeader(headers, "courseid", path);
        requireHeader(headers, "examtype", path);
        requireHeader(headers, "durationminutes", path);

        Map<String, Course> courseMap = new HashMap<>();
        if (courses != null) {
            for (Course course : courses) {
                courseMap.put(course.getCourseId(), course);
            }
        }

        List<Exam> exams = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();

        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            if (row.isEmpty() || isBlankRow(row)) {
                continue;
            }

            String examId = getField(headers, row, "examid");
            String courseId = getField(headers, row, "courseid");
            String examType = getField(headers, row, "examtype");
            int durationMinutes = parseInt(getField(headers, row, "durationminutes"), "duration minutes", i + 1, path);

            if (examId.isBlank()) {
                throw new IllegalArgumentException("Missing Exam ID at line " + (i + 1) + " in " + path.getFileName());
            }
            if (!seenIds.add(examId)) {
                throw new IllegalArgumentException("Duplicate Exam ID '" + examId + "' at line " + (i + 1) + " in " + path.getFileName());
            }

            Course course = courseMap.get(courseId);
            if (course == null) {
                throw new IllegalArgumentException("Unknown Course ID '" + courseId + "' at line " + (i + 1) + " in " + path.getFileName());
            }

            exams.add(new Exam(examId, course, examType, durationMinutes));
        }

        return exams;
    }

    private static List<List<String>> readCsv(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("CSV file is empty: " + path.getFileName());
        }

        List<List<String>> rows = new ArrayList<>();
        for (String line : lines) {
            rows.add(parseCsvLine(line));
        }
        return rows;
    }

    private static Map<String, Integer> mapHeaders(List<List<String>> rows, Path path) {
        List<String> headerRow = rows.get(0);
        if (headerRow.isEmpty()) {
            throw new IllegalArgumentException("CSV header row is empty: " + path.getFileName());
        }

        Map<String, Integer> headerIndex = new HashMap<>();
        for (int i = 0; i < headerRow.size(); i++) {
            String key = headerRow.get(i).trim().toLowerCase();
            if (!key.isEmpty()) {
                headerIndex.put(key, i);
            }
        }
        return headerIndex;
    }

    private static void requireHeader(Map<String, Integer> headers, String name, Path path) {
        if (!headers.containsKey(name)) {
            throw new IllegalArgumentException("Missing required header '" + name + "' in " + path.getFileName());
        }
    }

    private static String getField(Map<String, Integer> headers, List<String> row, String headerName) {
        Integer index = headers.get(headerName);
        if (index == null || index >= row.size()) {
            return "";
        }
        return row.get(index).trim();
    }

    private static int parseInt(String value, String label, int lineNumber, Path path) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid " + label + " at line " + lineNumber + " in " + path.getFileName());
        }
    }

    private static LocalDate parseDate(String value, int lineNumber, Path path) {
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date at line " + lineNumber + " in " + path.getFileName());
        }
    }

    private static LocalTime parseTime(String value, String label, int lineNumber, Path path) {
        try {
            return LocalTime.parse(value.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid " + label + " at line " + lineNumber + " in " + path.getFileName());
        }
    }

    private static boolean isBlankRow(List<String> row) {
        for (String value : row) {
            if (!value.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }

        fields.add(current.toString());
        return fields;
    }
}
