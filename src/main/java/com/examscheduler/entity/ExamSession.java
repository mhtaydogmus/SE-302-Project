package com.examscheduler.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExamSession {
    private int sessionId;
    private Exam exam;
    private TimeSlot timeSlot;
    private Room room;
    private List<Student> assignedStudents;
    private int maxCapacity;

    public ExamSession() {
        this.assignedStudents = new ArrayList<>();
        this.maxCapacity = 0;
    }

    public ExamSession(int sessionId, Exam exam, TimeSlot timeSlot, Room room) {
        this.sessionId = sessionId;
        this.exam = exam;
        this.timeSlot = timeSlot;
        this.room = room;
        this.assignedStudents = new ArrayList<>();
        this.maxCapacity = (room != null) ? room.getCapacity() : 0;
    }

    /**
     * Öğrenciyi bu session'a atar ve öğrencinin kendi listesine de ekler (bidirectional)
     */
    public boolean assignStudent(Student student) {
        if (student == null) {
            return false;
        }

        if (assignedStudents.size() >= maxCapacity) {
            return false; // Kapasite dolu
        }

        if (assignedStudents.contains(student)) {
            return false; // Zaten atanmış
        }

        // İki yönlü ilişkiyi koru
        assignedStudents.add(student);
        student.assignExamSession(this);  // <<< EN ÖNEMLİ SATIR!

        return true;
    }

    /**
     * Öğrenciyi bu session'dan çıkarır ve öğrencinin listesinden de kaldırır
     */
    public void removeStudent(Student student) {
        if (student != null && assignedStudents.remove(student)) {
            student.removeExamSession(this);  // <<< Bidirectional temizlik
        }
    }

    public boolean hasAvailableCapacity() {
        return assignedStudents.size() < maxCapacity;
    }

    public int getAvailableCapacity() {
        return maxCapacity - assignedStudents.size();
    }

    public boolean hasStudent(Student student) {
        return student != null && assignedStudents.contains(student);
    }

    // Getter & Setter
    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
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
        } else {
            this.maxCapacity = 0;
        }
    }

    public List<Student> getAssignedStudents() {
        return new ArrayList<>(assignedStudents); // Defensive copy
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExamSession that = (ExamSession) o;
        return sessionId == that.sessionId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId);
    }

    @Override
    public String toString() {
        return "ExamSession{" +
                "sessionId=" + sessionId +
                ", exam=" + (exam != null ? exam.getExamId() : "null") +
                ", timeSlot=" + timeSlot +
                ", room=" + (room != null ? room.getRoomId() : "null") +
                ", assignedStudents=" + assignedStudents.size() +
                ", maxCapacity=" + maxCapacity +
                '}';
    }
}