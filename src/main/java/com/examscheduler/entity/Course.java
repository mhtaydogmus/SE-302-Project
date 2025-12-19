package com.examscheduler.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Course {
    private String courseId;
    private String courseName;
    private String courseCode;
    private int credits;
    private int examDurationMinutes;
    private List<Enrollment> enrollments;

    public Course() {
        this.examDurationMinutes = 0;
        this.enrollments = new ArrayList<>();
    }

    public Course(String courseId, String courseName, String courseCode, int credits) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.courseCode = courseCode;
        this.credits = credits;
        this.examDurationMinutes = 0;
        this.enrollments = new ArrayList<>();
    }

    public void addEnrollment(Enrollment enrollment) {
        if (enrollment != null && !enrollments.contains(enrollment)) {
            enrollments.add(enrollment);
        }
    }

    public void removeEnrollment(Enrollment enrollment) {
        enrollments.remove(enrollment);
    }

    public List<Student> getEnrolledStudents() {
        List<Student> students = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            students.add(enrollment.getStudent());
        }
        return students;
    }

    public int getEnrollmentCount() {
        return enrollments.size();
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public int getExamDurationMinutes() {
        return examDurationMinutes;
    }

    public void setExamDurationMinutes(int examDurationMinutes) {
        this.examDurationMinutes = examDurationMinutes;
    }

    public List<Enrollment> getEnrollments() {
        return new ArrayList<>(enrollments);
    }

    public void setEnrollments(List<Enrollment> enrollments) {
        this.enrollments = enrollments != null ? new ArrayList<>(enrollments) : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return Objects.equals(courseId, course.courseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseId);
    }

    @Override
    public String toString() {
        return "Course{" +
                "courseId='" + courseId + '\'' +
                ", courseName='" + courseName + '\'' +
                ", courseCode='" + courseCode + '\'' +
                ", credits=" + credits +
                ", enrollmentCount=" + enrollments.size() +
                '}';
    }
}
