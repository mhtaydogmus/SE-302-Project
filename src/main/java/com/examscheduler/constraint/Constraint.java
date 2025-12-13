package com.examscheduler.constraint;

import com.examscheduler.entity.Schedule;

import java.util.List;

public interface Constraint {
    List<String> validate(Schedule schedule);

    String getName();

    String getDescription();
}
