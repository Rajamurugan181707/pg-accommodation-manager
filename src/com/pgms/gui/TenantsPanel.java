package com.pgms.gui;

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
import com.pgms.util.Dates;
import com.pgms.util.Money;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.sql.SQLException;

/** TENANT module screen. */
public class TenantsPanel extends JPanel implements Screen {

    private static final long serialVersionUID = 1L;
    private static final String[] SHOW = {"Current tenants", "Past tenants", "Everyone"};

    private final TenantService tenants = new TenantService();
    private final RoomService rooms = new RoomService();
    private final MessService mess = new MessService();
    private final RentService rent = new RentService();

    private final DefaultTableModel model =
            Ui.model("ID", "Name", "Phone", "Room", "Mess plan", "Joined", "Status");
    private final JTable table = Ui.table(model);
    private final PgField search = Ui.searchField(14);
    private final JComboBox<String> show = Ui.combo(SHOW);
    private final JLabel status = Ui.bold(" ");
    private DynamicArray<Tenant> shown = new DynamicArray<Tenant>();

    public TenantsPanel() {
        super(new BorderLayout(0, 16));
        setOpaque(false);

        JButton register = Ui.primary("Register tenant", "plus");
        JButton details = Ui.secondary("Details", "info");
        JButton shift = Ui.secondary("Shift room", "move");
        JButton plan = Ui.secondary("Change mess plan", "bowl");
        JButton checkout = Ui.secondary("Check out", "exit");
        search.setName("Search name");
        search.setToolTipText("Type part of a name");
        search.setHint("Search by name");

        RoundPanel bar = new RoundPanel(new BorderLayout(0, 14), 18);
        bar.add(Ui.row(search, show, Ui.gap(4), status), BorderLayout.NORTH);
        bar.add(Ui.row(register, details, shift, plan, checkout), BorderLayout.SOUTH);
        add(bar, BorderLayout.NORTH);
        Ui.widths(table, 56, 200, 120, 70, 160, 110, 110);
        add(Ui.card("Tenants", "users", Ui.scroll(table)), BorderLayout.CENTER);

        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { refresh(); }
            @Override public void removeUpdate(DocumentEvent e) { refresh(); }
            @Override public void changedUpdate(DocumentEvent e) { refresh(); }
        });
        show.addActionListener(e -> refresh());
        register.addActionListener(e -> register());
        details.addActionListener(e -> details());
        shift.addActionListener(e -> shift());
        plan.addActionListener(e -> changePlan());
        checkout.addActionListener(e -> checkout());
    }

    @Override
    public void refresh() {
        try {
            DynamicArray<Tenant> list;
            switch (show.getSelectedIndex()) {
                case 1: list = tenants.pastTenants(); break;
                case 2: list = tenants.allTenants(); break;
                default: list = tenants.activeTenants(); break;
            }
            final String needle = search.getText().trim().toLowerCase();
            if (!needle.isEmpty()) {
                list = list.filter(t -> t.getName().toLowerCase().contains(needle)); // linear search
            }
            shown = list;
            model.setRowCount(0);
            for (Tenant t : shown) {
                model.addRow(new Object[] {t.getId(), t.getName(), t.getPhone(), t.getRoomNo(),
                        t.getMessPlanName() == null ? "No mess" : t.getMessPlanName(),
                        Dates.format(t.getJoinDate()), t.getStatus()});
            }
            status.setText(shown.size() + " tenant(s) shown" + (needle.isEmpty() ? "." : " for \"" + needle + "\"."));
        } catch (Exception e) {
            Ui.problem(this, e);
        }
    }

    private Tenant selected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            throw new IllegalArgumentException("Click a tenant in the table first.");
        }
        return shown.get(row);
    }

    /** Drop-down items for rooms that still have a free bed. */
    @SuppressWarnings("unchecked")
    private Choice<Room>[] freeRooms(int exceptRoomId) throws SQLException {
        DynamicArray<Room> free = rooms.vacantRooms().filter(r -> r.getId() != exceptRoomId);
        if (free.isEmpty()) {
            throw new IllegalArgumentException("No room has a free bed right now.");
        }
        Choice<Room>[] items = new Choice[free.size()];
        for (int i = 0; i < free.size(); i++) {
            Room r = free.get(i);
            items[i] = new Choice<Room>(r, r.getRoomNo() + "  -  " + r.getType().name().toLowerCase()
                    + (r.hasAc() ? ", AC" : "") + ", " + r.vacantBeds() + " free, " + Money.inr(r.getRentPerBed()));
        }
        return items;
    }

    /** Drop-down items for mess plans, with "No mess" first. */
    @SuppressWarnings("unchecked")
    private Choice<Integer>[] planChoices() throws SQLException {
        DynamicArray<MessPlan> plans = mess.plans();
        Choice<Integer>[] items = new Choice[plans.size() + 1];
        items[0] = new Choice<Integer>(null, "No mess");
        for (int i = 0; i < plans.size(); i++) {
            MessPlan p = plans.get(i);
            items[i + 1] = new Choice<Integer>(p.getId(), p.getName() + "  -  " + Money.inr(p.monthlyCharge()));
        }
        return items;
    }

    @SuppressWarnings("unchecked")
    private void register() {
        try {
            final String[] msg = {null};
        Form f = new Form();
            JTextField name = f.text("Full name", "");
            JTextField phone = f.text("Mobile number", "");
            JTextField email = f.text("Email (optional)", "");
            JComboBox<Gender> gender = f.combo("Gender", Gender.values());
            JTextField job = f.text("Occupation (optional)", "");
            JComboBox<Choice<Room>> room = f.combo("Room", freeRooms(-1));
            JComboBox<Choice<Integer>> plan = f.combo("Mess plan", planChoices());
            JTextField joined = f.text("Joining date", Dates.format(Dates.today()));
            JTextField deposit = f.text("Security deposit", "0");
            boolean saved = f.show(this, "Register a tenant", () -> {
                Tenant t = tenants.register(Form.required(name), phone.getText().trim(), Form.optional(email),
                        (Gender) gender.getSelectedItem(), Form.optional(job),
                        ((Choice<Room>) room.getSelectedItem()).value().getRoomNo(),
                        ((Choice<Integer>) plan.getSelectedItem()).value(),
                        Form.date(joined), Form.amountOrZero(deposit));
                msg[0] = "Registered " + t.getName() + " (ID " + t.getId() + ") in room " + t.getRoomNo() + ".";
            });
            if (saved) {
                show.setSelectedIndex(0);
                refresh();
                Ui.done(status, msg[0]);
            }
        } catch (Exception e) {
            Ui.problem(this, e);
        }
    }

    private void details() {
        try {
            Tenant t = selected();
            double unpaid = 0;
            for (DueLine l : rent.statement(t.getId())) {
                unpaid += l.pending();
            }
            DefaultTableModel m = Ui.model("Detail", "Value");
            m.addRow(new Object[] {"Tenant ID", t.getId()});
            m.addRow(new Object[] {"Name", t.getName()});
            m.addRow(new Object[] {"Phone", t.getPhone()});
            m.addRow(new Object[] {"Email", t.getEmail() == null ? "-" : t.getEmail()});
            m.addRow(new Object[] {"Gender", t.getGender()});
            m.addRow(new Object[] {"Occupation", t.getOccupation() == null ? "-" : t.getOccupation()});
            m.addRow(new Object[] {"Room", t.getRoomNo()});
            m.addRow(new Object[] {"Mess plan", t.getMessPlanName() == null ? "No mess" : t.getMessPlanName()});
            m.addRow(new Object[] {"Joined", Dates.format(t.getJoinDate())});
            m.addRow(new Object[] {"Left", Dates.format(t.getLeaveDate())});
            m.addRow(new Object[] {"Deposit held", Money.inr(t.getDeposit())});
            m.addRow(new Object[] {"Unpaid rent", Money.inr(unpaid)});
            m.addRow(new Object[] {"Status", t.getStatus()});
            Ui.tableDialog(this, "Tenant details", t.getName(), m, null);
        } catch (Exception e) {
            Ui.problem(this, e);
        }
    }

    @SuppressWarnings("unchecked")
    private void shift() {
        try {
            Tenant t = selected();
            if (!t.isActive()) {
                throw new IllegalArgumentException(t.getName() + " has already checked out.");
            }
            final String[] msg = {null};
        Form f = new Form();
            f.note(t.getName() + " is in room " + t.getRoomNo());
            JComboBox<Choice<Room>> room = f.combo("Move to", freeRooms(t.getRoomId()));
            boolean saved = f.show(this, "Shift room", () -> {
                Room to = ((Choice<Room>) room.getSelectedItem()).value();
                tenants.shiftRoom(t.getId(), to.getRoomNo());
                msg[0] = "Moved " + t.getName() + " from " + t.getRoomNo() + " to " + to.getRoomNo() + ".";
            });
            if (saved) {
                refresh();
                Ui.done(status, msg[0]);
            }
        } catch (Exception e) {
            Ui.problem(this, e);
        }
    }

    @SuppressWarnings("unchecked")
    private void changePlan() {
        try {
            Tenant t = selected();
            if (!t.isActive()) {
                throw new IllegalArgumentException(t.getName() + " has already checked out.");
            }
            final String[] msg = {null};
        Form f = new Form();
            f.note(t.getName() + " is on: " + (t.getMessPlanName() == null ? "No mess" : t.getMessPlanName()));
            JComboBox<Choice<Integer>> plan = f.combo("New plan", planChoices());
            boolean saved = f.show(this, "Change mess plan", () -> {
                Choice<Integer> c = (Choice<Integer>) plan.getSelectedItem();
                tenants.changeMessPlan(t.getId(), c.value());
                msg[0] = t.getName() + " is now on " + c + ". Bills already made keep the old plan.";
            });
            if (saved) {
                refresh();
                Ui.done(status, msg[0]);
            }
        } catch (Exception e) {
            Ui.problem(this, e);
        }
    }

    private void checkout() {
        try {
            Tenant t = selected();
            if (!t.isActive()) {
                throw new IllegalArgumentException(t.getName() + " has already checked out.");
            }
            double unpaid = 0;
            for (DueLine l : rent.statement(t.getId())) {
                unpaid += l.pending();
            }
            final String[] msg = {null};
        Form f = new Form();
            f.note(t.getName() + ", room " + t.getRoomNo());
            f.note("Deposit held: " + Money.inr(t.getDeposit()) + "     Unpaid rent: " + Money.inr(unpaid));
            if (unpaid > 0) {
                f.note("Settle the unpaid rent (or adjust it from the deposit) first.");
            }
            JTextField date = f.text("Leaving date", Dates.format(Dates.today()));
            boolean saved = f.show(this, "Check out", () -> {
                tenants.checkout(t.getId(), Form.date(date));
                msg[0] = t.getName() + " checked out. Room " + t.getRoomNo() + " has a free bed again.";
            });
            if (saved) {
                refresh();
                Ui.done(status, msg[0]);
            }
        } catch (Exception e) {
            Ui.problem(this, e);
        }
    }
}
