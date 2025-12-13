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

        Room room = new Room("R101", "Main Hall", 50);

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

        Course course = new Course("CS101", "Introduction to Programming", "CS-101", 4);
        Student student1 = new Student("S001", "John", "Doe", "john.doe@example.com");
        Student student2 = new Student("S002", "Jane", "Smith", "jane.smith@example.com");

        Enrollment enrollment1 = new Enrollment("E001", student1, course);
        Enrollment enrollment2 = new Enrollment("E002", student2, course);

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

        Student student = new Student("S003", "Alice", "Johnson", "alice.j@example.com");
        Course course = new Course("MATH201", "Calculus II", "MATH-201", 4);
        Exam exam = new Exam("EX001", course, "FINAL", 120);

        LocalDate examDate = LocalDate.of(2025, 6, 15);
        TimeSlot slot1 = new TimeSlot(examDate, LocalTime.of(9, 0), LocalTime.of(11, 0));
        TimeSlot slot2 = new TimeSlot(examDate, LocalTime.of(13, 0), LocalTime.of(15, 0));
        TimeSlot slot3 = new TimeSlot(examDate, LocalTime.of(14, 0), LocalTime.of(16, 0));

        Room room = new Room("R201", "Room 201", 30);

        ExamSession session1 = new ExamSession("ES001", exam, slot1, room);
        ExamSession session2 = new ExamSession("ES002", exam, slot2, room);
        ExamSession session3 = new ExamSession("ES003", exam, slot3, room);

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

        Course course = new Course("PHYS301", "Quantum Mechanics", "PHYS-301", 3);
        Exam exam = new Exam("EX002", course, "MIDTERM", 90);
        Room room = new Room("R301", "Lab 301", 2);
        TimeSlot slot = new TimeSlot(LocalDate.of(2025, 6, 20), LocalTime.of(10, 0), LocalTime.of(11, 30));

        ExamSession session = new ExamSession("ES004", exam, slot, room);

        Student student1 = new Student("S004", "Bob", "Williams", "bob.w@example.com");
        Student student2 = new Student("S005", "Carol", "Brown", "carol.b@example.com");
        Student student3 = new Student("S006", "David", "Miller", "david.m@example.com");

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

        Schedule schedule = new Schedule("SCH001", "Final Exams Schedule",
                LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 30));

        schedule.addConstraint(new NoOverlapConstraint());
        schedule.addConstraint(new MaxExamsPerDayConstraint(2));

        Student student = new Student("S007", "Eve", "Davis", "eve.d@example.com");
        Course course1 = new Course("ENG101", "English Literature", "ENG-101", 3);
        Course course2 = new Course("HIST101", "World History", "HIST-101", 3);

        Exam exam1 = new Exam("EX003", course1, "FINAL", 120);
        Exam exam2 = new Exam("EX004", course2, "FINAL", 120);

        Room room = new Room("R401", "Room 401", 50);
        LocalDate examDate = LocalDate.of(2025, 6, 15);

        TimeSlot slot1 = new TimeSlot(examDate, LocalTime.of(9, 0), LocalTime.of(11, 0));
        TimeSlot slot2 = new TimeSlot(examDate, LocalTime.of(10, 0), LocalTime.of(12, 0));

        ExamSession session1 = new ExamSession("ES005", exam1, slot1, room);
        ExamSession session2 = new ExamSession("ES006", exam2, slot2, room);

        session1.assignStudent(student);
        session2.assignStudent(student);
        student.assignExamSession(session1);
        student.assignExamSession(session2);

        schedule.addExamSession(session1);
        schedule.addExamSession(session2);

        List<String> violations = schedule.validate();

        assert !schedule.isValid() : "Schedule should be invalid (overlapping sessions)";
        assert violations.size() > 0 : "Should have at least 1 violation";

        System.out.println("  ✓ Constraint validation working correctly");
        System.out.println("  Schedule valid: " + schedule.isValid());
        System.out.println("  Violations found: " + violations.size());
        for (String violation : violations) {
            System.out.println("    - " + violation);
        }
        System.out.println();
    }

    private static void testSchedulerBasic() {
        System.out.println("TEST: Scheduler Basic Functionality");

        Course course1 = new Course("CS301", "Data Structures", "CS-301", 4);
        Course course2 = new Course("CS302", "Algorithms", "CS-302", 4);

        Student student1 = new Student("S008", "Frank", "Wilson", "frank.w@example.com");
        Student student2 = new Student("S009", "Grace", "Taylor", "grace.t@example.com");

        Enrollment e1 = new Enrollment("E003", student1, course1);
        Enrollment e2 = new Enrollment("E004", student2, course1);
        Enrollment e3 = new Enrollment("E005", student1, course2);

        course1.addEnrollment(e1);
        course1.addEnrollment(e2);
        course2.addEnrollment(e3);
        student1.addEnrollment(e1);
        student2.addEnrollment(e2);
        student1.addEnrollment(e3);

        Exam exam1 = new Exam("EX005", course1, "FINAL", 120);
        Exam exam2 = new Exam("EX006", course2, "FINAL", 120);

        List<Exam> exams = new ArrayList<>();
        exams.add(exam1);
        exams.add(exam2);

        List<Course> courses = new ArrayList<>();
        courses.add(course1);
        courses.add(course2);

        Scheduler scheduler = new Scheduler();

        scheduler.addRoom(new Room("R501", "Room 501", 30));
        scheduler.addRoom(new Room("R502", "Room 502", 40));

        LocalDate date1 = LocalDate.of(2025, 6, 10);
        LocalDate date2 = LocalDate.of(2025, 6, 11);

        scheduler.addTimeSlot(new TimeSlot(date1, LocalTime.of(9, 0), LocalTime.of(11, 0)));
        scheduler.addTimeSlot(new TimeSlot(date1, LocalTime.of(13, 0), LocalTime.of(15, 0)));
        scheduler.addTimeSlot(new TimeSlot(date2, LocalTime.of(9, 0), LocalTime.of(11, 0)));

        Schedule schedule = scheduler.generateSchedule(courses, exams);

        assert schedule != null : "Schedule should be generated";
        assert schedule.getExamSessions().size() > 0 : "Schedule should have exam sessions";

        System.out.println("  ✓ Scheduler created successfully");
        System.out.println("  ✓ Schedule generated with " + schedule.getExamSessions().size() + " sessions");
        System.out.println("  Schedule: " + schedule);
        System.out.println("  Schedule valid: " + schedule.isValid());

        if (!schedule.isValid()) {
            List<String> violations = schedule.getValidationMessages();
            System.out.println("  Violations: " + violations.size());
            for (String violation : violations) {
                System.out.println("    - " + violation);
            }
        }

        System.out.println();
    }
}
