package com.pgms.ui;

import com.pgms.ds.DynamicArray;
import com.pgms.model.Billable;
import com.pgms.model.PaymentMode;
import com.pgms.model.RentPayment;
import com.pgms.model.Tenant;
import com.pgms.service.DueLine;
import com.pgms.service.RentService;
import com.pgms.service.TenantService;
import com.pgms.util.Dates;
import com.pgms.util.Money;
import com.pgms.util.Table;

import java.sql.SQLException;

/** Screens for the RENT module. */
public class RentMenu {

    private final ConsoleInput in;
    private final RentService rent = new RentService();
    private final TenantService tenants = new TenantService();

    public RentMenu(ConsoleInput in) {
        this.in = in;
    }

    public void show() throws SQLException {
        while (true) {
            System.out.println();
            System.out.println("------ RENT MODULE ------");
            System.out.println("1. Record a rent payment");
            System.out.println("2. Monthly dues - who has not paid");
            System.out.println("3. A tenant's bill for a month");
            System.out.println("4. A tenant's full statement");
            System.out.println("5. Payments received in a month");
            System.out.println("6. Edit a payment");
            System.out.println("7. Delete a payment");
            System.out.println("8. Edit a month's bill");
            System.out.println("0. Back");
            int choice = in.number("Choose: ", 0, 8);
            try {
                switch (choice) {
                    case 1: record(); break;
                    case 2: dues(); break;
                    case 3: bill(); break;
                    case 4: statement(); break;
                    case 5: received(); break;
                    case 6: editPayment(); break;
                    case 7: deletePayment(); break;
                    case 8: editBill(); break;
                    default: return;
                }
            } catch (IllegalArgumentException e) {
                System.out.println("  ! " + e.getMessage());
            }
        }
    }

    private void record() throws SQLException {
        int id = in.number("Tenant ID: ", 1, Integer.MAX_VALUE);
        String month = in.month("For month");
        DueLine line = rent.dueFor(id, month);
        System.out.println("  " + line.getTenant().getName() + ": bill " + Money.inr(line.due())
                + ", paid " + Money.inr(line.paid()) + ", pending " + Money.inr(line.pending()));
        if (line.pending() == 0) {
            System.out.println("  Nothing pending for " + month + ".");
            return;
        }
        double amount = in.amount("Amount received: ");
        while (amount > line.pending()) {
            System.out.println("  Only " + Money.inr(line.pending()) + " is pending; enter that or less.");
            amount = in.amount("Amount received: ");
        }
        PaymentMode mode = in.choose("Mode", PaymentMode.values());
        java.sql.Date on = in.date("Paid on");
        String note = in.optionalText("Note (Enter to skip): ");
        RentPayment p = rent.recordPayment(id, month, amount, on, mode, note);
        DueLine after = rent.dueFor(id, month);
        System.out.println("  Receipt #" + p.getId() + " saved. Still pending for " + month + ": "
                + Money.inr(after.pending()) + ".");
    }

    private void dues() throws SQLException {
        String month = in.month("Month");
        DynamicArray<DueLine> lines = rent.duesForMonth(month);
        Table t = new Table("ID", "Tenant", "Room", "Bill", "Paid", "Pending", "Status");
        double billed = 0;
        double paid = 0;
        double pending = 0;
        for (DueLine l : lines) {
            t.addRow(l.getTenant().getId(), l.getTenant().getName(), l.getTenant().getRoomNo(),
                    Money.inr(l.due()), Money.inr(l.paid()), Money.inr(l.pending()), l.status());
            billed += l.due();
            paid += l.paid();
            pending += l.pending();
        }
        System.out.println("Dues for " + month + " (most pending first):");
        t.print();
        System.out.println("  Billed " + Money.inr(billed) + " | Collected " + Money.inr(paid)
                + " | Pending " + Money.inr(pending));
    }

    private void bill() throws SQLException {
        int id = in.number("Tenant ID: ", 1, Integer.MAX_VALUE);
        String month = in.month("Month");
        DueLine l = rent.dueFor(id, month);
        Table t = new Table("Item", "Amount");
        for (Billable item : l.getItems()) {
            t.addRow(item.billLabel(), Money.inr(item.monthlyCharge()));
        }
        t.addRow("TOTAL BILL", Money.inr(l.due()));
        t.addRow("Paid", Money.inr(l.paid()));
        t.addRow("Pending", Money.inr(l.pending()));
        System.out.println("Bill for " + l.getTenant().getName() + ", " + month + ":");
        t.print();
    }

