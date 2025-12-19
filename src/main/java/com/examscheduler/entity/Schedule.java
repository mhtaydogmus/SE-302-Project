package com.examscheduler.entity;

import java.util.ArrayList;
import java.util.List;

public class Schedule {
    private final List<ExamSession> examSessions;

    public Schedule() {
        this.examSessions = new ArrayList<>();
    }

    /**
     * Schedule'a yeni bir exam session ekler
     */
    public void addExamSession(ExamSession session) {
        if (session != null && !examSessions.contains(session)) {
            examSessions.add(session);
        }
    }

    /**
     * Schedule'dan bir exam session'ı kaldırır
     */
    public void removeExamSession(ExamSession session) {
        examSessions.remove(session);
    }

    /**
     * Tüm exam session'ları defensive copy olarak döner
     */
    public List<ExamSession> getExamSessions() {
        return new ArrayList<>(examSessions);
    }

    @Override
    public String toString() {
        return "Schedule {" +
                "sessionCount=" + examSessions.size() +
                '}';
    }
}