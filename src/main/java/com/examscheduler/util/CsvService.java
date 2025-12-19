package com.examscheduler.util;

import com.examscheduler.entity.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class CsvService {

    // Students: ID,FirstName,LastName,Email → ID int
    public static List<Student> importStudents(Path path) throws IOException {
        List<Student> list = new ArrayList<>();
        List<String> lines = Files.readAllLines(path);

        for (int i = 1; i < lines.size(); i++) { // İlk satır header
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            String[] p = line.split(",", -1);
            if (p.length < 4) continue;

            try {
                int id = Integer.parseInt(p[0].trim());
                String firstName = p[1].trim();
                String lastName = p[2].trim();
                String email = p[3].trim();

                list.add(new Student(id, firstName, lastName, email));
            } catch (NumberFormatException ignored) {
                // ID sayı değilse satırı sessizce atla
            }
        }
        return list;
    }

    // Courses: ID,Code,Name,Credits → ID int
    public static List<Course> importCourses(Path path) throws IOException {
        List<Course> list = new ArrayList<>();
        List<String> lines = Files.readAllLines(path);

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            String[] p = line.split(",", -1);
            if (p.length < 4) continue;

            try {
                int id = Integer.parseInt(p[0].trim());
                String code = p[1].trim();
                String name = p[2].trim();
                int credits = Integer.parseInt(p[3].trim());

                list.add(new Course(id, code, name, credits));
            } catch (NumberFormatException ignored) {
                // Hatalı satırları sessizce atla
            }
        }
        return list;
    }

    // Rooms: ID,Name,Capacity → ID int (Room artık int kullanıyor)
    public static List<Room> importRooms(Path path) throws IOException {
        List<Room> list = new ArrayList<>();
        List<String> lines = Files.readAllLines(path);

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            String[] p = line.split(",", -1);
            if (p.length < 3) continue;

            try {
                int id = Integer.parseInt(p[0].trim());
                String name = p[1].trim();
                int capacity = Integer.parseInt(p[2].trim());

                list.add(new Room(id, name, capacity));
            } catch (NumberFormatException ignored) {
                // Hatalı satırları sessizce atla
            }
        }
        return list;
    }

    // TimeSlots: Date,StartTime,EndTime
    public static List<TimeSlot> importTimeSlots(Path path) throws IOException {
        List<TimeSlot> list = new ArrayList<>();
        List<String> lines = Files.readAllLines(path);

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            String[] p = line.split(",", -1);
            if (p.length < 3) continue;

            try {
                LocalDate date = LocalDate.parse(p[0].trim());
                LocalTime start = LocalTime.parse(p[1].trim());
                LocalTime end = LocalTime.parse(p[2].trim());

                list.add(new TimeSlot(date, start, end));
            } catch (Exception ignored) {
                // Tarih/saat formatı hatalıysa sessizce atla
            }
        }
        return list;
    }

    // Exams: ID,CourseID,Name,Duration → ID ve CourseID int
    public static List<Exam> importExams(Path path, List<Course> courses) throws IOException {
        List<Exam> list = new ArrayList<>();
        List<String> lines = Files.readAllLines(path);

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            String[] p = line.split(",", -1);
            if (p.length < 4) continue;

            try {
                int examId = Integer.parseInt(p[0].trim());
                int courseId = Integer.parseInt(p[1].trim());
                String name = p[2].trim();
                int duration = Integer.parseInt(p[3].trim());

                Course course = courses.stream()
                        .filter(c -> c.getCourseId() == courseId)
                        .findFirst()
                        .orElse(null);

                if (course != null) {
                    list.add(new Exam(examId, course, name, duration));
                }
            } catch (NumberFormatException ignored) {
                // Sayısal alanlarda hata varsa sessizce atla
            }
        }
        return list;
    }
}