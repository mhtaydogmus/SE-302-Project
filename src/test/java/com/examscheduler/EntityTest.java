package com.examscheduler;

import com.examscheduler.constraint.MaxExamsPerDayConstraint;
import com.examscheduler.constraint.NoOverlapConstraint;
import com.examscheduler.entity.*;
import com.examscheduler.scheduler.Scheduler;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class EntityTest {

    public static void main(String[] args) {
        System.out.println("=== Starting Entity Tests ===\n");

        testTimeSlotCreationAndOverlap();
        testRoomCreation();
        testCourseAndEnrollment();
        testStudentMethods();
        testExamSessionAssignment();
        testScheduleValidation();
        testSchedulerBasic();

        System.out.println("\n=== All Tests Completed ===");
    }

    private static void testTimeSlotCreationAndOverlap() {
        System.out.println("TEST: TimeSlot Creation and Overlap Detection");

        LocalDate date = LocalDate.of(2025, 6, 15);
        TimeSlot slot1 = new TimeSlot(date, LocalTime.of(9, 0), LocalTime.of(11, 0));
        TimeSlot slot2 = new TimeSlot(date, LocalTime.of(10, 0), LocalTime.of(12, 0));
        TimeSlot slot3 = new TimeSlot(date, LocalTime.of(13, 0), LocalTime.of(15, 0));
        TimeSlot slot4 = new TimeSlot(LocalDate.of(2025, 6, 16), LocalTime.of(9, 0), LocalTime.of(11, 0));

        assert slot1.overlaps(slot2) : "Slots 1 and 2 should overlap";
        assert !slot1.overlaps(slot3) : "Slots 1 and 3 should not overlap";
        assert !slot1.overlaps(slot4) : "Slots on different dates should not overlap";

        System.out.println("  ✓ TimeSlot creation successful");
        System.out.println("  ✓ Overlap detection working correctly");
        System.out.println("  Created: " + slot1);
        System.out.println();
    }

    private static void testRoomCreation() {
        System.out.println("TEST: Room Creation and Capacity");

        Room room = new Room(101, "Main Hall", 50);

        assert room.hasCapacity(30) : "Room should have capacity for 30 students";
        assert room.hasCapacity(50) : "Room should have capacity for 50 students";
        assert !room.hasCapacity(51) : "Room should not have capacity for 51 students";

        System.out.println("  ✓ Room creation successful");
        System.out.println("  ✓ Capacity checking working correctly");
        System.out.println("  Created: " + room);
        System.out.println();
    }

    private static void testCourseAndEnrollment() {
        System.out.println("TEST: Course and Enrollment Creation");

        Course course = new Course(101, "Introduction to Programming", "CS-101", 4);
        Student student1 = new Student(1001, "John", "Doe", "john.doe@example.com");
        Student student2 = new Student(1002, "Jane", "Smith", "jane.smith@example.com");

        Enrollment enrollment1 = new Enrollment(1, student1, course);
        Enrollment enrollment2 = new Enrollment(2, student2, course);

        course.addEnrollment(enrollment1);
        course.addEnrollment(enrollment2);
        student1.addEnrollment(enrollment1);
        student2.addEnrollment(enrollment2);

        assert course.getEnrollmentCount() == 2 : "Course should have 2 enrollments";
        assert course.getEnrolledStudents().size() == 2 : "Course should have 2 enrolled students";
        assert student1.getEnrolledCourses().size() == 1 : "Student1 should be enrolled in 1 course";

        System.out.println("  ✓ Course creation successful");
        System.out.println("  ✓ Enrollment linking working correctly");
        System.out.println("  Created: " + course);
        System.out.println("  Enrolled: " + student1.getFullName() + ", " + student2.getFullName());
        System.out.println();
    }

    private static void testStudentMethods() {
        System.out.println("TEST: Student Methods (getDailyExamCount, hasExamOverlap)");

        Student student = new Student(1003, "Alice", "Johnson", "alice.j@example.com");
        Course course = new Course(201, "Calculus II", "MATH-201", 4);
        Exam exam = new Exam(1, course, "FINAL", 120);

        LocalDate examDate = LocalDate.of(2025, 6, 15);
        TimeSlot slot1 = new TimeSlot(examDate, LocalTime.of(9, 0), LocalTime.of(11, 0));
        TimeSlot slot2 = new TimeSlot(examDate, LocalTime.of(13, 0), LocalTime.of(15, 0));
        TimeSlot slot3 = new TimeSlot(examDate, LocalTime.of(14, 0), LocalTime.of(16, 0));

        Room room = new Room(201, "Room 201", 30);

        ExamSession session1 = new ExamSession(1, exam, slot1, room);
        ExamSession session2 = new ExamSession(2, exam, slot2, room);
        ExamSession session3 = new ExamSession(3, exam, slot3, room);

        student.assignExamSession(session1);
        student.assignExamSession(session2);

        assert student.getDailyExamCount(examDate) == 2 : "Student should have 2 exams on " + examDate;
        assert !student.hasExamOverlap(session3) == false : "Session3 should overlap with session2";
        assert student.hasExamOverlap(session3) : "Session3 should overlap with session2";

        System.out.println("  ✓ getDailyExamCount working correctly");
        System.out.println("  ✓ hasExamOverlap detecting conflicts correctly");
        System.out.println("  Student: " + student);
        System.out.println("  Daily exam count on " + examDate + ": " + student.getDailyExamCount(examDate));
        System.out.println();
    }

    private static void testExamSessionAssignment() {
        System.out.println("TEST: ExamSession Student Assignment");

        Course course = new Course(301, "Quantum Mechanics", "PHYS-301", 3);
        Exam exam = new Exam(2, course, "MIDTERM", 90);
        Room room = new Room(301, "Lab 301", 2);
        TimeSlot slot = new TimeSlot(LocalDate.of(2025, 6, 20), LocalTime.of(10, 0), LocalTime.of(11, 30));

        ExamSession session = new ExamSession(4, exam, slot, room);

        Student student1 = new Student(1004, "Bob", "Williams", "bob.w@example.com");
        Student student2 = new Student(1005, "Carol", "Brown", "carol.b@example.com");
        Student student3 = new Student(1006, "David", "Miller", "david.m@example.com");

        assert session.assignStudent(student1) : "Should assign student1";
        assert session.assignStudent(student2) : "Should assign student2";
        assert !session.assignStudent(student3) : "Should not assign student3 (room full)";
        assert session.getAssignedStudents().size() == 2 : "Session should have 2 students";
        assert !session.hasAvailableCapacity() : "Session should be full";

        System.out.println("  ✓ Student assignment working correctly");
        System.out.println("  ✓ Capacity limits enforced correctly");
        System.out.println("  Session: " + session);
        System.out.println("  Assigned students: " + session.getAssignedStudents().size() + "/" + session.getMaxCapacity());
        System.out.println();
    }

    private static void testScheduleValidation() {
        System.out.println("TEST: Schedule Validation with Constraints");

        Schedule schedule = new Schedule();
        Scheduler scheduler = new Scheduler();
        scheduler.setMaxExamsPerDay(2);

        Student student = new Student(1007, "Eve", "Davis", "eve.d@example.com");
        Course course1 = new Course(401, "English Literature", "ENG-101", 3);
        Course course2 = new Course(402, "World History", "HIST-101", 3);

        Exam exam1 = new Exam(3, course1, "FINAL", 120);
        Exam exam2 = new Exam(4, course2, "FINAL", 120);

        Room room = new Room(401, "Room 401", 50);
        LocalDate examDate = LocalDate.of(2025, 6, 15);

        TimeSlot slot1 = new TimeSlot(examDate, LocalTime.of(9, 0), LocalTime.of(11, 0));
        TimeSlot slot2 = new TimeSlot(examDate, LocalTime.of(10, 0), LocalTime.of(12, 0));

        ExamSession session1 = new ExamSession(5, exam1, slot1, room);
        ExamSession session2 = new ExamSession(6, exam2, slot2, room);

        session1.assignStudent(student);
        session2.assignStudent(student);

        schedule.addExamSession(session1);
        schedule.addExamSession(session2);

        List<String> violations = scheduler.validateSchedule(schedule);

        assert violations.size() > 0 : "Should have at least 1 violation";

        System.out.println("  ✓ Constraint validation working correctly");
        System.out.println("  Violations found: " + violations.size());
        for (String violation : violations) {
            System.out.println("    - " + violation);
        }
        System.out.println();
    }

    private static void testSchedulerBasic() {
        System.out.println("TEST: Scheduler Basic Functionality");

        Course course1 = new Course(501, "Data Structures", "CS-301", 4);
        Course course2 = new Course(502, "Algorithms", "CS-302", 4);

        Student student1 = new Student(1008, "Frank", "Wilson", "frank.w@example.com");
        Student student2 = new Student(1009, "Grace", "Taylor", "grace.t@example.com");

        Enrollment e1 = new Enrollment(3, student1, course1);
        Enrollment e2 = new Enrollment(4, student2, course1);
        Enrollment e3 = new Enrollment(5, student1, course2);

        course1.addEnrollment(e1);
        course1.addEnrollment(e2);
        course2.addEnrollment(e3);
        student1.addEnrollment(e1);
        student2.addEnrollment(e2);
        student1.addEnrollment(e3);

        Exam exam1 = new Exam(5, course1, "FINAL", 120);
        Exam exam2 = new Exam(6, course2, "FINAL", 120);

        List<Exam> exams = new ArrayList<>();
        exams.add(exam1);
        exams.add(exam2);

        List<Course> courses = new ArrayList<>();
        courses.add(course1);
        courses.add(course2);

        Scheduler scheduler = new Scheduler();

        List<Room> rooms = new ArrayList<>();
        rooms.add(new Room(501, "Room 501", 30));
        rooms.add(new Room(502, "Room 502", 40));
        scheduler.setAvailableRooms(rooms);

        LocalDate date1 = LocalDate.of(2025, 6, 10);
        LocalDate date2 = LocalDate.of(2025, 6, 11);

        List<TimeSlot> timeSlots = new ArrayList<>();
        timeSlots.add(new TimeSlot(date1, LocalTime.of(9, 0), LocalTime.of(11, 0)));
        timeSlots.add(new TimeSlot(date1, LocalTime.of(13, 0), LocalTime.of(15, 0)));
        timeSlots.add(new TimeSlot(date2, LocalTime.of(9, 0), LocalTime.of(11, 0)));
        scheduler.setAvailableTimeSlots(timeSlots);

        Schedule schedule = scheduler.generateSchedule(courses, exams);

        assert schedule != null : "Schedule should be generated";
        assert schedule.getExamSessions().size() > 0 : "Schedule should have exam sessions";

        System.out.println("  ✓ Scheduler created successfully");
        System.out.println("  ✓ Schedule generated with " + schedule.getExamSessions().size() + " sessions");
        System.out.println("  Schedule: " + schedule);

        List<String> violations = scheduler.validateSchedule(schedule);
        boolean isValid = violations.isEmpty();
        System.out.println("  Schedule valid: " + isValid);

        if (!isValid) {
            System.out.println("  Violations: " + violations.size());
            for (String violation : violations) {
                System.out.println("    - " + violation);
            }
        }

        System.out.println();
    }
}
