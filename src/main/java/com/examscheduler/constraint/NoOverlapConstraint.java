package com.examscheduler.constraint;

import com.examscheduler.entity.ExamSession;
import com.examscheduler.entity.Schedule;
import com.examscheduler.entity.Student;
import com.examscheduler.entity.TimeSlot;

import java.time.LocalTime;
import java.util.*;

public class NoOverlapConstraint implements Constraint {

    @Override
    public List<String> validate(Schedule schedule) {
        List<String> violations = new ArrayList<>();

        if (schedule == null || schedule.getExamSessions() == null) {
            return violations;
        }

        // Tüm öğrencileri topla (Schedule'da yardımcı metod yoksa burada yapıyoruz)
        Set<Student> allStudents = new HashSet<>();
        for (ExamSession session : schedule.getExamSessions()) {
            allStudents.addAll(session.getAssignedStudents());
        }

        for (Student student : allStudents) {
            // Öğrencinin tüm session'larını topla
            List<ExamSession> studentSessions = new ArrayList<>();
            for (ExamSession session : schedule.getExamSessions()) {
                if (session.getAssignedStudents().contains(student)) {
                    studentSessions.add(session);
                }
            }

            // Optimizasyon: Session'ları startTime'a göre sırala
            studentSessions.sort(Comparator.comparing(es -> {
                TimeSlot ts = es.getTimeSlot();
                return ts != null ? ts.getStartTime() : LocalTime.MAX;
            }));

            // Pairwise kontrol (sıralı olduğu için daha verimli olabilir)
            for (int i = 0; i < studentSessions.size(); i++) {
                ExamSession session1 = studentSessions.get(i);
                TimeSlot ts1 = session1.getTimeSlot();

                if (ts1 == null) continue;

                for (int j = i + 1; j < studentSessions.size(); j++) {
                    ExamSession session2 = studentSessions.get(j);
                    TimeSlot ts2 = session2.getTimeSlot();

                    if (ts2 == null) continue;

                    if (ts1.overlaps(ts2)) {
                        String violation = String.format(
                                "OVERLAP VIOLATION: Student %d (%s) has overlapping exams:%n" +
                                        "  • Session %d (%s) at %s - %s%n" +
                                        "  • Session %d (%s) at %s - %s",
                                student.getStudentId(),
                                student.getFullName(),
                                session1.getSessionId(),
                                session1.getExam() != null ? session1.getExam().getName() : "Unknown Exam",
                                ts1.getStartTime(),
                                ts1.getEndTime(),
                                session2.getSessionId(),
                                session2.getExam() != null ? session2.getExam().getName() : "Unknown Exam",
                                ts2.getStartTime(),
                                ts2.getEndTime()
                        );
                        violations.add(violation);
                    }
                }
            }
        }

        return violations;
    }

    @Override
    public String getName() {
        return "No Overlap Constraint";
    }

    @Override
    public String getDescription() {
        return "Ensures that no student is assigned to two exams at overlapping times.";
    }

    @Override
    public String toString() {
        return getName();
    }
}