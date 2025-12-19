package com.examscheduler.constraint;

import com.examscheduler.entity.Schedule;
import com.examscheduler.entity.Student;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MaxExamsPerDayConstraint extends Constraint {
    private int maxExamsPerDay;

    public MaxExamsPerDayConstraint() {
        this.maxExamsPerDay = 2;
    }

    public MaxExamsPerDayConstraint(int maxExamsPerDay) {
        this.maxExamsPerDay = maxExamsPerDay;
    }

    @Override
    public List<String> validate(Schedule schedule) {
        List<String> violations = new ArrayList<>();

        if (schedule == null) {
            return violations;
        }

        Set<Student> students = schedule.getAllStudents();
        Set<LocalDate> dates = schedule.getAllDates();

        for (Student student : students) {
            for (LocalDate date : dates) {
                int count = student.getDailyExamCount(date);

                if (count > maxExamsPerDay) {
                    String violation = String.format(
                        "MAX EXAMS VIOLATION: Student %s (%s) has %d exams on %s " +
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

    public int getMaxExamsPerDay() {
        return maxExamsPerDay;
    }

    public void setMaxExamsPerDay(int maxExamsPerDay) {
        this.maxExamsPerDay = maxExamsPerDay;
    }

    @Override
    public String toString() {
        return "MaxExamsPerDayConstraint{" +
                ", maxExamsPerDay=" + maxExamsPerDay +
                '}';
    }
}
