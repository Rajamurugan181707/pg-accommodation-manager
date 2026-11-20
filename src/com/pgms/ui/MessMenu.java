package com.pgms.ui;

import com.pgms.ds.DynamicArray;
import com.pgms.model.DayMenu;
import com.pgms.model.MessPlan;
import com.pgms.model.Tenant;
import com.pgms.service.MessService;
import com.pgms.service.TenantService;
import com.pgms.util.Money;
import com.pgms.util.Table;

import java.sql.SQLException;

/** Screens for the MESS module. */
public class MessMenu {

    private static final String[] DAYS = {
        "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"
    };

    private final ConsoleInput in;
    private final MessService mess = new MessService();
    private final TenantService tenants = new TenantService();
    private final TenantMenu tenantMenu;

    public MessMenu(ConsoleInput in) {
        this.in = in;
        this.tenantMenu = new TenantMenu(in);
    }

    public void show() throws SQLException {
        while (true) {
            System.out.println();
            System.out.println("------ MESS MODULE ------");
            System.out.println("1. View mess plans and headcount");
            System.out.println("2. Add a mess plan");
            System.out.println("3. Change a tenant's mess plan");
            System.out.println("4. View this week's menu");
            System.out.println("5. Change the menu for a day");
            System.out.println("0. Back");
            int choice = in.number("Choose: ", 0, 5);
            try {
                switch (choice) {
                    case 1: plans(); break;
                    case 2: addPlan(); break;
                    case 3: changePlan(); break;
                    case 4: menu(); break;
                    case 5: changeMenu(); break;
                    default: return;
                }
            } catch (IllegalArgumentException e) {
                System.out.println("  ! " + e.getMessage());
            }
        }
    }

    private void plans() throws SQLException {
        DynamicArray<MessPlan> plans = mess.plans();
        int[] heads = mess.headcount(plans);
        Table t = new Table("ID", "Plan", "Meals/day", "Per month", "Tenants");
        int total = 0;
        double income = 0;
        for (int i = 0; i < plans.size(); i++) {
            MessPlan p = plans.get(i);
            t.addRow(p.getId(), p.getName(), p.getMealsPerDay(), Money.inr(p.monthlyCharge()), heads[i]);
            total += heads[i];
            income += heads[i] * p.monthlyCharge();
        }
        t.print();
        System.out.println("  " + total + " tenants eat in the mess. Mess income per month: " + Money.inr(income));
    }

    private void addPlan() throws SQLException {
        String name = in.text("Plan name: ");
        int meals = in.number("Meals per day (1-3): ", 1, 3);
        double charge = in.amount("Charge per month: ");
        MessPlan p = mess.addPlan(name, meals, charge);
        System.out.println("  Plan '" + p.getName() + "' added with ID " + p.getId() + ".");
    }

    private void changePlan() throws SQLException {
        int id = in.number("Tenant ID: ", 1, Integer.MAX_VALUE);
        Tenant t = tenants.requireActive(id);
        System.out.println("  " + t.getName() + " is on: "
                + (t.getMessPlanName() == null ? "No mess" : t.getMessPlanName()));
        Integer planId = tenantMenu.askPlan();
        tenants.changeMessPlan(id, planId);
        System.out.println("  Mess plan updated. Bills already made keep the old plan; new bills use this one.");
    }

    private void menu() throws SQLException {
        Table t = new Table("Day", "Breakfast", "Lunch", "Dinner");
        for (DayMenu d : mess.weeklyMenu()) {
            t.addRow(d.getDay(), d.getBreakfast(), d.getLunch(), d.getDinner());
        }
        t.print();
    }

    private void changeMenu() throws SQLException {
        for (int i = 0; i < DAYS.length; i++) {
            System.out.println("  " + (i + 1) + ". " + DAYS[i]);
        }
        String day = DAYS[in.number("Day: ", 1, 7) - 1];
        String b = in.text("Breakfast: ");
        String l = in.text("Lunch: ");
        String d = in.text("Dinner: ");
        mess.updateMenu(day, b, l, d);
        System.out.println("  Menu for " + day + " saved.");
    }
}
