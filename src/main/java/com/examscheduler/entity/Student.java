package com.examscheduler.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Student {
    private String studentId;
    private String firstName;
    private String lastName;
    private String email;
    private String gender;
    private List<Enrollment> enrollments;
    private List<ExamSession> assignedSessions;

    public Student() {
        this.enrollments = new ArrayList<>();
        this.assignedSessions = new ArrayList<>();
    }

    public Student(String studentId, String firstName, String lastName, String email, String gender) {
        this.studentId = studentId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.gender = gender;
        this.enrollments = new ArrayList<>();
        this.assignedSessions = new ArrayList<>();
    }
    
    public Student(String studentId, String firstName, String lastName, String email) {
        this(studentId, firstName, lastName, email, "Not specified");
    }


    public int getDailyExamCount(LocalDate date) {
        if (date == null) {
            return 0;
        }

        int count = 0;
        for (ExamSession session : assignedSessions) {
            if (session.getTimeSlot() != null &&
                date.equals(session.getTimeSlot().getDate())) {
                count++;
            }
        }
        return count;
    }

    public boolean hasExamOverlap(ExamSession session) {
        if (session == null || session.getTimeSlot() == null) {
            return false;
        }

        for (ExamSession existingSession : assignedSessions) {
            if (existingSession.getTimeSlot() != null &&
                existingSession.getTimeSlot().overlaps(session.getTimeSlot())) {
                return true;
            }
        }
        return false;
    }

    public void addEnrollment(Enrollment enrollment) {
        if (enrollment != null && !enrollments.contains(enrollment)) {
            enrollments.add(enrollment);
        }
    }

    public void removeEnrollment(Enrollment enrollment) {
        enrollments.remove(enrollment);
    }

    public void assignExamSession(ExamSession session) {
        if (session != null && !assignedSessions.contains(session)) {
            assignedSessions.add(session);
        }
    }

    public void removeExamSession(ExamSession session) {
        assignedSessions.remove(session);
    }

    public List<Course> getEnrolledCourses() {
        List<Course> courses = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            courses.add(enrollment.getCourse());
        }
        return courses;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }


    public List<Enrollment> getEnrollments() {
        return new ArrayList<>(enrollments);
    }

    public void setEnrollments(List<Enrollment> enrollments) {
        this.enrollments = enrollments != null ? new ArrayList<>(enrollments) : new ArrayList<>();
    }

    public List<ExamSession> getAssignedSessions() {
        return new ArrayList<>(assignedSessions);
    }

    public void setAssignedSessions(List<ExamSession> assignedSessions) {
        this.assignedSessions = assignedSessions != null ? new ArrayList<>(assignedSessions) : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return Objects.equals(studentId, student.studentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId);
    }

    @Override
    public String toString() {
        return "Student{" +
                "studentId='" + studentId + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", email='" + email + '\'' +
                ", gender='" + gender + '\'' +
                ", enrollmentCount=" + enrollments.size() +
                ", assignedSessionCount=" + assignedSessions.size() +
                '}';
    }
}
