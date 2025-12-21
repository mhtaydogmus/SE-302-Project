package com.examscheduler.constraint;

import com.examscheduler.entity.ExamSession;
import com.examscheduler.entity.Room;
import com.examscheduler.entity.Schedule;

import java.util.ArrayList;
import java.util.List;

public class RoomCapacityConstraint extends Constraint {

    @Override
    public List<String> validate(Schedule schedule) {
        List<String> violations = new ArrayList<>();

        if (schedule == null) {
            return violations;
        }

        for (ExamSession session : schedule.getExamSessions()) {
            Room room = session.getRoom();
            int assignedCount = session.getAssignedStudents().size();
            if (room == null) {
                violations.add(String.format(
                        "ROOM CAPACITY VIOLATION: Session %s has no room assigned.",
                        session.getSessionId()
                ));
                continue;
            }
            if (assignedCount > room.getCapacity()) {
                violations.add(String.format(
                        "ROOM CAPACITY VIOLATION: Session %s exceeds room capacity (%d/%d).",
                        session.getSessionId(),
                        assignedCount,
                        room.getCapacity()
                ));
            }
        }

        return violations;
    }
}
