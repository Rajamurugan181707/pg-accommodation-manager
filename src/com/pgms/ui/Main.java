package com.pgms.ui;

import com.pgms.db.DBConnection;
import com.pgms.ds.DynamicArray;
import com.pgms.service.DueLine;
import com.pgms.service.RentService;
import com.pgms.service.RoomService;
import com.pgms.service.TenantService;
import com.pgms.util.Dates;
import com.pgms.util.Money;

import java.sql.SQLException;

/**
 * PG ACCOMMODATION MANAGEMENT SYSTEM
 * II CSE F mini project (DS + OOPS) - A. Raja Murugan
 *
 * Modules: Room, Tenant, Rent, Mess.   DS concept: Array.   Storage: MySQL via JDBC.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=============================================");
        System.out.println("     PG ACCOMMODATION MANAGEMENT SYSTEM");
        System.out.println("=============================================");
        try {
            DBConnection.testConnection();
            int made = new RentService().billEveryoneUpToNow();
            if (made > 0) {
                System.out.println("Made " + made + " new monthly bill(s) up to " + Dates.currentMonth() + ".");
            }
        } catch (SQLException e) {
            System.out.println("Cannot connect to MySQL: " + e.getMessage());
            System.out.println("Check that MySQL is running, sql/schema.sql has been loaded,");
            System.out.println("and the user / password in db.properties are right.");
            System.exit(1);
        }

        ConsoleInput in = new ConsoleInput();
        RoomMenu roomMenu = new RoomMenu(in);
        TenantMenu tenantMenu = new TenantMenu(in);
        RentMenu rentMenu = new RentMenu(in);
        MessMenu messMenu = new MessMenu(in);

        try {
            while (true) {
                try {
                    dashboard();
                    System.out.println("1. Room module");
                    System.out.println("2. Tenant module");
                    System.out.println("3. Rent module");
                    System.out.println("4. Mess module");
                    System.out.println("0. Exit");
                    int choice = in.number("Choose: ", 0, 4);
                    switch (choice) {
                        case 1: roomMenu.show(); break;
                        case 2: tenantMenu.show(); break;
                        case 3: rentMenu.show(); break;
                        case 4: messMenu.show(); break;
                        default:
                            System.out.println("Goodbye.");
                            return;
                    }
                } catch (SQLException e) {
                    // Wait for the user here; without this the menu loop would retry
                    // the dashboard forever and flood the screen while MySQL is down.
                    System.out.println("  ! Database error: " + e.getMessage());
                    System.out.println("  Check that MySQL is running (mysql.server start).");
                    if (in.line("  Press Enter to try again, or type 0 to exit: ").equals("0")) {
                        System.out.println("Goodbye.");
                        return;
                    }
                }
            }
        } catch (ConsoleInput.EndOfInput e) {
            System.out.println("Input ended. Goodbye.");
        }
    }

    /** One-screen summary shown above the main menu. */
    private static void dashboard() throws SQLException {
        int[] beds = new RoomService().bedSummary();
        int tenants = new TenantService().activeTenants().size();
        String month = Dates.currentMonth();
        DynamicArray<DueLine> dues = new RentService().duesForMonth(month);
        double billed = 0;
        double pending = 0;
        int unpaid = 0;
        for (DueLine l : dues) {
            billed += l.due();
            pending += l.pending();
            if (l.pending() > 0) {
                unpaid++;
            }
        }
        System.out.println();
        System.out.println("Beds: " + beds[1] + " taken / " + beds[0] + " total (" + (beds[0] - beds[1])
                + " free)   Tenants: " + tenants);
        System.out.println(month + ": billed " + Money.inr(billed) + ", pending " + Money.inr(pending)
                + " from " + unpaid + " tenant(s)");
        System.out.println("---------------------------------------------");
    }
}
