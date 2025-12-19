package com.examscheduler.constraint;

import com.examscheduler.entity.ExamSession;
import com.examscheduler.entity.Schedule;
import com.examscheduler.entity.Student;

import java.time.LocalDate;
import java.util.*;

public class MaxExamsPerDayConstraint implements Constraint {

    private int maxExamsPerDay;

    public MaxExamsPerDayConstraint() {
        this.maxExamsPerDay = 2;
    }

    public MaxExamsPerDayConstraint(int maxExamsPerDay) {
        this.maxExamsPerDay = Math.max(1, maxExamsPerDay); // en az 1 olsun
    }

    @Override
    public List<String> validate(Schedule schedule) {
        List<String> violations = new ArrayList<>();

        if (schedule == null || schedule.getExamSessions() == null) {
            return violations;
        }

        // Helper: tüm öğrencileri topla (Schedule'da yoksa burada yap)
        Set<Student> allStudents = new HashSet<>();
        Set<LocalDate> allDates = new HashSet<>();

        for (ExamSession session : schedule.getExamSessions()) {
            allStudents.addAll(session.getAssignedStudents());
            if (session.getTimeSlot() != null) {
                allDates.add(session.getTimeSlot().getDate());
            }
        }

        for (Student student : allStudents) {
            for (LocalDate date : allDates) {
                int count = student.getDailyExamCount(date);

                if (count > maxExamsPerDay) {
                    String violation = String.format(
                            "MAX EXAMS VIOLATION: Student %d (%s) has %d exams on %s " +
                                    "(maximum allowed: %d)",
                            student.getStudentId(),
                            student.getFullName(),
                            count,
                            date,
                            maxExamsPerDay
                    );
                    violations.add(violation);
                }
            }
        }

        return violations;
    }

    @Override
    public String getName() {
        return "Max Exams Per Day Constraint";
    }

    @Override
    public String getDescription() {
        return "Limits students to maximum " + maxExamsPerDay + " exams per day";
    }

    public int getMaxExamsPerDay() {
        return maxExamsPerDay;
    }

    public void setMaxExamsPerDay(int maxExamsPerDay) {
        this.maxExamsPerDay = Math.max(1, maxExamsPerDay);
    }

    @Override
    public String toString() {
        return getName() + " (max = " + maxExamsPerDay + ")";
    }
}