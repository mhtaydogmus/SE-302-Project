package com.examscheduler.data;

import com.examscheduler.entity.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Optional;

public class DataStore {

    private static final ObservableList<Student> students = FXCollections.observableArrayList();
    private static final ObservableList<Course> courses = FXCollections.observableArrayList();
    private static final ObservableList<Room> rooms = FXCollections.observableArrayList();
    private static final ObservableList<TimeSlot> timeSlots = FXCollections.observableArrayList();
    private static final ObservableList<Exam> exams = FXCollections.observableArrayList();
    private static final ObservableList<Enrollment> enrollments = FXCollections.observableArrayList();

    public static ObservableList<Student> getStudents() { return students; }
    public static ObservableList<Course> getCourses() { return courses; }
    public static ObservableList<Room> getRooms() { return rooms; }
    public static ObservableList<TimeSlot> getTimeSlots() { return timeSlots; }
    public static ObservableList<Exam> getExams() { return exams; }
    public static ObservableList<Enrollment> getEnrollments() { return enrollments; }

    // Hızlı erişim metodları (ID bazlı)
    public static Optional<Student> findStudentById(int studentId) {
        return students.stream()
                .filter(s -> s.getStudentId() == studentId)
                .findFirst();
    }

    public static Optional<Course> findCourseById(String courseId) {
        if (courseId == null) return Optional.empty();
        return courses.stream()
                .filter(c -> courseId.equals(c.getCourseId()))  // null-safe: önce parametreyi koy
                .findFirst();
    }

    public static Optional<Room> findRoomById(String roomId) {
        if (roomId == null) return Optional.empty();
        return rooms.stream()
                .filter(r -> roomId.equals(r.getRoomId()))
                .findFirst();
    }

    // Güvenli ekleme metodları (bidirectional ilişkiyi korur)

    public static boolean addStudent(Student student) {
        if (student == null || findStudentById(student.getStudentId()).isPresent()) {
            return false;
        }
        return students.add(student);
    }

    public static boolean removeStudent(Student student) {
        if (student == null) return false;

        // İlgili enrollment'ları temizle
        List<Enrollment> toRemove = enrollments.stream()
                .filter(e -> e.getStudent().equals(student))
                .toList();

        for (Enrollment e : toRemove) {
            removeEnrollment(e);
        }

        return students.remove(student);
    }

    public static boolean addCourse(Course course) {
        if (course == null) return false;
        return courses.add(course);
    }

    public static boolean addRoom(Room room) {
        if (room == null) return false;
        return rooms.add(room);
    }

    public static boolean addTimeSlot(TimeSlot timeSlot) {
        if (timeSlot == null) return false;
        return timeSlots.add(timeSlot);
    }

    public static boolean addExam(Exam exam) {
        if (exam == null) return false;
        return exams.add(exam);
    }

    /**
     * Enrollment ekler ve bidirectional ilişkiyi korur
     */
    public static boolean addEnrollment(Enrollment enrollment) {
        if (enrollment == null || enrollments.contains(enrollment)) {
            return false;
        }

        Student student = enrollment.getStudent();
        Course course = enrollment.getCourse();

        if (student == null || course == null) {
            return false;
        }

        // Bidirectional ilişkiyi sağla
        student.addEnrollment(enrollment);
        if (course.getEnrollments() != null) { // Course'da enrollments listesi varsa
            course.addEnrollment(enrollment);   // Bu metodu Course'a eklememiz gerekebilir
        }

        return enrollments.add(enrollment);
    }

    public static boolean removeEnrollment(Enrollment enrollment) {
        if (enrollment == null) return false;

        Student student = enrollment.getStudent();
        Course course = enrollment.getCourse();

        if (student != null) {
            student.removeEnrollment(enrollment);
        }
        if (course != null && course.getEnrollments() != null) {
            course.removeEnrollment(enrollment);
        }

        return enrollments.remove(enrollment);
    }

    public static void clearAll() {
        // Önce enrollment'ları temizle (ilişki kopmasın)
        enrollments.clear();
        students.clear();
        courses.clear();
        rooms.clear();
        timeSlots.clear();
        exams.clear();
    }
}