package com.pgms.gui;

import com.pgms.ds.DynamicArray;
import com.pgms.model.Billable;
import com.pgms.model.PaymentMode;
import com.pgms.model.RentPayment;
import com.pgms.service.DueLine;
import com.pgms.service.RentService;
import com.pgms.util.Dates;
import com.pgms.util.Money;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;

/** RENT module screen. */
public class RentPanel extends JPanel implements Screen {

    private static final long serialVersionUID = 1L;

    private final RentService rent = new RentService();
    private final DefaultTableModel model =
            Ui.model("ID", "Tenant", "Room", "Bill", "Paid", "Pending", "Status");
    private final JTable table = Ui.table(model);
    private final JComboBox<String> month = Ui.combo(recentMonths());
    private final JLabel totals = Ui.bold(" ");
    private final StatCard billedCard = new StatCard("Billed", "receipt", 0);
    private final StatCard paidCard = new StatCard("Collected", "check", 2);
    private final StatCard pendingCard = new StatCard("Pending", "rupee", 3);
    private DynamicArray<DueLine> shown = new DynamicArray<DueLine>();

    public RentPanel() {
        super(new BorderLayout(0, 16));
        setOpaque(false);

        JButton record = Ui.primary("Record payment", "receipt");
        JButton bill = Ui.secondary("View bill", "info");
        JButton editBill = Ui.secondary("Edit bill", "edit");
        JButton editPay = Ui.secondary("Edit payment", "receipt");
        JButton statement = Ui.secondary("Statement", "list");
        JButton received = Ui.secondary("Month's payments", "calendar");

        JLabel monthLabel = Ui.bold("Month");
        monthLabel.setIcon(Ui.icon("calendar", 16));
        monthLabel.setIconTextGap(8);
        RoundPanel bar = new RoundPanel(new BorderLayout(0, 14), 18);
        bar.add(Ui.row(monthLabel, month, Ui.gap(4), totals), BorderLayout.NORTH);
        bar.add(Ui.row(record, editPay, editBill, bill, statement, received), BorderLayout.SOUTH);

        JPanel stats = new JPanel(new GridLayout(1, 3, 16, 0));
        stats.setOpaque(false);
        stats.add(billedCard);
        stats.add(paidCard);
        stats.add(pendingCard);

        JPanel top = new JPanel(new BorderLayout(0, 16));
        top.setOpaque(false);
        top.add(bar, BorderLayout.NORTH);
        top.add(stats, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);
        Ui.widths(table, 56, 200, 70, 120, 120, 120, 120);
        add(Ui.card("Dues, most pending first", "list", Ui.scroll(table)), BorderLayout.CENTER);

        month.addActionListener(e -> refresh());
        record.addActionListener(e -> record());
        bill.addActionListener(e -> bill());
        editBill.addActionListener(e -> editBill());
        editPay.addActionListener(e -> editPayment());
        statement.addActionListener(e -> statement());
        received.addActionListener(e -> received());
    }

    /** The current month first, then the 11 before it. */
    private static String[] recentMonths() {
        String[] months = new String[12];
        String m = Dates.currentMonth();
        for (int i = 0; i < 12; i++) {
            months[i] = m;
            m = previous(m);
        }
        return months;
    }

    private static String previous(String m) {
        int y = Integer.parseInt(m.substring(0, 4));
        int mo = Integer.parseInt(m.substring(5)) - 1;
        if (mo == 0) {
            mo = 12;
            y--;
        }
        return String.format("%04d-%02d", y, mo);
    }

    private String month() {
        return (String) month.getSelectedItem();
    }

    @Override
    public void refresh() {
        try {
            shown = rent.duesForMonth(month());
            model.setRowCount(0);
            double bill = 0;
            double paid = 0;
            double pending = 0;
            for (DueLine l : shown) {
                model.addRow(new Object[] {l.getTenant().getId(), l.getTenant().getName(), l.getTenant().getRoomNo(),
                        Money.inr(l.due()), Money.inr(l.paid()), Money.inr(l.pending()), l.status()});
                bill += l.due();
                paid += l.paid();
                pending += l.pending();
            }
            totals.setText(shown.size() + " tenant(s) billed for " + month() + ".");
            int owing = 0;
            for (DueLine l : shown) {
                if (l.pending() > 0) {
                    owing++;
                }
            }
            billedCard.set(Money.inr(bill), "for " + month(), -1);
            paidCard.set(Money.inr(paid), Math.round(bill == 0 ? 0 : 100 * paid / bill) + "% of the bill", bill == 0 ? 0 : paid / bill);
            pendingCard.set(Money.inr(pending), owing + " tenant(s) still owe", -1);
        } catch (Exception e) {
            Ui.problem(this, e);
        }
    }

