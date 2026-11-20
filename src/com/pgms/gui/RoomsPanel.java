package com.pgms.gui;

import com.pgms.ds.DynamicArray;
import com.pgms.model.Room;
import com.pgms.model.RoomType;
import com.pgms.service.RoomService;
import com.pgms.util.Money;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;

/** ROOM module screen: a toolbar and a grid of room cards. */
public class RoomsPanel extends JPanel implements Screen {

    private static final long serialVersionUID = 1L;

    private final RoomService rooms = new RoomService();
    private final PgField findBox = Ui.searchField(9);
    private final JCheckBox freeOnly = Ui.check("Only rooms with free beds");
    private final JComboBox<String> sortBy = Ui.combo(new String[] {"Sort by room number", "Sort by rent"});
    private final JLabel status = Ui.bold(" ");
    private final JPanel grid = new JPanel(new GridLayout(0, 4, 16, 16));
    private DynamicArray<Room> shown = new DynamicArray<Room>();
    private final DynamicArray<RoomCard> cards = new DynamicArray<RoomCard>();
    private Room selected;

    public RoomsPanel() {
        super(new BorderLayout(0, 16));
        setOpaque(false);

        JButton find = Ui.secondary("Find room", "search");
        JButton add = Ui.primary("Add room", "plus");
        JButton rent = Ui.secondary("Edit room", "edit");
        JButton delete = Ui.secondary("Delete room", "trash");
        findBox.setName("Room number");
        findBox.setToolTipText("Type a room number, e.g. 102");
        findBox.setHint("e.g. 102");

        RoundPanel bar = new RoundPanel(new BorderLayout(0, 14), 18);
        bar.add(Ui.row(findBox, find, Ui.gap(6), freeOnly, sortBy), BorderLayout.NORTH);
        bar.add(Ui.row(add, rent, delete, Ui.gap(8), status), BorderLayout.SOUTH);
        add(bar, BorderLayout.NORTH);

        grid.setOpaque(false);
        JPanel holder = new JPanel(new BorderLayout());
        holder.setOpaque(false);
        holder.add(grid, BorderLayout.NORTH);
        add(Ui.clearScroll(holder), BorderLayout.CENTER);

        find.addActionListener(e -> find());
        findBox.addActionListener(e -> find());
        freeOnly.addActionListener(e -> refresh());
        sortBy.addActionListener(e -> refresh());
        add.addActionListener(e -> addRoom());
        rent.addActionListener(e -> editRoom());
        delete.addActionListener(e -> deleteRoom());
    }

    @Override
    public void refresh() {
        try {
            shown = sortBy.getSelectedIndex() == 1 ? rooms.roomsSortedByRent() : rooms.allRooms();
            if (freeOnly.isSelected()) {
                shown = shown.filter(r -> !r.isFull());
            }
            grid.removeAll();
            while (!cards.isEmpty()) {
                cards.removeAt(cards.size() - 1);
            }
            Room keep = null;
            for (Room r : shown) {
                final Room room = r;
                RoomCard card = new RoomCard(r, () -> select(room));
                if (selected != null && selected.getId() == r.getId()) {
                    keep = r;
                    card.setSelected(true);
                }
                cards.add(card);
                grid.add(card);
            }
            selected = keep;
            grid.revalidate();
            grid.repaint();
            status.setText(shown.size() + " room(s) shown." + (selected == null
                    ? "  Click a card to select it." : "  Room " + selected.getRoomNo() + " is selected."));
        } catch (Exception e) {
            Ui.problem(this, e);
        }
    }

    private void select(Room r) {
        selected = r;
        for (RoomCard c : cards) {
            c.setSelected(c.room().getId() == r.getId());
        }
        status.setText("Room " + r.getRoomNo() + " is selected: " + r.getType().name().toLowerCase() + ", "
                + (r.isFull() ? "full" : r.vacantBeds() + " bed(s) free") + ", " + Money.inr(r.getRentPerBed()) + " per bed.");
    }

