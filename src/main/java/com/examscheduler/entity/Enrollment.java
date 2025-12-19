package com.examscheduler.entity;

import java.time.LocalDateTime;
import java.util.Objects;

public class Enrollment {
    private int enrollmentId;                    // String → int yapıldı
    private Student student;
    private Course course;
    private LocalDateTime enrollmentDate;
    private EnrollmentStatus status;             // String → Enum

    // Enum tanımı (iç sınıfta veya ayrı dosyada olabilir)
    public enum EnrollmentStatus {
        ACTIVE, DROPPED, COMPLETED
    }

    public Enrollment() {
        this.enrollmentDate = LocalDateTime.now();
        this.status = EnrollmentStatus.ACTIVE;
    }

    public Enrollment(int enrollmentId, Student student, Course course) {
        this.enrollmentId = enrollmentId;
        this.student = student;
        this.course = course;
        this.enrollmentDate = LocalDateTime.now();
        this.status = EnrollmentStatus.ACTIVE;

        // Bidirectional ilişkiyi otomatik kur (eğer nesneler null değilse)
        linkStudentAndCourse();
    }

    public Enrollment(int enrollmentId, Student student, Course course, LocalDateTime enrollmentDate, EnrollmentStatus status) {
        this.enrollmentId = enrollmentId;
        this.student = student;
        this.course = course;
        this.enrollmentDate = enrollmentDate;
        this.status = status != null ? status : EnrollmentStatus.ACTIVE;

        linkStudentAndCourse();
    }

    /**
     * Student ve Course ile bidirectional ilişkiyi kurar
     */
    private void linkStudentAndCourse() {
        if (student != null) {
            student.addEnrollment(this);
        }
        if (course != null) {
            course.addEnrollment(this);
        }
    }

    public boolean isActive() {
        return status == EnrollmentStatus.ACTIVE;
    }

    public void drop() {
        this.status = EnrollmentStatus.DROPPED;
    }

    public void complete() {
        this.status = EnrollmentStatus.COMPLETED;
    }

    // Getter & Setter
    public int getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(int enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public LocalDateTime getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(LocalDateTime enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public void setStatus(EnrollmentStatus status) {
        this.status = status != null ? status : EnrollmentStatus.ACTIVE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Enrollment that = (Enrollment) o;
        return enrollmentId == that.enrollmentId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(enrollmentId);
    }

    @Override
    public String toString() {
        String studentId = student != null ? String.valueOf(student.getStudentId()) : "null";
        String courseCode = course != null ? course.getCourseCode() : "null";
        return "Enrollment{" +
                "enrollmentId=" + enrollmentId +
                ", studentId=" + studentId +
                ", courseCode='" + courseCode + '\'' +
                ", enrollmentDate=" + enrollmentDate +
                ", status=" + status +
                '}';
    }
}