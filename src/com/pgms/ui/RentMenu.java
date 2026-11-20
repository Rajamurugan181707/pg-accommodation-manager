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
            System.out.println("0. Back");
            int choice = in.number("Choose: ", 0, 5);
            try {
                switch (choice) {
                    case 1: record(); break;
                    case 2: dues(); break;
                    case 3: bill(); break;
                    case 4: statement(); break;
                    case 5: received(); break;
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
}
