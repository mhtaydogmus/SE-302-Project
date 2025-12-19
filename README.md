# Student Exam Scheduling System - JavaFX GUI Skeleton

This repository contains the **basic JavaFX GUI skeleton** requested in *Meeting Report 4 (Task 3)*:
- Main JavaFX project structure
- Main window layout with menu navigation
- Placeholder views for:
  - Student management
  - Course management
  - Schedule generation screen
- Navigation between views is functional

## Requirements
- Java 17+

## Run (Maven)
From the project root:
```bash
mvn clean javafx:run
```

## Notes
This GUI is intentionally minimal. It is expected to be connected to:
- Entity classes (Student, Course, ...)
- Scheduling logic (Scheduler.generateSchedule)
- Constraint validation (Schedule.validate)
in later implementation tasks.
