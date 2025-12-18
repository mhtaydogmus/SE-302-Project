package edu.ieu.se302.examscheduler.ui.util;

import com.examscheduler.entity.Course;
import com.examscheduler.entity.Enrollment;
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
import java.util.UUID;

public final class CsvImportService {
    private CsvImportService() {
    }

    public static List<Student> importStudents(Path path) throws IOException {
        List<List<String>> rows = readCsv(path);
        Map<String, Integer> headers = mapHeaders(rows, path);

        List<Student> students = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();

        if (isSingleColumnIdList(rows)) {
            parseSingleColumnStudentIds(rows, students, seenIds, path);
        } else {
            int studentIdIndex = resolveHeaderIndex(headers, path, "studentid",
                    new String[]{"student_id", "student id", "std_id", "stdid", "id"});
            int firstNameIndex = resolveHeaderIndex(headers, path, "firstname",
                    new String[]{"first_name", "first name", "givenname", "given_name"});
            int lastNameIndex = resolveHeaderIndex(headers, path, "lastname",
                    new String[]{"last_name", "last name", "surname", "familyname", "family_name"});
            int emailIndex = resolveHeaderIndex(headers, path, "email",
                    new String[]{"email_address", "email address"});
            int genderIndex = resolveHeaderIndex(headers, path, "gender",
                    new String[]{"sex"});

            for (int i = 1; i < rows.size(); i++) {
                List<String> row = rows.get(i);
                if (row.isEmpty() || isBlankRow(row)) {
                    continue;
                }

                String id = getField(row, studentIdIndex);
                String firstName = getField(row, firstNameIndex);
                String lastName = getField(row, lastNameIndex);
                String email = getField(row, emailIndex);
                String gender = getField(row, genderIndex);

                if (id.isBlank()) {
                    throw new IllegalArgumentException("Missing Student ID at line " + (i + 1) + " in " + path.getFileName());
                }
                if (!seenIds.add(id)) {
                    throw new IllegalArgumentException("Duplicate Student ID '" + id + "' at line " + (i + 1) + " in " + path.getFileName());
                }

                students.add(new Student(id, firstName, lastName, email, gender));
            }
        }

        return students;
    }

    public static List<Course> importCourses(Path path) throws IOException {
        List<List<String>> rows = readCsv(path);
        Map<String, Integer> headers = mapHeaders(rows, path);

        List<Course> courses = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();

        if (isSingleColumnIdList(rows)) {
            parseSingleColumnCourseCodes(rows, courses, seenIds, path);
        } else {
            int courseIdIndex = resolveHeaderIndex(headers, path, "courseid",
                    new String[]{"course_id", "course id", "id"});
            int courseNameIndex = resolveHeaderIndex(headers, path, "coursename",
                    new String[]{"course_name", "course name", "name"});
            int courseCodeIndex = resolveHeaderIndex(headers, path, "coursecode",
                    new String[]{"course_code", "course code", "code"});
            int creditsIndex = resolveHeaderIndex(headers, path, "credits",
                    new String[]{"credit", "credit_hours", "credit hours"});

            for (int i = 1; i < rows.size(); i++) {
                List<String> row = rows.get(i);
                if (row.isEmpty() || isBlankRow(row)) {
                    continue;
                }

                String id = getField(row, courseIdIndex);
                String name = getField(row, courseNameIndex);
                String code = getField(row, courseCodeIndex);
                int credits = parseInt(getField(row, creditsIndex), "credits", i + 1, path);

                if (id.isBlank()) {
                    throw new IllegalArgumentException("Missing Course ID at line " + (i + 1) + " in " + path.getFileName());
                }
                if (!seenIds.add(id)) {
                    throw new IllegalArgumentException("Duplicate Course ID '" + id + "' at line " + (i + 1) + " in " + path.getFileName());
                }

                courses.add(new Course(id, name, code, credits));
            }
        }

        return courses;
    }

