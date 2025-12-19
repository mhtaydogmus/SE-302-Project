package com.examscheduler.scheduler;

import com.examscheduler.constraint.Constraint;
import com.examscheduler.constraint.MaxExamsPerDayConstraint;
import com.examscheduler.constraint.NoOverlapConstraint;
import com.examscheduler.entity.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Scheduler {
    private List<Room> availableRooms;
    private List<TimeSlot> availableTimeSlots;
    private int maxExamsPerDay = 2;

    private int nextSessionId = 1; // Otomatik artan session ID için

    // Constraint'ları Scheduler içinde tutuyoruz (Design Document'a uygun)
    private final List<Constraint> constraints;

    public Scheduler() {
        this.availableRooms = new ArrayList<>();
        this.availableTimeSlots = new ArrayList<>();
        this.constraints = new ArrayList<>();

        // Varsayılan constraint'ları ekle (dökümanda Scheduler'ın constraint kullandığı belirtiliyor)
        this.constraints.add(new NoOverlapConstraint());
        this.constraints.add(new MaxExamsPerDayConstraint(maxExamsPerDay));
    }

    public void setAvailableRooms(List<Room> rooms) {
        this.availableRooms = new ArrayList<>(rooms);
    }

    public void setAvailableTimeSlots(List<TimeSlot> slots) {
        this.availableTimeSlots = new ArrayList<>(slots);
    }

    public void setMaxExamsPerDay(int max) {
        if (max > 0) {
            this.maxExamsPerDay = max;
            // MaxExamsPerDayConstraint varsa güncelle
            constraints.stream()
                    .filter(c -> c instanceof MaxExamsPerDayConstraint)
                    .map(c -> (MaxExamsPerDayConstraint) c)
                    .forEach(c -> c.setMaxExamsPerDay(max));
        }
    }

    /**
     * Schedule üretir ve tüm constraint'ları kullanarak validation yapar
     */
    public Schedule generateSchedule(List<Course> courses, List<Exam> exams) {
        Schedule schedule = new Schedule(); // Artık sadece examSessions tutuyor

        // En çok öğrencisi olan exam'leri önce yerleştir (dökümana uygun)
        List<Exam> sortedExams = exams.stream()
                .sorted(Comparator.comparingInt(e -> -e.getEnrolledStudents().size()))
                .collect(Collectors.toList());

        for (Exam exam : sortedExams) {
            List<Student> enrolled = new ArrayList<>(exam.getEnrolledStudents());
            if (enrolled.isEmpty()) continue;

            List<ExamSession> sessions = assignStudentsToSessions(exam, enrolled, schedule);
            for (ExamSession session : sessions) {
                exam.addExamSession(session);
                schedule.addExamSession(session);
            }
        }

        // Validation artık Scheduler'da yapılıyor
        return schedule;
    }

    /**
     * Schedule'ı tüm constraint'lara göre doğrular ve ihlalleri döner
     * Design Document 5.5.4'e uygun
     */
    public List<String> validateSchedule(Schedule schedule) {
        List<String> violations = new ArrayList<>();

        for (Constraint constraint : constraints) {
            violations.addAll(constraint.validate(schedule));
        }

        return violations;
    }

    private List<ExamSession> assignStudentsToSessions(Exam exam, List<Student> students, Schedule schedule) {
        List<ExamSession> sessions = new ArrayList<>();
        List<Student> remaining = new ArrayList<>(students);

        int attempts = 0;
        final int MAX_ATTEMPTS = availableTimeSlots.size() * 2;

        while (!remaining.isEmpty() && attempts < MAX_ATTEMPTS) {
            attempts++;

            TimeSlot bestSlot = findBestTimeSlot(remaining);
            if (bestSlot == null) {
                System.out.println("UYARI: " + exam.getName() + " için uygun time slot bulunamadı. Kalan: " + remaining.size());
                break;
            }

            int requiredCapacity = Math.min(remaining.size(), getMaxRoomCapacity());
            Room bestRoom = findBestRoom(requiredCapacity);

            if (bestRoom == null || bestRoom.getCapacity() < 1) {
                System.out.println("UYARI: Uygun oda bulunamadı için " + exam.getName());
                break;
            }

            ExamSession session = new ExamSession(
                    nextSessionId++,
                    exam,
                    bestSlot,
                    bestRoom
            );

            List<Student> assignable = new ArrayList<>();
            for (Student s : remaining) {
                if (!s.hasExamOverlap(session) &&
                        s.getDailyExamCount(bestSlot.getDate()) < maxExamsPerDay) {
                    assignable.add(s);
                    if (assignable.size() >= bestRoom.getCapacity()) break;
                }
            }

            if (assignable.isEmpty()) {
                continue;
            }

            for (Student s : assignable) {
                session.assignStudent(s);
                remaining.remove(s);
            }

            sessions.add(session);
        }

        if (!remaining.isEmpty()) {
            System.out.println("HATA: " + exam.getName() + " sınavı için " + remaining.size() +
                    " öğrenci yerleştirilemedi!");
        }

        return sessions;
    }

    private TimeSlot findBestTimeSlot(List<Student> students) {
        return availableTimeSlots.stream()
                .min(Comparator.comparingInt(slot -> {
                    int conflictCount = 0;
                    for (Student s : students) {
                        if (s.hasExamOverlap(new ExamSession(0, null, slot, null)) ||
                                s.getDailyExamCount(slot.getDate()) >= maxExamsPerDay) {
                            conflictCount++;
                        }
                    }
                    return conflictCount;
                }))
                .orElse(null);
    }

    private Room findBestRoom(int minRequiredCapacity) {
        return availableRooms.stream()
                .filter(r -> r.getCapacity() >= minRequiredCapacity)
                .min(Comparator.comparingInt(Room::getCapacity))
                .orElseGet(() ->
                        availableRooms.stream()
                                .max(Comparator.comparingInt(Room::getCapacity))
                                .orElse(null)
                );
    }

    private int getMaxRoomCapacity() {
        return availableRooms.stream()
                .mapToInt(Room::getCapacity)
                .max()
                .orElse(0);
    }
}