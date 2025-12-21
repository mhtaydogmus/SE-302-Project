package com.examscheduler.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExamSession {
    private String sessionId;
    private Exam exam;
    private TimeSlot timeSlot;
    private Room room;
    private List<Student> assignedStudents;
    private int maxCapacity;

    public ExamSession() {
        this.assignedStudents = new ArrayList<>();
    }

    public ExamSession(String sessionId, Exam exam, TimeSlot timeSlot, Room room) {
        this.sessionId = sessionId;
        this.exam = exam;
        this.timeSlot = timeSlot;
        this.room = room;
        this.assignedStudents = new ArrayList<>();
        this.maxCapacity = room != null ? room.getCapacity() : 0;
    }

    public boolean assignStudent(Student student) {
        if (student == null) {
            return false;
        }

        if (assignedStudents.size() >= maxCapacity) {
            return false;
        }

        if (!assignedStudents.contains(student)) {
            assignedStudents.add(student);
            return true;
        }

        return false;
    }

    public void removeStudent(Student student) {
        assignedStudents.remove(student);
    }

    public boolean hasAvailableCapacity() {
        return assignedStudents.size() < maxCapacity;
    }

    public int getAvailableCapacity() {
        return maxCapacity - assignedStudents.size();
    }

    public boolean hasStudent(Student student) {
        return assignedStudents.contains(student);
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Exam getExam() {
        return exam;
    }

    public void setExam(Exam exam) {
        this.exam = exam;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
        if (room != null) {
            this.maxCapacity = room.getCapacity();
        }
    }

    public List<Student> getAssignedStudents() {
        return new ArrayList<>(assignedStudents);
    }

    public void setAssignedStudents(List<Student> assignedStudents) {
        this.assignedStudents = assignedStudents != null ? new ArrayList<>(assignedStudents) : new ArrayList<>();
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public java.time.LocalTime getActualEndTime() {
        if (timeSlot == null || exam == null) {
            return null;
        }
        java.time.LocalTime startTime = timeSlot.getStartTime();
        if (startTime == null) {
            return null;
        }
        int durationMinutes = exam.getDurationMinutes();
        return startTime.plusMinutes(durationMinutes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExamSession that = (ExamSession) o;
        return Objects.equals(sessionId, that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId);
    }

    @Override
    public String toString() {
        return "ExamSession{" +
                "sessionId='" + sessionId + '\'' +
                ", exam=" + (exam != null ? exam.getExamId() : "null") +
                ", timeSlot=" + timeSlot +
                ", room=" + (room != null ? room.getRoomId() : "null") +
                ", assignedStudents=" + assignedStudents.size() +
                ", maxCapacity=" + maxCapacity +
                '}';
    }
}