    /** Used by tests: select the n-th card on screen. */
    void selectIndex(int n) {
        select(shown.get(n));
    }

    /** Binary search in RoomService, then select and scroll to the card. */
    private void find() {
        String no = findBox.getText().trim();
        if (no.isEmpty()) {
            return;
        }
        try {
            final Room r = rooms.findByRoomNo(no);
            if (r == null) {
                status.setText("No room numbered " + no + " (binary search found nothing).");
                return;
            }
            int index = shown.indexOf(x -> x.getId() == r.getId());
            if (index < 0) {
                freeOnly.setSelected(false);
                refresh();
                index = shown.indexOf(x -> x.getId() == r.getId());
            }
            select(r);
            RoomCard card = cards.get(index);
            grid.scrollRectToVisible(new Rectangle(card.getBounds()));
            status.setText("Found room " + r.getRoomNo() + " by binary search: " + r.getType().name().toLowerCase()
                    + ", " + (r.isFull() ? "full" : r.vacantBeds() + " bed(s) free") + ".");
        } catch (Exception e) {
            Ui.problem(this, e);
        }
    }

    private Room selected() {
        if (selected == null) {
            throw new IllegalArgumentException("Click a room card first.");
        }
        return selected;
    }

    private void addRoom() {
        final String[] msg = {null};
        Form f = new Form();
        JTextField no = f.text("Room number", "");
        JTextField floor = f.text("Floor (0 = ground)", "0");
        JComboBox<RoomType> type = f.combo("Type", RoomType.values());
        JTextField rent = f.text("Rent per bed", "");
        JCheckBox ac = f.check("AC room");
        boolean saved = f.show(this, "Add a room", () -> {
            Room r = rooms.addRoom(Form.required(no), Form.whole(floor, 0, 50),
                    (RoomType) type.getSelectedItem(), Form.amount(rent), ac.isSelected());
            msg[0] = "Room " + r.getRoomNo() + " added with " + r.getCapacity() + " bed(s).";
        });
        if (saved) {
            refresh();
            Ui.done(status, msg[0]);
        }
    }

    private void editRoom() {
        try {
            final Room r = selected();
            final String[] msg = {null};
            Form f = new Form();
            f.note("Editing room " + r.getRoomNo() + " (" + r.getOccupied() + " tenant(s) in it now)");
            JTextField no = f.text("Room number", r.getRoomNo());
            JTextField floor = f.text("Floor (0 = ground)", String.valueOf(r.getFloor()));
            JComboBox<RoomType> type = f.combo("Type", RoomType.values());
            type.setSelectedItem(r.getType());
            JTextField rent = f.text("Rent per bed", Money.plain(r.getRentPerBed()));
            JCheckBox ac = f.check("AC room");
            ac.setSelected(r.hasAc());
            boolean saved = f.show(this, "Edit room", () -> {
                Room updated = rooms.editRoom(r.getRoomNo(), Form.required(no), Form.whole(floor, 0, 50),
                        (RoomType) type.getSelectedItem(), Form.amount(rent), ac.isSelected());
                msg[0] = "Room " + updated.getRoomNo() + " saved. Bills already made keep their old rent.";
                selected = updated;
            });
            if (saved) {
                refresh();
                Ui.done(status, msg[0]);
            }
        } catch (Exception e) {
            Ui.problem(this, e);
        }
    }

    private void deleteRoom() {
        try {
            Room r = selected();
            if (Ui.confirm(this, "Delete room", "Delete room " + r.getRoomNo() + "? This cannot be undone.")) {
                rooms.deleteRoom(r.getRoomNo());
                selected = null;
                refresh();
                Ui.done(status, "Room " + r.getRoomNo() + " deleted.");
            }
        } catch (Exception e) {
            Ui.problem(this, e);
        }
    }
}
