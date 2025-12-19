package com.examscheduler.scheduler;

import com.examscheduler.constraint.MaxExamsPerDayConstraint;
import com.examscheduler.constraint.NoConsecutiveExamsConstraint;
import com.examscheduler.constraint.NoOverlapConstraint;
import com.examscheduler.constraint.RoomCapacityConstraint;
import com.examscheduler.constraint.Constraint;
import com.examscheduler.entity.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public Schedule generateSchedule(List<Course> courses, List<Exam> ignoredExams) {
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

        List<Exam> examsToSchedule = new ArrayList<>();
        if (courses != null) {
            for (Course course : courses) {
                if (course == null) continue;
                String examId = course.getCourseCode() != null && !course.getCourseCode().isBlank()
                        ? course.getCourseCode()
                        : course.getCourseId();
                int duration = course.getExamDurationMinutes() > 0 ? course.getExamDurationMinutes() : 120;
                examsToSchedule.add(new Exam(examId, course, "Course Exam", duration));
            }
        }

        // Prioritization: Sort exams by number of students in descending order
        examsToSchedule.sort(Comparator.comparingInt((Exam exam) -> exam.getEnrolledStudents().size()).reversed());

        for (Exam exam : examsToSchedule) {
            List<Student> enrolledStudents = exam.getEnrolledStudents();
            if (enrolledStudents.isEmpty()) {
                schedule.addSchedulingNote("Skipped exam " + exam.getExamId() + ": No enrolled students.");
                continue;
            }

            List<ExamSession> sessions = findAndCreateExamSessions(exam, enrolledStudents, schedule);

            if (sessions.isEmpty()) {
                // Detailed logging is now handled inside findAndCreateExamSessions
            } else {
                for (ExamSession session : sessions) {
                    exam.addExamSession(session);
                    schedule.addExamSession(session);
                }
            }
        }

        List<String> violations = schedule.validate();
        if (!violations.isEmpty()) {
            attemptToResolveViolations(schedule, violations);
        }

        return schedule;
    }

    private List<ExamSession> findAndCreateExamSessions(Exam exam, List<Student> students, Schedule schedule) {
        Map<String, Integer> failureReasons = new HashMap<>();

        for (TimeSlot timeSlot : availableTimeSlots) {
            // Step 1: Check for student conflicts using a temporary session, as requested.
            boolean studentConflict = false;
            ExamSession proposedSession = new ExamSession(null, exam, timeSlot, null);
            for (Student student : students) {
                if (student.hasExamOverlap(proposedSession)) {
                    studentConflict = true;
                    break;
                }
            }
            if (studentConflict) {
                failureReasons.merge("STUDENT_CONFLICT", 1, Integer::sum);
                continue;
            }

            // Step 1.5: Check for max exams per day constraint
            boolean maxExamsConflict = false;
            for (Student student : students) {
                if (student.getDailyExamCount(timeSlot.getDate()) >= this.maxExamsPerDay) {
                    maxExamsConflict = true;
                    break;
                }
            }
            if (maxExamsConflict) {
                failureReasons.merge("MAX_EXAMS_PER_DAY", 1, Integer::sum);
                continue; // Try the next time slot
            }

            // Step 2: Find available rooms for this time slot.
            List<Room> availableRoomsForSlot = new ArrayList<>();
            for (Room room : this.availableRooms) {
                if (isRoomAvailable(room, timeSlot, new ArrayList<>(), schedule)) {
                    availableRoomsForSlot.add(room);
                }
            }

            // Step 3: Try to find a single room that fits.
            List<Room> roomsThatFit = new ArrayList<>();
            for (Room room : availableRoomsForSlot) {
                if (room.getCapacity() >= students.size()) {
                    roomsThatFit.add(room);
                }
            }

            if (!roomsThatFit.isEmpty()) {
                // Shuffle the list of suitable rooms to avoid 'first-fit' bias.
                Collections.shuffle(roomsThatFit);
                Room selectedRoom = roomsThatFit.get(0); // Pick the first from the shuffled list.

                String sessionId = exam.getExamId() + "-S1";
                ExamSession session = new ExamSession(sessionId, exam, timeSlot, selectedRoom);
                for (Student student : students) {
                    session.assignStudent(student);
                    student.assignExamSession(session);
                }
                return List.of(session);
            }

            // Step 4: If no single room is big enough, try to fit into multiple rooms.
            // Sort rooms largest to smallest for efficient packing.
            availableRoomsForSlot.sort(Comparator.comparingInt(Room::getCapacity).reversed());
            long totalCapacity = availableRoomsForSlot.stream().mapToLong(Room::getCapacity).sum();
            if (totalCapacity >= students.size()) {
                List<ExamSession> sessions = new ArrayList<>();
                List<Student> remainingStudents = new ArrayList<>(students);
                int sessionIndex = 0;

                for (Room room : availableRoomsForSlot) {
                    if (remainingStudents.isEmpty()) break;

                    String sessionId = exam.getExamId() + "-S" + (++sessionIndex);
                    ExamSession session = new ExamSession(sessionId, exam, timeSlot, room);

                    List<Student> studentsToAssign = new ArrayList<>(remainingStudents.subList(0, Math.min(remainingStudents.size(), room.getCapacity())));
                    for (Student student : studentsToAssign) {
                        session.assignStudent(student);
                        student.assignExamSession(session);
                    }

                    remainingStudents.removeAll(studentsToAssign);
                    sessions.add(session);
                }
                return sessions;
            } else {
                failureReasons.merge("INSUFFICIENT_CAPACITY", 1, Integer::sum);
            }
        }
        
        // If loop finishes, scheduling failed for this exam. Log detailed reason.
        String reason = "Could not find a suitable time/room for exam " + exam.getExamId() + ". Failures: " + failureReasons;
        schedule.addSchedulingNote(reason);
        return new ArrayList<>();
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
