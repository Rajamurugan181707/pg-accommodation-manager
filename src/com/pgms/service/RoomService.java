package com.pgms.service;

import com.pgms.dao.RoomDAO;
import com.pgms.ds.DynamicArray;
import com.pgms.model.Room;
import com.pgms.model.RoomType;

import java.sql.SQLException;
import java.util.Comparator;

/** ROOM module rules. Rooms are loaded into a DynamicArray, then sorted and searched. */
public class RoomService {

    public static final Comparator<Room> BY_ROOM_NO = new Comparator<Room>() {
        @Override
        public int compare(Room a, Room b) {
            return a.getRoomNo().compareToIgnoreCase(b.getRoomNo());
        }
    };

    public static final Comparator<Room> BY_RENT = new Comparator<Room>() {
        @Override
        public int compare(Room a, Room b) {
            int byRent = Double.compare(a.getRentPerBed(), b.getRentPerBed());
            return byRent != 0 ? byRent : BY_ROOM_NO.compare(a, b);
        }
    };

    private final RoomDAO dao = new RoomDAO();

    /** All rooms, sorted by room number (insertion sort). */
    public DynamicArray<Room> allRooms() throws SQLException {
        DynamicArray<Room> rooms = dao.findAll();
        rooms.sort(BY_ROOM_NO);
        return rooms;
    }

    /** Rooms with at least one free bed (linear search / filter). */
    public DynamicArray<Room> vacantRooms() throws SQLException {
        return allRooms().filter(new DynamicArray.Matcher<Room>() {
            @Override
            public boolean matches(Room r) {
                return !r.isFull();
            }
        });
    }

    public DynamicArray<Room> roomsSortedByRent() throws SQLException {
        DynamicArray<Room> rooms = dao.findAll();
        rooms.sort(BY_RENT);
        return rooms;
    }

    /** Binary search on the room-number-sorted array. Returns null if not found. */
    public Room findByRoomNo(final String roomNo) throws SQLException {
        DynamicArray<Room> sorted = allRooms();
        int index = sorted.binarySearch(new DynamicArray.KeyProbe<Room>() {
            @Override
            public int compareToKey(Room r) {
                return r.getRoomNo().compareToIgnoreCase(roomNo.trim());
            }
        });
        return index < 0 ? null : sorted.get(index);
    }

    public Room requireRoom(String roomNo) throws SQLException {
        Room room = findByRoomNo(roomNo);
        if (room == null) {
            throw new IllegalArgumentException("No room numbered " + roomNo);
        }
        return room;
    }

    public Room addRoom(String roomNo, int floor, RoomType type, double rentPerBed, boolean ac)
            throws SQLException {
        String no = roomNo.trim().toUpperCase();
        if (!no.matches("[A-Z0-9]{1,10}")) {
            throw new IllegalArgumentException("Room number can only have letters and digits (max 10)");
        }
        if (floor < 0 || floor > 50) {
            throw new IllegalArgumentException("Floor must be between 0 and 50");
        }
        if (findByRoomNo(no) != null) {
            throw new IllegalArgumentException("Room " + no + " already exists");
        }
        Room room = new Room(0, no, floor, type, 0, rentPerBed, ac);
        dao.insert(room);
        return room;
    }

    public void updateRent(String roomNo, double newRent) throws SQLException {
        Room room = requireRoom(roomNo);
        room.setRentPerBed(newRent); // validates the amount
        dao.updateRent(room.getId(), newRent);
    }

    public void deleteRoom(String roomNo) throws SQLException {
        Room room = requireRoom(roomNo);
        if (room.getOccupied() > 0) {
            throw new IllegalArgumentException("Room " + room.getRoomNo() + " still has tenants");
        }
        if (dao.tenantHistoryCount(room.getId()) > 0) {
            throw new IllegalArgumentException("Room " + room.getRoomNo()
                    + " has past tenants on record, so it is kept for their history");
        }
        dao.delete(room.getId());
    }

    /** [total beds, occupied beds] counted with a simple loop over the array. */
    public int[] bedSummary() throws SQLException {
        int total = 0;
        int occupied = 0;
        for (Room r : dao.findAll()) {
            total += r.getCapacity();
            occupied += r.getOccupied();
        }
        return new int[] {total, occupied};
    }

    /**
     * Edit a room. Room number must stay unique; the type can only change to one
     * that still fits the people already in the room. Bills already made keep
     * their old rent.
     */
    public Room editRoom(String currentNo, String newNo, int floor, RoomType type, double rentPerBed, boolean ac)
            throws SQLException {
        Room room = requireRoom(currentNo);
        String no = newNo.trim().toUpperCase();
        if (!no.matches("[A-Z0-9]{1,10}")) {
            throw new IllegalArgumentException("Room number can only have letters and digits (max 10)");
        }
        if (!no.equalsIgnoreCase(room.getRoomNo()) && findByRoomNo(no) != null) {
            throw new IllegalArgumentException("Room " + no + " already exists");
        }
        if (floor < 0 || floor > 50) {
            throw new IllegalArgumentException("Floor must be between 0 and 50");
        }
        if (type.beds() < room.getOccupied()) {
            throw new IllegalArgumentException("Room " + room.getRoomNo() + " has " + room.getOccupied()
                    + " tenants, so it cannot become a " + type.name().toLowerCase() + " room ("
                    + type.beds() + " bed" + (type.beds() > 1 ? "s" : "") + "). Shift someone out first.");
        }
        Room checked = new Room(room.getId(), no, floor, type, room.getOccupied(), rentPerBed, ac);
        if (!dao.update(room.getId(), no, floor, type, checked.getRentPerBed(), ac)) {
            throw new IllegalArgumentException("The room's tenants would not fit; nothing was changed");
        }
        return checked;
    }
}
