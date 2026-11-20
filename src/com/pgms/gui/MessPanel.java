package com.pgms.gui;

import com.pgms.ds.DynamicArray;
import com.pgms.model.DayMenu;
import com.pgms.model.MessPlan;
import com.pgms.service.MessService;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** MESS module screen: plan cards with headcount, and the weekly menu. */
public class MessPanel extends JPanel implements Screen {

    private static final long serialVersionUID = 1L;

    private final MessService mess = new MessService();
    private final JPanel planGrid = new JPanel(new GridLayout(1, 0, 14, 0));
    private final DefaultTableModel menu = Ui.model("Day", "Breakfast", "Lunch", "Dinner");
    private final JTable menuTable = Ui.table(menu);
    private final JLabel summary = Ui.bold(" ");
    private DynamicArray<DayMenu> week = new DynamicArray<DayMenu>();

    public MessPanel() {
        super(new BorderLayout(0, 16));
        setOpaque(false);

        JButton addPlan = Ui.primary("Add mess plan", "plus");
        RoundPanel plans = new RoundPanel(new BorderLayout(0, 14), 18);
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.add(Ui.heading("Mess plans", "bowl"), BorderLayout.WEST);
        head.add(Ui.rowRight(summary, addPlan), BorderLayout.EAST);
        plans.add(head, BorderLayout.NORTH);
        planGrid.setOpaque(false);
        plans.add(planGrid, BorderLayout.CENTER);
        plans.add(Ui.small("TO MOVE A TENANT TO ANOTHER PLAN, USE  TENANTS  >  CHANGE MESS PLAN"), BorderLayout.SOUTH);
        add(plans, BorderLayout.NORTH);

        JButton editDay = Ui.secondary("Edit selected day", "edit");
        RoundPanel week = new RoundPanel(new BorderLayout(0, 14), 18);
        JPanel head2 = new JPanel(new BorderLayout());
        head2.setOpaque(false);
        head2.add(Ui.heading("This week's menu", "calendar"), BorderLayout.WEST);
        head2.add(Ui.rowRight(editDay), BorderLayout.EAST);
        week.add(head2, BorderLayout.NORTH);
        Ui.widths(menuTable, 170, 220, 260, 220);
        week.add(Ui.scroll(menuTable), BorderLayout.CENTER);
        add(week, BorderLayout.CENTER);

        addPlan.addActionListener(e -> addPlan());
        editDay.addActionListener(e -> editDay());
    }

    @Override
    public void refresh() {
        try {
            DynamicArray<MessPlan> list = mess.plans();
            int[] heads = mess.headcount(list);
            planGrid.removeAll();
            int eaters = 0;
            double income = 0;
            for (int i = 0; i < list.size(); i++) {
                MessPlan p = list.get(i);
                planGrid.add(new PlanCard(p, heads[i], i));
                eaters += heads[i];
                income += heads[i] * p.monthlyCharge();
            }
            planGrid.revalidate();
            planGrid.repaint();
            summary.setText(eaters + " eating  |  " + Money.inr(income) + " a month   ");

            String today = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(new Date()).toUpperCase();
            week = mess.weeklyMenu();
            menu.setRowCount(0);
            for (DayMenu d : week) {
                String day = d.getDay().equals(today) ? d.getDay() + "  (TODAY)" : d.getDay();
                menu.addRow(new Object[] {day, d.getBreakfast(), d.getLunch(), d.getDinner()});
            }
            for (int i = 0; i < week.size(); i++) {
                if (week.get(i).getDay().equals(today)) {
                    menuTable.setRowSelectionInterval(i, i);
                }
            }
        } catch (Exception e) {
            Ui.problem(this, e);
        }
    }

    private void addPlan() {
        Form f = new Form();
        JTextField name = f.text("Plan name", "");
        JComboBox<Integer> meals = f.combo("Meals per day", new Integer[] {1, 2, 3});
        JTextField charge = f.text("Charge per month", "");
        final String[] msg = {null};
        boolean saved = f.show(this, "Add mess plan", () -> {
            MessPlan p = mess.addPlan(Form.required(name), (Integer) meals.getSelectedItem(), Form.amount(charge));
            msg[0] = "Plan '" + p.getName() + "' added.";
        });
        if (saved) {
            refresh();
            Toast.show(this, msg[0]);
        }
    }

    private void editDay() {
        int row = menuTable.getSelectedRow();
        if (row < 0) {
            Ui.error(this, "Click a day in the menu first.");
            return;
        }
        DayMenu d = week.get(row);
        Form f = new Form();
        f.note(d.getDay());
        JTextField b = f.text("Breakfast", d.getBreakfast());
        JTextField l = f.text("Lunch", d.getLunch());
        JTextField n = f.text("Dinner", d.getDinner());
        boolean saved = f.show(this, "Edit menu", () ->
                mess.updateMenu(d.getDay(), Form.required(b), Form.required(l), Form.required(n)));
        if (saved) {
            refresh();
            Toast.show(this, "Menu for " + d.getDay() + " saved.");
        }
    }
}
