package edu.ieu.se302.examscheduler.ui.util;

import com.examscheduler.entity.TimeSlot;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TimeSlotGenerator {

    /**
     * Generates a list of TimeSlot objects for a given date range and time range.
     *
     * @param start The start date (inclusive).
     * @param end The end date (inclusive).
     * @param dayStart The start time for exams each day.
     * @param dayEnd The end time for exams each day.
     * @param examDuration The duration of a single exam in minutes.
     * @param bufferMinutes The buffer time between exams in minutes.
     * @return A list of generated TimeSlot objects.
     */
    public static List<TimeSlot> generateBulkTimeSlots(
            LocalDate start, LocalDate end,
            LocalTime dayStart, LocalTime dayEnd,
            int examDuration, int bufferMinutes) {

        List<TimeSlot> generatedSlots = new ArrayList<>();

        if (start == null || end == null || dayStart == null || dayEnd == null || examDuration <= 0) {
            return generatedSlots;
        }

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            LocalTime currentTime = dayStart;
            while (true) {
                LocalTime startTime = currentTime;
                LocalTime endTime = startTime.plusMinutes(examDuration);

                if (endTime.isAfter(dayEnd)) {
                    break; // This exam slot would end after the allowed day end time
                }

                generatedSlots.add(new TimeSlot(date, startTime, endTime));

                currentTime = endTime.plusMinutes(bufferMinutes);
            }
        }

        return generatedSlots;
    }
}
