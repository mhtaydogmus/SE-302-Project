package com.examscheduler.constraint;

import com.examscheduler.entity.ExamSession;
import com.examscheduler.entity.Schedule;
import com.examscheduler.entity.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NoConsecutiveExamsConstraint extends Constraint {

    @Override
    public List<String> validate(Schedule schedule) {
        List<String> violations = new ArrayList<>();

        if (schedule == null) {
            return violations;
        }

        Set<Student> students = schedule.getAllStudents();

        for (Student student : students) {
            List<ExamSession> sessions = schedule.getSessionsForStudent(student);

            for (int i = 0; i < sessions.size(); i++) {
                ExamSession session1 = sessions.get(i);
                if (session1.getTimeSlot() == null) {
                    continue;
                }

                for (int j = i + 1; j < sessions.size(); j++) {
                    ExamSession session2 = sessions.get(j);
                    if (session2.getTimeSlot() == null) {
                        continue;
                    }

                    if (!session1.getTimeSlot().getDate().equals(session2.getTimeSlot().getDate())) {
                        continue;
                    }

                    boolean consecutive = session1.getTimeSlot().getEndTime().equals(session2.getTimeSlot().getStartTime()) ||
                        session2.getTimeSlot().getEndTime().equals(session1.getTimeSlot().getStartTime());

                    if (consecutive) {
                        String violation = String.format(
                            "CONSECUTIVE EXAMS VIOLATION: Student %s (%s) has back-to-back exams: " +
                            "Session %s at %s and Session %s at %s",
                            student.getStudentId(),
                            student.getFullName(),
                            session1.getSessionId(),
                            session1.getTimeSlot(),
                            session2.getSessionId(),
                            session2.getTimeSlot()
                        );
                        violations.add(violation);
                    }
                }
            }
        }

        return violations;
    }
}
