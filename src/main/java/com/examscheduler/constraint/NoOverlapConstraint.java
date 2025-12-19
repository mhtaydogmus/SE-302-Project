package com.examscheduler.constraint;

import com.examscheduler.entity.ExamSession;
import com.examscheduler.entity.Schedule;
import com.examscheduler.entity.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NoOverlapConstraint extends Constraint {

    @Override
    public List<String> validate(Schedule schedule) {
        List<String> violations = new ArrayList<>();

        if (schedule == null) {
            return violations;
        }

        Set<Student> students = schedule.getAllStudents();
        List<ExamSession> allSessions = schedule.getExamSessions();

        for (Student student : students) {
            List<ExamSession> studentSessions = schedule.getSessionsForStudent(student);

            for (int i = 0; i < studentSessions.size(); i++) {
                ExamSession session1 = studentSessions.get(i);

                for (int j = i + 1; j < studentSessions.size(); j++) {
                    ExamSession session2 = studentSessions.get(j);

                    if (session1.getTimeSlot() != null &&
                        session2.getTimeSlot() != null &&
                        session1.getTimeSlot().overlaps(session2.getTimeSlot())) {

                        String violation = String.format(
                            "OVERLAP VIOLATION: Student %s (%s) has overlapping exams: " +
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

        for (int i = 0; i < allSessions.size(); i++) {
            ExamSession first = allSessions.get(i);
            if (first.getRoom() == null || first.getTimeSlot() == null) {
                continue;
            }

            for (int j = i + 1; j < allSessions.size(); j++) {
                ExamSession second = allSessions.get(j);
                if (second.getRoom() == null || second.getTimeSlot() == null) {
                    continue;
                }

                boolean sameRoom = first.getRoom().equals(second.getRoom());
                boolean overlap = first.getTimeSlot().overlaps(second.getTimeSlot());

                if (sameRoom && overlap) {
                    String violation = String.format(
                        "ROOM OVERLAP VIOLATION: Room %s has overlapping sessions %s and %s at %s / %s",
                        first.getRoom().getRoomName(),
                        first.getSessionId(),
                        second.getSessionId(),
                        first.getTimeSlot(),
                        second.getTimeSlot()
                    );
                    violations.add(violation);
                }
            }
        }

        return violations;
    }
}
