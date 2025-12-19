package com.examscheduler.scheduler;

import com.examscheduler.constraint.MaxExamsPerDayConstraint;
import com.examscheduler.constraint.NoConsecutiveExamsConstraint;
import com.examscheduler.constraint.NoOverlapConstraint;
import com.examscheduler.constraint.RoomCapacityConstraint;
import com.examscheduler.constraint.Constraint;
import com.examscheduler.entity.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Scheduler {
    private List<Room> availableRooms;
    private List<TimeSlot> availableTimeSlots;
    private int maxExamsPerDay;
    private List<Constraint> customConstraints;

    public Scheduler() {
        this.availableRooms = new ArrayList<>();
        this.availableTimeSlots = new ArrayList<>();
        this.maxExamsPerDay = 2;
        this.customConstraints = new ArrayList<>();
    }

    public Scheduler(List<Room> availableRooms, List<TimeSlot> availableTimeSlots, int maxExamsPerDay) {
        this.availableRooms = availableRooms != null ? new ArrayList<>(availableRooms) : new ArrayList<>();
        this.availableTimeSlots = availableTimeSlots != null ? new ArrayList<>(availableTimeSlots) : new ArrayList<>();
        this.maxExamsPerDay = maxExamsPerDay;
        this.customConstraints = new ArrayList<>();
    }

    public Scheduler(List<Room> availableRooms, List<TimeSlot> availableTimeSlots, int maxExamsPerDay, List<Constraint> customConstraints) {
        this.availableRooms = availableRooms != null ? new ArrayList<>(availableRooms) : new ArrayList<>();
        this.availableTimeSlots = availableTimeSlots != null ? new ArrayList<>(availableTimeSlots) : new ArrayList<>();
        this.maxExamsPerDay = maxExamsPerDay;
        this.customConstraints = customConstraints != null ? new ArrayList<>(customConstraints) : new ArrayList<>();
    }

    public Schedule generateSchedule(List<Course> courses, List<Exam> exams) {
        Schedule schedule = new Schedule(
            UUID.randomUUID().toString(),
            "Generated Exam Schedule",
            LocalDate.now(),
            LocalDate.now().plusMonths(1)
        );

        schedule.addConstraint(new NoOverlapConstraint());
        schedule.addConstraint(new MaxExamsPerDayConstraint(maxExamsPerDay));
        schedule.addConstraint(new NoConsecutiveExamsConstraint());
        schedule.addConstraint(new RoomCapacityConstraint());
        for (Constraint constraint : customConstraints) {
            schedule.addConstraint(constraint);
        }

        for (Exam exam : exams) {
            List<Student> enrolledStudents = exam.getEnrolledStudents();

            if (enrolledStudents.isEmpty()) {
                continue;
            }

            List<ExamSession> sessions = createExamSessions(exam, enrolledStudents, schedule);

            for (ExamSession session : sessions) {
                exam.addExamSession(session);
                schedule.addExamSession(session);
            }
        }

        List<String> violations = schedule.validate();

        if (!violations.isEmpty()) {
            attemptToResolveViolations(schedule, violations);
        }

        return schedule;
    }


    private List<ExamSession> createExamSessions(Exam exam, List<Student> students, Schedule schedule) {
        List<ExamSession> sessions = new ArrayList<>();
        List<Student> remainingStudents = new ArrayList<>(students);

        int sessionIndex = 0;

        while (!remainingStudents.isEmpty()) {
            TimeSlot selectedTimeSlot = selectTimeSlot(remainingStudents, schedule, sessions);
            Room selectedRoom = selectAvailableRoom(remainingStudents, selectedTimeSlot, sessions, schedule);

            if (selectedTimeSlot == null || selectedRoom == null) {
                System.err.println("WARNING: Could not schedule all students for exam " + exam.getExamId());
                break;
            }

            String sessionId = exam.getExamId() + "-S" + (++sessionIndex);
            ExamSession session = new ExamSession(sessionId, exam, selectedTimeSlot, selectedRoom);

            List<Student> studentsToAssign = new ArrayList<>();
            for (Student student : new ArrayList<>(remainingStudents)) {
                if (!student.hasExamOverlap(session) &&
                    student.getDailyExamCount(session.getTimeSlot().getDate()) < maxExamsPerDay &&
                    !hasConsecutiveExam(student, session)) {
                    studentsToAssign.add(student);
                    if (studentsToAssign.size() >= selectedRoom.getCapacity()) {
                        break;
                    }
                }
            }

            if (studentsToAssign.isEmpty()) {
                System.err.println("WARNING: No students could be assigned to a new session for exam " +
                                   exam.getExamId() + " (likely due to conflicts)");
                break;
            }

            for (Student student : studentsToAssign) {
                session.assignStudent(student);
                student.assignExamSession(session);
                remainingStudents.remove(student);
            }

            sessions.add(session);
        }

        return sessions;
    }

    private boolean hasConsecutiveExam(Student student, ExamSession newSession) {
        if (student == null || newSession == null || newSession.getTimeSlot() == null) {
            return false;
        }

        for (ExamSession existingSession : student.getAssignedSessions()) {
            if (existingSession.getTimeSlot() == null) {
                continue;
            }
            if (!newSession.getTimeSlot().getDate().equals(existingSession.getTimeSlot().getDate())) {
                continue;
            }
            if (newSession.getTimeSlot().getStartTime().equals(existingSession.getTimeSlot().getEndTime()) ||
                newSession.getTimeSlot().getEndTime().equals(existingSession.getTimeSlot().getStartTime())) {
                return true;
            }
        }

        return false;
    }

    private TimeSlot selectTimeSlot(List<Student> students, Schedule schedule, List<ExamSession> pendingSessions) {
        for (TimeSlot timeSlot : availableTimeSlots) {
            boolean suitableForMost = true;

            int conflictCount = 0;
            for (Student student : students) {
                if (student.getDailyExamCount(timeSlot.getDate()) >= maxExamsPerDay) {
                    conflictCount++;
                }
            }

            if (conflictCount < students.size() * 0.2) {
                if (!hasFreeRoom(timeSlot, students.size(), pendingSessions, schedule)) {
                    continue;
                }
                return new TimeSlot(timeSlot.getDate(), timeSlot.getStartTime(), timeSlot.getEndTime());
            }
        }

        if (!availableTimeSlots.isEmpty()) {
            TimeSlot ts = availableTimeSlots.get(0);
            if (!hasFreeRoom(ts, students.size(), pendingSessions, schedule)) {
                return null;
            }
            return new TimeSlot(ts.getDate(), ts.getStartTime(), ts.getEndTime());
        }

        return null;
    }


    private Room selectAvailableRoom(List<Student> students, TimeSlot timeSlot, List<ExamSession> pendingSessions, Schedule schedule) {
        if (timeSlot == null) {
            return null;
        }

        int requiredCapacity = students.size();
        Room bestRoom = null;

        for (Room room : availableRooms) {
            if (!isRoomAvailable(room, timeSlot, pendingSessions, schedule)) {
                continue;
            }

            if (room.getCapacity() >= requiredCapacity) {
                if (bestRoom == null || room.getCapacity() < bestRoom.getCapacity()) {
                    bestRoom = room;
                }
            } else if (bestRoom == null) {
                bestRoom = room; // fallback to any available room if none meet capacity
            }
        }

        return bestRoom;
    }

    private boolean hasFreeRoom(TimeSlot timeSlot, int requiredCapacity, List<ExamSession> pendingSessions, Schedule schedule) {
        for (Room room : availableRooms) {
            if (isRoomAvailable(room, timeSlot, pendingSessions, schedule)) {
                return true;
            }
        }
        return false;
    }

    private boolean isRoomAvailable(Room room, TimeSlot timeSlot, List<ExamSession> pendingSessions, Schedule schedule) {
        if (room == null || timeSlot == null) {
            return false;
        }

        for (ExamSession session : pendingSessions) {
            if (session.getRoom() != null &&
                session.getRoom().equals(room) &&
                session.getTimeSlot() != null &&
                session.getTimeSlot().overlaps(timeSlot)) {
                return false;
            }
        }

        if (schedule != null) {
            for (ExamSession session : schedule.getExamSessions()) {
                if (session.getRoom() != null &&
                    session.getRoom().equals(room) &&
                    session.getTimeSlot() != null &&
                    session.getTimeSlot().overlaps(timeSlot)) {
                    return false;
                }
            }
        }

        return true;
    }


    private void attemptToResolveViolations(Schedule schedule, List<String> violations) {
        System.err.println("Schedule has " + violations.size() + " violations:");
        for (String violation : violations) {
            System.err.println("  - " + violation);
        }
    }

    public List<Room> getAvailableRooms() {
        return new ArrayList<>(availableRooms);
    }

    public void setAvailableRooms(List<Room> availableRooms) {
        this.availableRooms = availableRooms != null ? new ArrayList<>(availableRooms) : new ArrayList<>();
    }

    public void addRoom(Room room) {
        if (room != null && !availableRooms.contains(room)) {
            availableRooms.add(room);
        }
    }

    public List<TimeSlot> getAvailableTimeSlots() {
        return new ArrayList<>(availableTimeSlots);
    }

    public void setAvailableTimeSlots(List<TimeSlot> availableTimeSlots) {
        this.availableTimeSlots = availableTimeSlots != null ? new ArrayList<>(availableTimeSlots) : new ArrayList<>();
    }

    public void addTimeSlot(TimeSlot timeSlot) {
        if (timeSlot != null && !availableTimeSlots.contains(timeSlot)) {
            availableTimeSlots.add(timeSlot);
        }
    }

    public int getMaxExamsPerDay() {
        return maxExamsPerDay;
    }

    public void setMaxExamsPerDay(int maxExamsPerDay) {
        this.maxExamsPerDay = maxExamsPerDay;
    }

    public void addConstraint(Constraint constraint) {
        if (constraint != null && !customConstraints.contains(constraint)) {
            customConstraints.add(constraint);
        }
    }

    public List<Constraint> getCustomConstraints() {
        return new ArrayList<>(customConstraints);
    }

    public void setCustomConstraints(List<Constraint> customConstraints) {
        this.customConstraints = customConstraints != null ? new ArrayList<>(customConstraints) : new ArrayList<>();
    }
}