    public static List<Room> importRooms(Path path) throws IOException {
        List<List<String>> rows = readCsv(path);
        Map<String, Integer> headers = mapHeaders(rows, path);

        List<Room> rooms = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();

        if (isSingleColumnIdList(rows)) {
            parseSingleColumnRooms(rows, rooms, seenIds, path);
        } else {
            int roomIdIndex = resolveHeaderIndex(headers, path, "roomid",
                    new String[]{"room_id", "room id", "id", "classroomid", "classroom_id"});
            int roomNameIndex = resolveHeaderIndex(headers, path, "roomname",
                    new String[]{"room_name", "room name", "name", "classroom", "classroom_name"});
            int capacityIndex = resolveHeaderIndex(headers, path, "capacity",
                    new String[]{"room_capacity", "room capacity"});

            for (int i = 1; i < rows.size(); i++) {
                List<String> row = rows.get(i);
                if (row.isEmpty() || isBlankRow(row)) {
                    continue;
                }

                String id = getField(row, roomIdIndex);
                String name = getField(row, roomNameIndex);
                int capacity = parseInt(getField(row, capacityIndex), "capacity", i + 1, path);

                if (id.isBlank()) {
                    throw new IllegalArgumentException("Missing Room ID at line " + (i + 1) + " in " + path.getFileName());
                }
                if (!seenIds.add(id)) {
                    throw new IllegalArgumentException("Duplicate Room ID '" + id + "' at line " + (i + 1) + " in " + path.getFileName());
                }

                rooms.add(new Room(id, name, capacity));
            }
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

    public static List<Enrollment> importAttendance(Path path, List<Student> students, List<Course> courses) throws IOException {
        List<List<String>> rows = readCsv(path);

        Map<String, Student> studentMap = new HashMap<>();
        if (students != null) {
            for (Student s : students) {
                studentMap.put(s.getStudentId(), s);
            }
        }

        Map<String, Course> courseMap = new HashMap<>();
        if (courses != null) {
            for (Course c : courses) {
                courseMap.put(c.getCourseId(), c);
            }
        }

        List<Enrollment> enrollments = new ArrayList<>();
        Set<String> seenPairs = new HashSet<>();
        Course currentCourse = null;

        for (int i = 0; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            if (row.isEmpty() || isBlankRow(row)) {
                continue;
            }

            List<String> nonEmptyCells = new ArrayList<>();
            for (String cell : row) {
                if (cell != null && !cell.trim().isEmpty()) {
                    nonEmptyCells.add(cell);
                }
            }
            if (nonEmptyCells.isEmpty()) {
                continue;
            }

            String firstToken = cleanToken(nonEmptyCells.get(0));
            boolean headerLike = firstToken.toLowerCase().contains("course");
            boolean onlyFirstCell = nonEmptyCells.size() == 1;
            boolean firstIsCourse = courseMap.containsKey(firstToken);

            // Detect course rows (single cell course code)
            if ((onlyFirstCell && firstIsCourse) || (onlyFirstCell && headerLike)) {
                currentCourse = courseMap.get(firstToken);
                if (currentCourse == null && headerLike) {
                    continue; // skip header-like row
                }
                if (currentCourse == null) {
                    throw new IllegalArgumentException("Unknown Course ID '" + firstToken + "' at line " + (i + 1) + " in " + path.getFileName());
                }
                continue;
            }

            Course course = null;
            int studentStartIndex = 0;
            if (firstIsCourse) {
                course = courseMap.get(firstToken);
                studentStartIndex = 1;
            } else {
                course = currentCourse;
            }

            if (course == null) {
                if (firstIsCourse) {
                    throw new IllegalArgumentException("Unknown Course ID '" + firstToken + "' at line " + (i + 1) + " in " + path.getFileName() +
                            ". Import courses before attendance.");
                } else {
                    throw new IllegalArgumentException("Missing course header before student list at line " + (i + 1) + " in " + path.getFileName());
                }
            }
            currentCourse = course;

            for (int j = studentStartIndex; j < row.size(); j++) {
                String cell = row.get(j);
                if (cell == null || cell.trim().isEmpty()) {
                    continue;
                }
            List<String> tokens = splitStudentTokens(cell);
            for (String token : tokens) {
                if (token.isEmpty()) {
                    continue;
                }
                Student student = studentMap.get(token);
                    if (student == null) {
                        throw new IllegalArgumentException("Unknown Student ID '" + token + "' at line " + (i + 1) + " in " + path.getFileName());
                    }
                    String pairKey = course.getCourseId() + "|" + token;
                    if (!seenPairs.add(pairKey)) {
                        continue;
                    }
                    Enrollment enrollment = new Enrollment(UUID.randomUUID().toString(), student, course);
                    enrollments.add(enrollment);
                    student.addEnrollment(enrollment);
                    course.addEnrollment(enrollment);
                }
            }
        }

        return enrollments;
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

    private static int resolveHeaderIndex(Map<String, Integer> headers, Path path, String canonical, String[] aliases) {
        Integer index = headers.get(canonical);
        if (index != null) {
            return index;
        }
        for (String alias : aliases) {
            index = headers.get(alias);
            if (index != null) {
                return index;
            }
        }
        throw new IllegalArgumentException("Missing required header '" + canonical + "' in " + path.getFileName());
    }

    private static String getField(Map<String, Integer> headers, List<String> row, String headerName) {
        Integer index = headers.get(headerName);
        if (index == null || index >= row.size()) {
            return "";
        }
        return row.get(index).trim();
    }

    private static String getField(List<String> row, int index) {
        if (index < 0 || index >= row.size()) {
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

    private static boolean isSingleColumnIdList(List<List<String>> rows) {
        if (rows.isEmpty()) {
            return false;
        }
        int nonEmptyRows = 0;
        for (List<String> row : rows) {
            if (row.isEmpty() || isBlankRow(row)) {
                continue;
            }
            int nonEmptyCells = 0;
            for (String cell : row) {
                if (!cell.trim().isEmpty()) {
                    nonEmptyCells++;
                }
            }
            if (nonEmptyCells > 1) {
                return false;
            }
            nonEmptyRows++;
        }
        return nonEmptyRows > 0;
    }

    private static void parseSingleColumnStudentIds(List<List<String>> rows,
                                                    List<Student> students,
                                                    Set<String> seenIds,
                                                    Path path) {
        for (int i = 0; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            if (row.isEmpty() || isBlankRow(row)) {
                continue;
            }
            String value = row.get(0).trim();
            if (value.isEmpty()) {
                continue;
            }
            if (i == 0 && value.toLowerCase().contains("student")) {
                continue;
            }
            String id = value;
            if (!seenIds.add(id)) {
                throw new IllegalArgumentException("Duplicate Student ID '" + id + "' at line " + (i + 1) + " in " + path.getFileName());
            }
            students.add(new Student(id, "N/A", "N/A", "N/A", "Not specified"));
        }
    }

    private static void parseSingleColumnCourseCodes(List<List<String>> rows,
                                                     List<Course> courses,
                                                     Set<String> seenIds,
                                                     Path path) {
        for (int i = 0; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            if (row.isEmpty() || isBlankRow(row)) {
                continue;
            }
            String value = row.get(0).trim();
            if (value.isEmpty()) {
                continue;
            }
            if (i == 0 && value.toLowerCase().contains("course")) {
                continue;
            }
            String id = value;
            if (!seenIds.add(id)) {
                throw new IllegalArgumentException("Duplicate Course ID '" + id + "' at line " + (i + 1) + " in " + path.getFileName());
            }
            courses.add(new Course(id, "N/A", id, 0));
        }
    }

    private static void parseSingleColumnRooms(List<List<String>> rows,
                                               List<Room> rooms,
                                               Set<String> seenIds,
                                               Path path) {
        for (int i = 0; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            if (row.isEmpty() || isBlankRow(row)) {
                continue;
            }
            String value = row.get(0).trim();
            if (value.isEmpty()) {
                continue;
            }
            String lower = value.toLowerCase();
            if (i == 0 && (lower.contains("classroom") || lower.contains("room"))) {
                continue;
            }
            String[] parts = value.split("[;,]");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Invalid room row at line " + (i + 1) + " in " + path.getFileName());
            }
            String roomName = parts[0].trim();
            int capacity = parseInt(parts[1].trim(), "capacity", i + 1, path);
            String id = roomName;
            if (!seenIds.add(id)) {
                throw new IllegalArgumentException("Duplicate Room ID '" + id + "' at line " + (i + 1) + " in " + path.getFileName());
            }
            rooms.add(new Room(id, roomName, capacity));
        }
    }

    private static List<String> splitStudentTokens(String cell) {
        String cleaned = cell.replace("[", " ")
                .replace("]", " ")
                .replace("'", " ")
                .replace("\"", " ")
                .trim();
        String[] parts = cleaned.split("[,;\\s]+");
        List<String> tokens = new ArrayList<>();
        for (String part : parts) {
            if (!part.trim().isEmpty()) {
                tokens.add(part.trim());
            }
        }
        return tokens;
    }

    private static String cleanToken(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("[", "")
                .replace("]", "")
                .replace("'", "")
                .replace("\"", "")
                .trim();
    }
}
