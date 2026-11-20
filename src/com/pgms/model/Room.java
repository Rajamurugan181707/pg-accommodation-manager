package com.pgms.model;

import com.pgms.util.Check;

/** ROOM module entity. Each bed in the room is rented separately. */
public class Room implements Billable {

    private int id;
    private String roomNo;
    private int floor;
    private RoomType type;
    private int occupied;
    private double rentPerBed;
    private boolean ac;

    public Room(int id, String roomNo, int floor, RoomType type,
                int occupied, double rentPerBed, boolean ac) {
        this.id = id;
        this.roomNo = roomNo;
        this.floor = floor;
        this.type = type;
        this.occupied = occupied;
        setRentPerBed(rentPerBed);
        this.ac = ac;
    }

    public int getCapacity() {
        return type.beds();
    }

    public int vacantBeds() {
        return getCapacity() - occupied;
    }

    public boolean isFull() {
        return vacantBeds() == 0;
    }

    @Override
    public double monthlyCharge() {
        return rentPerBed;
    }

    @Override
    public String billLabel() {
        return "Room " + roomNo + " (" + type.name().toLowerCase() + (ac ? ", AC" : "") + ", per bed)";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public int getFloor() {
        return floor;
    }

    public RoomType getType() {
        return type;
    }

    public int getOccupied() {
        return occupied;
    }

    public double getRentPerBed() {
        return rentPerBed;
    }

    public final void setRentPerBed(double rentPerBed) {
        this.rentPerBed = Check.money(rentPerBed, false, "Rent");
    }

    public boolean hasAc() {
        return ac;
    }
}
