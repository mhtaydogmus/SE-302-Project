package com.examscheduler.entity;

import java.time.LocalDateTime;
import java.util.Objects;

public class Enrollment {
    private String enrollmentId;
    private Student student;
        private Course course;
        private LocalDateTime enrollmentDate;
        private EnrollmentStatus status;
    
        public Enrollment() {
        }
    
        public Enrollment(String enrollmentId, Student student, Course course) {
            this.enrollmentId = enrollmentId;
            this.student = student;
            this.course = course;
            this.enrollmentDate = LocalDateTime.now();
            this.status = EnrollmentStatus.ACTIVE;
        }
    
        public Enrollment(String enrollmentId, Student student, Course course, LocalDateTime enrollmentDate, EnrollmentStatus status) {
            this.enrollmentId = enrollmentId;
            this.student = student;
            this.course = course;
            this.enrollmentDate = enrollmentDate;
            this.status = status;
        }
    
        public boolean isActive() {
            return status == EnrollmentStatus.ACTIVE;
        }
    
        public void drop() {
            this.status = EnrollmentStatus.WITHDRAWN;
        }
    
        public void complete() {
            this.status = EnrollmentStatus.COMPLETED;
        }

    public String getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(String enrollmentId) {
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
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Enrollment that = (Enrollment) o;
        return Objects.equals(enrollmentId, that.enrollmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enrollmentId);
    }

    @Override
    public String toString() {
        return "Enrollment{" +
                "enrollmentId='" + enrollmentId + '\'' +
                ", student=" + (student != null ? student.getStudentId() : "null") +
                ", course=" + (course != null ? course.getCourseCode() : "null") +
                ", enrollmentDate=" + enrollmentDate +
                ", status=" + status +
                '}';
    }
}
