package com.examscheduler.entity;

import java.util.Objects;

public class Room {
    private int roomId;         // ← Design Document'a uygun olarak int yapıldı
    private String roomName;
    private int capacity;

    public Room() {
        // Default constructor (JavaFX tabloları ve dialoglar için gerekli)
    }

    public Room(int roomId, String roomName, int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity cannot be negative");
        }
        this.roomId = roomId;
        this.roomName = roomName;
        this.capacity = capacity;
    }

    /**
     * Belirli sayıda öğrenci için kapasite yeterli mi?
     */
    public boolean hasCapacity(int numberOfStudents) {
        return numberOfStudents >= 0 && this.capacity >= numberOfStudents;
    }

    // Getter & Setter
    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity cannot be negative");
        }
        this.capacity = capacity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return roomId == room.roomId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomId);
    }

    /**
     * Görünüm için gerçekçi format: "Amfi A (101) - Capacity: 120"
     */
    @Override
    public String toString() {
        return roomName + " (" + roomId + ") - Capacity: " + capacity;
    }
}