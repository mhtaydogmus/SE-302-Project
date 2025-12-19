package com.examscheduler.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public class TimeSlot {
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    public TimeSlot() {
    }

    public TimeSlot(LocalDate date, LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null || date == null) {
            throw new IllegalArgumentException("Date, startTime and endTime cannot be null");
        }
        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * İki time slot'un çakışıp çakışmadığını kontrol eder.
     * Aynı gün değilse → false
     * Zaman aralıkları kesişiyorsa → true
     */
    public boolean overlaps(TimeSlot other) {
        if (other == null) {
            return false;
        }

        // Farklı günlerdeyse çakışma yok
        if (!this.date.equals(other.date)) {
            return false;
        }

        // Klasik interval overlap formülü:
        // A: this.start < other.end  AND  other.start < this.end
        return this.startTime.isBefore(other.endTime) &&
                other.startTime.isBefore(this.endTime);
    }

    /**
     * Bu slotun toplam süresini dakika olarak döner (opsiyonel faydalı metod)
     */
    public long getDurationMinutes() {
        return java.time.Duration.between(startTime, endTime).toMinutes();
    }

    // Getter & Setter
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeSlot timeSlot = (TimeSlot) o;
        return Objects.equals(date, timeSlot.date) &&
                Objects.equals(startTime, timeSlot.startTime) &&
                Objects.equals(endTime, timeSlot.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, startTime, endTime);
    }

    @Override
    public String toString() {
        return "TimeSlot{" +
                "date=" + date +
                ", start=" + startTime +
                ", end=" + endTime +
                ", duration=" + getDurationMinutes() + "min" +
                '}';
    }
}