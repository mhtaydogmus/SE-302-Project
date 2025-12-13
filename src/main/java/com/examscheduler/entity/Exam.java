package com.examscheduler.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Exam {
    private String examId;
    private Course course;
    private String examType;
    private int durationMinutes;
    private List<ExamSession> examSessions;

    public Exam() {
        this.examSessions = new ArrayList<>();
    }

    public Exam(String examId, Course course, String examType, int durationMinutes) {
        this.examId = examId;
        this.course = course;
        this.examType = examType;
        this.durationMinutes = durationMinutes;
        this.examSessions = new ArrayList<>();
    }

    public void addExamSession(ExamSession session) {
        if (session != null && !examSessions.contains(session)) {
            examSessions.add(session);
        }
    }

    public void removeExamSession(ExamSession session) {
        examSessions.remove(session);
    }

    public List<Student> getEnrolledStudents() {
        if (course != null) {
            return course.getEnrolledStudents();
        }
        return new ArrayList<>();
    }

    public String getExamId() {
        return examId;
    }

    public void setExamId(String examId) {
        this.examId = examId;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public String getExamType() {
        return examType;
    }

    public void setExamType(String examType) {
        this.examType = examType;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public List<ExamSession> getExamSessions() {
        return new ArrayList<>(examSessions);
    }

    public void setExamSessions(List<ExamSession> examSessions) {
        this.examSessions = examSessions != null ? new ArrayList<>(examSessions) : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Exam exam = (Exam) o;
        return Objects.equals(examId, exam.examId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(examId);
    }

    @Override
    public String toString() {
        return "Exam{" +
                "examId='" + examId + '\'' +
                ", course=" + (course != null ? course.getCourseCode() : "null") +
                ", examType='" + examType + '\'' +
                ", durationMinutes=" + durationMinutes +
                ", sessionCount=" + examSessions.size() +
                '}';
    }
}
