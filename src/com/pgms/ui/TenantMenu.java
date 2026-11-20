package com.pgms.ui;

import com.pgms.ds.DynamicArray;
import com.pgms.model.Gender;
import com.pgms.model.MessPlan;
import com.pgms.model.Room;
import com.pgms.model.Tenant;
import com.pgms.service.DueLine;
import com.pgms.service.MessService;
import com.pgms.service.RentService;
import com.pgms.service.RoomService;
import com.pgms.service.TenantService;
import com.pgms.util.Check;
import com.pgms.util.Dates;
import com.pgms.util.Money;
import com.pgms.util.Table;

import java.sql.SQLException;

/** Screens for the TENANT module. */
public class TenantMenu {

    private final ConsoleInput in;
    private final TenantService tenants = new TenantService();
    private final RoomService rooms = new RoomService();
    private final MessService mess = new MessService();
    private final RentService rent = new RentService();

    public TenantMenu(ConsoleInput in) {
        this.in = in;
    }

    public void show() throws SQLException {
        while (true) {
            System.out.println();
            System.out.println("------ TENANT MODULE ------");
            System.out.println("1. Register a tenant and give a bed");
            System.out.println("2. View current tenants");
            System.out.println("3. Search tenants by name (linear search)");
            System.out.println("4. View one tenant's details");
            System.out.println("5. Shift a tenant to another room");
            System.out.println("6. Check out a tenant");
            System.out.println("7. View past tenants");
            System.out.println("0. Back");
            int choice = in.number("Choose: ", 0, 7);
            try {
                switch (choice) {
                    case 1: register(); break;
                    case 2: print(tenants.activeTenants()); break;
                    case 3: print(tenants.searchByName(in.text("Name or part of it: "))); break;
                    case 4: details(); break;
                    case 5: shift(); break;
                    case 6: checkout(); break;
                    case 7: print(tenants.pastTenants()); break;
                    default: return;
                }
            } catch (IllegalArgumentException e) {
                System.out.println("  ! " + e.getMessage());
            }
        }
    }

    static void print(DynamicArray<Tenant> list) {
        Table t = new Table("ID", "Name", "Phone", "Room", "Mess plan", "Joined", "Status");
        for (Tenant x : list) {
            t.addRow(x.getId(), x.getName(), x.getPhone(), x.getRoomNo(),
                    x.getMessPlanName() == null ? "No mess" : x.getMessPlanName(),
                    Dates.format(x.getJoinDate()), x.getStatus());
        }
        t.print();
    }

    private void register() throws SQLException {
        System.out.println("Rooms with free beds:");
        RoomMenu.print(rooms.vacantRooms());
        String name = checked("Full name: ", 1);
        String phone = in.matching("Mobile number (10 digits): ", "[6-9][0-9]{9}",
                "Enter a 10-digit mobile number starting with 6, 7, 8 or 9.");
        if (tenants.phoneTaken(phone)) {
            throw new IllegalArgumentException("A current tenant already has phone " + phone);
        }
        String email = checked("Email (Enter to skip): ", 2);
        Gender gender = in.choose("Gender", Gender.values());
        String job = checked("Occupation (Enter to skip): ", 3);
        String roomNo = askRoomWithFreeBed();
        Integer planId = askPlan();
        java.sql.Date joined = in.date("Joining date");
        double deposit = in.amountOrZero("Security deposit (0 if none): ");
        Tenant t = tenants.register(name, phone, email, gender, job, roomNo, planId, joined, deposit);
        System.out.println("  Registered " + t.getName() + " with tenant ID " + t.getId()
                + " in room " + t.getRoomNo() + ".");
    }

    /** Asks until the answer passes its check: 1 = name, 2 = email, 3 = occupation. */
    private String checked(String prompt, int kind) {
        while (true) {
            String s = in.line(prompt);
            try {
                switch (kind) {
                    case 1: return Check.text(s, 60, "Name", true);
                    case 2: return Check.email(s);
                    default: return Check.text(s, 40, "Occupation", false);
                }
            } catch (IllegalArgumentException e) {
                System.out.println("  " + e.getMessage() + ".");
            }
        }
    }

