package com.examscheduler.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Exam {
    private int examId;                      // String → int yapıldı (belgeye uygun)
    private Course course;
    private String name;
    private int durationMinutes;
    private List<ExamSession> examSessions;

    public Exam() {
        this.examSessions = new ArrayList<>();
    }

    public Exam(int examId, Course course, String name, int durationMinutes) {
        this.examId = examId;
        this.course = course;
        this.name = name;
        this.durationMinutes = durationMinutes;
        this.examSessions = new ArrayList<>();
    }

    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public List<ExamSession> getExamSessions() {
        return new ArrayList<>(examSessions);  // Defensive copy
    }

    public void addExamSession(ExamSession session) {
        if (session != null && !examSessions.contains(session)) {
            examSessions.add(session);
        }
    }

    public void removeExamSession(ExamSession session) {
        if (session != null) {
            examSessions.remove(session);
        }
    }

    /**
     * Önemli: Enrolled öğrencileri Course üzerinden alır
     */
    public List<Student> getEnrolledStudents() {
        return course != null ? course.getEnrolledStudents() : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Exam exam = (Exam) o;
        return examId == exam.examId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(examId);
    }

    @Override
    public String toString() {
        String courseName = course != null ? course.getCourseName() : "No Course";
        return (name != null ? name : "Unnamed Exam") +
                " (ID: " + examId + ") - " +
                courseName +
                " - " + durationMinutes + " min";
    }
}