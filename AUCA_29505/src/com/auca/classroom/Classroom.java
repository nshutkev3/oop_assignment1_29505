package com.auca.classroom;

import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;

/**
 * A physical classroom.
 */
public class Classroom implements Identifiable, Registrable {

    private String classroomId;
    private String buildingName;
    private String roomNumber;
    private int capacity;
    private int floorNumber;
    private boolean available;

    /** Constructor: initialises every attribute of this type. */
    public Classroom(String classroomId, String buildingName, String roomNumber, int capacity, int floorNumber, boolean available) {
        this.classroomId = classroomId;
        this.buildingName = buildingName;
        this.roomNumber = roomNumber;
        this.capacity = capacity;
        this.floorNumber = floorNumber;
        this.available = available;
    }

    public String getClassroomId() {
        return classroomId;
    }

    public void setClassroomId(String classroomId) {
        this.classroomId = classroomId;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(int floorNumber) {
        this.floorNumber = floorNumber;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String getId() {
        return classroomId;
    }

    @Override
    public String getDisplayName() {
        return buildingName + " " + roomNumber;
    }

    @Override
    public boolean isActive() {
        return available;
    }

    @Override
    public void validate() throws ValidationException {
        Registrable.requireText("Classroom ID", classroomId, MIN_ID_LENGTH, MAX_ID_LENGTH);
        Registrable.requireText("Building name", buildingName, 2, 60);
        Registrable.requireText("Room number", roomNumber, 1, 10);
        if (capacity <= 0) {
            throw new ValidationException("Capacity must be greater than zero.");
        }
        if (floorNumber < 0) {
            throw new ValidationException("Floor number must be zero or greater.");
        }
    }

    @Override
    public void printDetails() {
        System.out.println("---- Classroom ----");
        System.out.println("Classroom ID      : " + (classroomId));
        System.out.println("Building          : " + (buildingName));
        System.out.println("Room number       : " + (roomNumber));
        System.out.println("Capacity          : " + (capacity));
        System.out.println("Floor             : " + (floorNumber));
        System.out.println("Available         : " + (available ? "YES" : "NO"));
    }

    @Override
    public String getRegistrationSummary() {
        return "Classroom [" + classroomId + "] " + buildingName + " " + roomNumber + " (capacity " + capacity + ")";
    }

    @Override
    public String toString() {
        return getRegistrationSummary();
    }
}
