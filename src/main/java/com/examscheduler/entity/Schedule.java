package com.examscheduler.entity;

import com.examscheduler.constraint.Constraint;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Schedule {
    private String scheduleId;
    private String scheduleName;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<ExamSession> examSessions;
    private List<Constraint> constraints;
    private List<String> schedulingNotes;
    private boolean isValid;
    private List<String> validationMessages;

    public Schedule() {
        this.examSessions = new ArrayList<>();
        this.constraints = new ArrayList<>();
        this.schedulingNotes = new ArrayList<>();
        this.validationMessages = new ArrayList<>();
        this.isValid = false;
    }

    public Schedule(String scheduleId, String scheduleName, LocalDate startDate, LocalDate endDate) {
        this.scheduleId = scheduleId;
        this.scheduleName = scheduleName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.examSessions = new ArrayList<>();
        this.constraints = new ArrayList<>();
        this.schedulingNotes = new ArrayList<>();
        this.validationMessages = new ArrayList<>();
        this.isValid = false;
    }

    public List<String> getSchedulingNotes() {
        return new ArrayList<>(schedulingNotes);
    }

    public void addSchedulingNote(String note) {
        if (note != null && !note.isBlank()) {
            this.schedulingNotes.add(note);
        }
    }

    public List<String> validate() {
        validationMessages.clear();

        for (Constraint constraint : constraints) {
            List<String> violations = constraint.validate(this);
            validationMessages.addAll(violations);
        }

        isValid = validationMessages.isEmpty();
        return new ArrayList<>(validationMessages);
    }

    public void addExamSession(ExamSession session) {
        if (session != null && !examSessions.contains(session)) {
            examSessions.add(session);
        }
    }

    public void removeExamSession(ExamSession session) {
        examSessions.remove(session);
    }

    public void addConstraint(Constraint constraint) {
        if (constraint != null && !constraints.contains(constraint)) {
            constraints.add(constraint);
        }
    }

    public void removeConstraint(Constraint constraint) {
        constraints.remove(constraint);
    }

    public Set<Student> getAllStudents() {
        Set<Student> students = new HashSet<>();
        for (ExamSession session : examSessions) {
            students.addAll(session.getAssignedStudents());
        }
        return students;
    }

    public List<ExamSession> getSessionsForStudent(Student student) {
        List<ExamSession> studentSessions = new ArrayList<>();
        for (ExamSession session : examSessions) {
            if (session.hasStudent(student)) {
                studentSessions.add(session);
            }
        }
        return studentSessions;
    }

    public List<ExamSession> getSessionsOnDate(LocalDate date) {
        List<ExamSession> dateSessions = new ArrayList<>();
        for (ExamSession session : examSessions) {
            if (session.getTimeSlot() != null &&
                date.equals(session.getTimeSlot().getDate())) {
                dateSessions.add(session);
            }
        }
        return dateSessions;
    }

    public Set<LocalDate> getAllDates() {
        Set<LocalDate> dates = new HashSet<>();
        for (ExamSession session : examSessions) {
            if (session.getTimeSlot() != null) {
                dates.add(session.getTimeSlot().getDate());
            }
        }
        return dates;
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public void setScheduleName(String scheduleName) {
        this.scheduleName = scheduleName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<ExamSession> getExamSessions() {
        return new ArrayList<>(examSessions);
    }

    public void setExamSessions(List<ExamSession> examSessions) {
        this.examSessions = examSessions != null ? new ArrayList<>(examSessions) : new ArrayList<>();
    }

    public List<Constraint> getConstraints() {
        return new ArrayList<>(constraints);
    }

    public void setConstraints(List<Constraint> constraints) {
        this.constraints = constraints != null ? new ArrayList<>(constraints) : new ArrayList<>();
    }

    public boolean isValid() {
        return isValid;
    }

    public List<String> getValidationMessages() {
        return new ArrayList<>(validationMessages);
    }

    @Override
    public String toString() {
        return "Schedule{" +
                "scheduleId='" + scheduleId + '\'' +
                ", scheduleName='" + scheduleName + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", sessionCount=" + examSessions.size() +
                ", constraintCount=" + constraints.size() +
                ", isValid=" + isValid +
                '}';
    }
}