    private void statement() throws SQLException {
        int id = in.number("Tenant ID: ", 1, Integer.MAX_VALUE);
        Tenant tenant = tenants.requireTenant(id);
        DynamicArray<DueLine> months = rent.statement(id);
        Table t = new Table("Month", "Bill", "Paid", "Pending", "Status");
        double pending = 0;
        for (DueLine l : months) {
            t.addRow(l.getMonth(), Money.inr(l.due()), Money.inr(l.paid()), Money.inr(l.pending()), l.status());
            pending += l.pending();
        }
        System.out.println("Statement for " + tenant.getName() + " (room " + tenant.getRoomNo() + "):");
        t.print();
        System.out.println("  Total unpaid: " + Money.inr(pending));

        Table p = new Table("Receipt", "Month", "Amount", "Paid on", "Mode", "Note");
        for (RentPayment r : rent.paymentsOf(id)) {
            p.addRow(r.getId(), r.getBillMonth(), Money.inr(r.getAmount()), Dates.format(r.getPaidOn()),
                    r.getMode(), r.getNote());
        }
        System.out.println("Payments:");
        p.print();
    }

    private void received() throws SQLException {
        String month = in.month("Month");
        Table t = new Table("Receipt", "Tenant", "Amount", "Paid on", "Mode", "Note");
        double total = 0;
        for (RentPayment r : rent.paymentsIn(month)) {
            t.addRow(r.getId(), r.getTenantName(), Money.inr(r.getAmount()), Dates.format(r.getPaidOn()),
                    r.getMode(), r.getNote());
            total += r.getAmount();
        }
        System.out.println("Payments towards " + month + ":");
        t.print();
        System.out.println("  Total received: " + Money.inr(total));
    }

    private void editPayment() throws SQLException {
        RentPayment p = rent.requirePayment(in.number("Receipt number: ", 1, Integer.MAX_VALUE));
        System.out.println("  Receipt #" + p.getId() + ": " + p.getTenantName() + ", " + p.getBillMonth()
                + ". Press Enter to keep the value in [brackets].");
        String a = RoomMenu.keep(in.line("  Amount [" + com.pgms.util.Money.plain(p.getAmount()) + "]: "),
                String.valueOf(p.getAmount()));
        String m = RoomMenu.keep(in.line("  Mode 1=CASH 2=UPI 3=CARD 4=BANK [" + p.getMode() + "]: "), "");
        String d = RoomMenu.keep(in.line("  Paid on [" + Dates.format(p.getPaidOn()) + "]: "), Dates.format(p.getPaidOn()));
        String n = in.line("  Note [" + (p.getNote() == null ? "" : p.getNote()) + "]: ");
        double amount;
        java.sql.Date on;
        try {
            amount = Double.parseDouble(a.replace(",", ""));
            on = Dates.parse(d);
        } catch (NumberFormatException | java.text.ParseException e) {
            throw new IllegalArgumentException("Amount must be a number and the date must look like 2026-11-20");
        }
        PaymentMode mode = m.matches("[1-4]") ? PaymentMode.values()[Integer.parseInt(m) - 1] : p.getMode();
        String note = n.trim().isEmpty() ? p.getNote() : n.trim().equals("-") ? null : n.trim();
        RentPayment u = rent.editPayment(p.getId(), amount, on, mode, note);
        System.out.println("  Receipt #" + u.getId() + " saved: " + Money.inr(u.getAmount()) + ".");
    }

    private void deletePayment() throws SQLException {
        RentPayment p = rent.requirePayment(in.number("Receipt number to delete: ", 1, Integer.MAX_VALUE));
        if (in.yesNo("Delete receipt #" + p.getId() + " (" + p.getTenantName() + ", " + p.getBillMonth() + ", "
                + Money.inr(p.getAmount()) + ")? This cannot be undone.")) {
            rent.deletePayment(p.getId());
            System.out.println("  Receipt #" + p.getId() + " deleted.");
        }
    }

    private void editBill() throws SQLException {
        int id = in.number("Tenant ID: ", 1, Integer.MAX_VALUE);
        String month = in.month("Month");
        DueLine l = rent.dueFor(id, month);
        double roomPart = l.getItems()[0].monthlyCharge();
        double messPart = l.getItems().length > 1 ? l.getItems()[1].monthlyCharge() : 0;
        System.out.println("  " + l.getTenant().getName() + ", " + month + ": paid so far " + Money.inr(l.paid())
                + ". Press Enter to keep the value in [brackets].");
        String r = RoomMenu.keep(in.line("  Room rent [" + com.pgms.util.Money.plain(roomPart) + "]: "), String.valueOf(roomPart));
        String m = RoomMenu.keep(in.line("  Mess charge [" + com.pgms.util.Money.plain(messPart) + "]: "), String.valueOf(messPart));
        try {
            DueLine u = rent.editBill(id, month, Double.parseDouble(r.replace(",", "")), Double.parseDouble(m.replace(",", "")));
            System.out.println("  Bill saved: " + Money.inr(u.due()) + ", pending " + Money.inr(u.pending()) + ".");
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Amounts must be numbers");
        }
    }
}
