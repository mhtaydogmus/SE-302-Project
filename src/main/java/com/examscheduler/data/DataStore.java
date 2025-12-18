package com.examscheduler.data;

import com.examscheduler.entity.*;

import java.util.ArrayList;
import java.util.List;

public class DataStore {
    public static final List<Student> students = new ArrayList<>();
    public static final List<Course> courses = new ArrayList<>();
    public static final List<Room> rooms = new ArrayList<>();
    public static final List<TimeSlot> timeSlots = new ArrayList<>();
}