    private DueLine selected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            throw new IllegalArgumentException("Click a tenant in the table first.");
        }
        return shown.get(row);
    }

    private void record() {
        try {
            DueLine l = selected();
            if (l.pending() == 0) {
                throw new IllegalArgumentException(l.getTenant().getName() + " has already paid in full for " + month() + ".");
            }
            Form f = new Form();
            f.note(l.getTenant().getName() + "  |  " + month() + "  |  bill " + Money.inr(l.due())
                    + ", paid " + Money.inr(l.paid()) + ", pending " + Money.inr(l.pending()));
            JTextField amount = f.text("Amount received", String.valueOf(Math.round(l.pending())));
            JComboBox<PaymentMode> mode = f.combo("Mode", PaymentMode.values());
            JTextField date = f.text("Paid on", Dates.format(Dates.today()));
            JTextField note = f.text("Note (optional)", "");
            final String m = month();
            boolean saved = f.show(this, "Record payment", () -> {
                RentPayment p = rent.recordPayment(l.getTenant().getId(), m, Form.amount(amount),
                        Form.date(date), (PaymentMode) mode.getSelectedItem(), Form.optional(note));
                DueLine after = rent.dueFor(l.getTenant().getId(), m);
                Ui.info(this, "Payment saved", "Receipt #" + p.getId() + " for " + Money.inr(p.getAmount())
                        + " saved.\nStill pending for " + m + ": " + Money.inr(after.pending()));
                Toast.show(this, "Receipt #" + p.getId() + " saved: " + Money.inr(p.getAmount()) + " from "
                        + l.getTenant().getName());
            });
            if (saved) {
                refresh();
            }
        } catch (Exception e) {
            Ui.problem(this, e);
        }
    }

    private void bill() {
        try {
            DueLine l = selected();
            DefaultTableModel m = Ui.model("Item", "Amount");
            for (Billable item : l.getItems()) {
                m.addRow(new Object[] {item.billLabel(), Money.inr(item.monthlyCharge())});
            }
            m.addRow(new Object[] {"TOTAL BILL", Money.inr(l.due())});
            m.addRow(new Object[] {"Paid", Money.inr(l.paid())});
            m.addRow(new Object[] {"Pending", Money.inr(l.pending())});
            Ui.tableDialog(this, "Bill", l.getTenant().getName() + "  -  " + month(), m, "Status: " + l.status());
        } catch (Exception e) {
            Ui.problem(this, e);
        }
    }

    private void statement() {
        try {
            DueLine sel = selected();
            int id = sel.getTenant().getId();
            DefaultTableModel m = Ui.model("Month", "Bill", "Paid", "Pending", "Status");
            double unpaid = 0;
            for (DueLine l : rent.statement(id)) {
                m.addRow(new Object[] {l.getMonth(), Money.inr(l.due()), Money.inr(l.paid()),
                        Money.inr(l.pending()), l.status()});
                unpaid += l.pending();
            }
            m.addRow(new Object[] {"", "", "", "", ""});
            m.addRow(new Object[] {"RECEIPTS", "Month", "Amount", "Paid on", "Mode"});
            for (RentPayment p : rent.paymentsOf(id)) {
                m.addRow(new Object[] {"#" + p.getId(), p.getBillMonth(), Money.inr(p.getAmount()),
                        Dates.format(p.getPaidOn()), p.getMode()});
            }
            Ui.tableDialog(this, "Statement", sel.getTenant().getName() + "  -  room " + sel.getTenant().getRoomNo(),
                    m, "Total unpaid: " + Money.inr(unpaid));
        } catch (Exception e) {
            Ui.problem(this, e);
        }
    }

    private void received() {
        try {
            DefaultTableModel m = Ui.model("Receipt", "Tenant", "Amount", "Paid on", "Mode", "Note");
            double total = 0;
            for (RentPayment p : rent.paymentsIn(month())) {
                m.addRow(new Object[] {p.getId(), p.getTenantName(), Money.inr(p.getAmount()),
                        Dates.format(p.getPaidOn()), p.getMode(), p.getNote() == null ? "-" : p.getNote()});
                total += p.getAmount();
            }
            Ui.tableDialog(this, "Payments received", "Payments towards " + month(), m,
                    "Total received: " + Money.inr(total));
        } catch (Exception e) {
            Ui.problem(this, e);
        }
    }

    /** Change one month's saved bill for the selected tenant (e.g. a discount). */
    private void editBill() {
        try {
            final DueLine l = selected();
            final String m = month();
            double roomPart = l.getItems()[0].monthlyCharge();
            double messPart = l.getItems().length > 1 ? l.getItems()[1].monthlyCharge() : 0;
            Form f = new Form();
            f.note(l.getTenant().getName() + "  |  bill for " + m + "  |  paid so far " + Money.inr(l.paid()));
            JTextField rentField = f.text("Room rent", Money.plain(roomPart));
            JTextField messField = f.text("Mess charge", Money.plain(messPart));
            f.note("This changes only " + m + ". Other months keep their own bills.");
            boolean saved = f.show(this, "Edit bill", () ->
                    rent.editBill(l.getTenant().getId(), m, Form.amount(rentField), Form.amountOrZero(messField)));
            if (saved) {
                refresh();
                Toast.show(this, "Bill for " + l.getTenant().getName() + ", " + m + " saved.");
            }
        } catch (Exception e) {
            Ui.problem(this, e);
        }
    }

    /** Correct or delete one of the selected tenant's receipts for this month. */
    @SuppressWarnings("unchecked")
    private void editPayment() {
        try {
            final DueLine l = selected();
            final String m = month();
            DynamicArray<RentPayment> mine = rent.paymentsOf(l.getTenant().getId())
                    .filter(p -> p.getBillMonth().equals(m));
            if (mine.isEmpty()) {
                throw new IllegalArgumentException(l.getTenant().getName() + " has no payments for " + m + ".");
            }
            Choice<RentPayment>[] items = new Choice[mine.size()];
            for (int i = 0; i < mine.size(); i++) {
                RentPayment p = mine.get(i);
                items[i] = new Choice<RentPayment>(p, "Receipt #" + p.getId() + "  -  " + Money.inr(p.getAmount())
                        + "  on " + Dates.format(p.getPaidOn()));
            }
            Form f = new Form();
            f.note(l.getTenant().getName() + "  |  " + m + "  |  bill " + Money.inr(l.due()));
            JComboBox<Choice<RentPayment>> which = f.combo("Receipt", items);
            JTextField amount = f.text("Amount", "");
            JComboBox<PaymentMode> mode = f.combo("Mode", PaymentMode.values());
            JTextField date = f.text("Paid on", "");
            JTextField note = f.text("Note (optional)", "");
            javax.swing.JCheckBox delete = f.check("Delete this receipt");
            Runnable fill = () -> {
                RentPayment p = ((Choice<RentPayment>) which.getSelectedItem()).value();
                amount.setText(Money.plain(p.getAmount()));
                mode.setSelectedItem(p.getMode());
                date.setText(Dates.format(p.getPaidOn()));
                note.setText(p.getNote() == null ? "" : p.getNote());
            };
            fill.run();
            which.addActionListener(e -> fill.run());
            final String[] msg = {null};
            boolean saved = f.show(this, "Edit payment", () -> {
                RentPayment p = ((Choice<RentPayment>) which.getSelectedItem()).value();
                if (delete.isSelected()) {
                    if (!Ui.confirm(this, "Delete receipt", "Delete receipt #" + p.getId() + " for "
                            + Money.inr(p.getAmount()) + "? This cannot be undone.")) {
                        throw new IllegalArgumentException("Not deleted.");
                    }
                    rent.deletePayment(p.getId());
                    msg[0] = "Receipt #" + p.getId() + " deleted.";
                } else {
                    RentPayment u = rent.editPayment(p.getId(), Form.amount(amount), Form.date(date),
                            (PaymentMode) mode.getSelectedItem(), Form.optional(note));
                    msg[0] = "Receipt #" + u.getId() + " saved: " + Money.inr(u.getAmount()) + ".";
                }
            });
            if (saved) {
                refresh();
                Toast.show(this, msg[0]);
            }
        } catch (Exception e) {
            Ui.problem(this, e);
        }
    }
}
