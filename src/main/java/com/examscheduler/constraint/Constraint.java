package com.examscheduler.constraint;

import com.examscheduler.entity.Schedule;

import java.util.List;

public abstract class Constraint {
    public abstract List<String> validate(Schedule schedule);
}
