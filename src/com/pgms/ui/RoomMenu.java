package com.pgms.ui;

import com.pgms.ds.DynamicArray;
import com.pgms.model.Room;
import com.pgms.model.RoomType;
import com.pgms.service.RoomService;
import com.pgms.util.Money;
import com.pgms.util.Table;

import java.sql.SQLException;

/** Screens for the ROOM module. */
public class RoomMenu {

    private final ConsoleInput in;
    private final RoomService rooms = new RoomService();

    public RoomMenu(ConsoleInput in) {
        this.in = in;
    }

    public void show() throws SQLException {
        while (true) {
            System.out.println();
            System.out.println("------ ROOM MODULE ------");
            System.out.println("1. View all rooms");
            System.out.println("2. View rooms with free beds");
            System.out.println("3. Search a room by number (binary search)");
            System.out.println("4. View rooms sorted by rent");
            System.out.println("5. Add a room");
            System.out.println("6. Edit a room (number, floor, type, rent, AC)");
            System.out.println("7. Delete an empty room");
            System.out.println("0. Back");
            int choice = in.number("Choose: ", 0, 7);
            try {
                switch (choice) {
                    case 1: print(rooms.allRooms()); break;
                    case 2: print(rooms.vacantRooms()); break;
                    case 3: search(); break;
                    case 4: print(rooms.roomsSortedByRent()); break;
                    case 5: add(); break;
                    case 6: editRoom(); break;
                    case 7: delete(); break;
                    default: return;
                }
            } catch (IllegalArgumentException e) {
                System.out.println("  ! " + e.getMessage());
            }
        }
    }

    static void print(DynamicArray<Room> list) {
        Table t = new Table("Room", "Floor", "Type", "AC", "Beds", "Taken", "Free", "Rent/bed");
        for (Room r : list) {
            t.addRow(r.getRoomNo(), r.getFloor() == 0 ? "Ground" : String.valueOf(r.getFloor()),
                    r.getType(), r.hasAc() ? "Yes" : "No", r.getCapacity(), r.getOccupied(),
                    r.vacantBeds(), Money.inr(r.getRentPerBed()));
        }
        t.print();
    }

    private void search() throws SQLException {
        String no = in.text("Room number: ");
        Room r = rooms.findByRoomNo(no);
        if (r == null) {
            System.out.println("  No room numbered " + no + ".");
            return;
        }
        DynamicArray<Room> one = new DynamicArray<Room>(1);
        one.add(r);
        print(one);
    }

    private void add() throws SQLException {
        String no = in.matching("New room number (e.g. 203): ", "[A-Za-z0-9]{1,10}",
                "Use only letters and digits, up to 10 (e.g. 203 or G04).").toUpperCase();
        if (rooms.findByRoomNo(no) != null) {
            throw new IllegalArgumentException("Room " + no + " already exists");
        }
        int floor = in.number("Floor (0 = ground): ", 0, 50);
        RoomType type = in.choose("Type", RoomType.values());
        double rent = in.amount("Rent per bed per month: ");
        boolean ac = in.yesNo("AC room?");
        Room r = rooms.addRoom(no, floor, type, rent, ac);
        System.out.println("  Room " + r.getRoomNo() + " added with " + r.getCapacity() + " bed(s).");
    }

    private void delete() throws SQLException {
        String no = in.text("Room number to delete: ");
        if (in.yesNo("Really delete room " + no + "?")) {
            rooms.deleteRoom(no);
            System.out.println("  Room deleted.");
        }
    }

    private void editRoom() throws SQLException {
        Room r = rooms.requireRoom(in.text("Room number to edit: "));
        System.out.println("  Press Enter to keep the value in [brackets].");
        String no = keep(in.line("  Room number [" + r.getRoomNo() + "]: "), r.getRoomNo());
        String floorText = keep(in.line("  Floor [" + r.getFloor() + "]: "), String.valueOf(r.getFloor()));
        int floor;
        try {
            floor = Integer.parseInt(floorText);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Floor must be a whole number");
        }
        String typeText = keep(in.line("  Type 1=SINGLE 2=DOUBLE 3=TRIPLE [" + r.getType() + "]: "), "");
        RoomType type = r.getType();
        if (typeText.matches("[1-3]")) {
            type = RoomType.values()[Integer.parseInt(typeText) - 1];
        }
        String rentText = keep(in.line("  Rent per bed [" + com.pgms.util.Money.plain(r.getRentPerBed()) + "]: "),
                String.valueOf(r.getRentPerBed()));
        double rent;
        try {
            rent = Double.parseDouble(rentText.replace(",", ""));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Rent must be a number");
        }
        String acText = keep(in.line("  AC room? y/n [" + (r.hasAc() ? "y" : "n") + "]: "), r.hasAc() ? "y" : "n");
        Room u = rooms.editRoom(r.getRoomNo(), no, floor, type, rent, acText.toLowerCase().startsWith("y"));
        System.out.println("  Room " + u.getRoomNo() + " saved. Bills already made keep their old rent.");
    }

    static String keep(String typed, String old) {
        return typed == null || typed.trim().isEmpty() ? old : typed.trim();
    }
}