    /** Asks until the room number is one that exists and has a free bed. */
    private String askRoomWithFreeBed() throws SQLException {
        while (true) {
            String no = in.text("Room number: ");
            Room r = rooms.findByRoomNo(no);
            if (r == null) {
                System.out.println("  No room numbered " + no + ".");
            } else if (r.isFull()) {
                System.out.println("  Room " + r.getRoomNo() + " is full. Pick one from the list above.");
            } else {
                return r.getRoomNo();
            }
        }
    }

    /** Shows the plans and asks until it gets a real plan ID, or Enter for no mess. */
    Integer askPlan() throws SQLException {
        DynamicArray<MessPlan> plans = mess.plans();
        Table t = new Table("ID", "Plan", "Meals/day", "Per month");
        for (MessPlan p : plans) {
            t.addRow(p.getId(), p.getName(), p.getMealsPerDay(), Money.inr(p.monthlyCharge()));
        }
        t.print();
        while (true) {
            String s = in.line("Mess plan ID (Enter = no mess): ");
            if (s.isEmpty()) {
                return null;
            }
            try {
                int id = Integer.parseInt(s);
                if (mess.findPlan(id) != null) {
                    return id;
                }
            } catch (NumberFormatException ignored) {
                // fall through to the message
            }
            System.out.println("  Enter one of the plan IDs above, or just press Enter.");
        }
    }

    private void details() throws SQLException {
        Tenant t = tenants.requireTenant(in.number("Tenant ID: ", 1, Integer.MAX_VALUE));
        System.out.println();
        System.out.println("  " + t);
        System.out.println("  Email      : " + (t.getEmail() == null ? "-" : t.getEmail()));
        System.out.println("  Gender     : " + t.getGender());
        System.out.println("  Occupation : " + (t.getOccupation() == null ? "-" : t.getOccupation()));
        System.out.println("  Room       : " + t.getRoomNo());
        System.out.println("  Mess plan  : " + (t.getMessPlanName() == null ? "No mess" : t.getMessPlanName()));
        System.out.println("  Joined     : " + Dates.format(t.getJoinDate()));
        System.out.println("  Left       : " + Dates.format(t.getLeaveDate()));
        System.out.println("  Deposit    : " + Money.inr(t.getDeposit()));
        System.out.println("  Status     : " + t.getStatus());
    }

    private void shift() throws SQLException {
        int id = in.number("Tenant ID: ", 1, Integer.MAX_VALUE);
        Tenant t = tenants.requireActive(id);
        System.out.println("  " + t.getName() + " is in room " + t.getRoomNo() + ". Rooms with free beds:");
        RoomMenu.print(rooms.vacantRooms());
        String to = in.text("Move to room: ");
        tenants.shiftRoom(id, to);
        System.out.println("  Moved " + t.getName() + " to room " + to.toUpperCase() + ".");
    }

    private void checkout() throws SQLException {
        int id = in.number("Tenant ID: ", 1, Integer.MAX_VALUE);
        Tenant t = tenants.requireActive(id);
        double pending = 0;
        for (DueLine line : rent.statement(id)) {
            pending += line.pending();
        }
        System.out.println("  " + t.getName() + ", room " + t.getRoomNo()
                + ". Deposit held: " + Money.inr(t.getDeposit())
                + ". Unpaid rent so far: " + Money.inr(pending) + ".");
        if (pending > 0) {
            System.out.println("  Settle the unpaid rent (or adjust it from the deposit) before they leave.");
        }
        java.sql.Date leaving = in.date("Leaving date");
        if (in.yesNo("Check out " + t.getName() + "?")) {
            tenants.checkout(id, leaving);
            System.out.println("  Checked out. Room " + t.getRoomNo() + " has a free bed again.");
        }
    }
}
