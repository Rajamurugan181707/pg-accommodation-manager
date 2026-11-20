package com.pgms.model;

import com.pgms.util.Check;

/** MESS module entity: a monthly meal plan a tenant can opt into. */
public class MessPlan implements Billable {

    private int id;
    private String name;
    private int mealsPerDay;
    private double monthlyCharge;

    public MessPlan(int id, String name, int mealsPerDay, double monthlyCharge) {
        if (mealsPerDay < 1 || mealsPerDay > 3) {
            throw new IllegalArgumentException("Meals per day must be 1, 2 or 3");
        }
        this.id = id;
        this.name = Check.text(name, 40, "Plan name", true);
        this.mealsPerDay = mealsPerDay;
        this.monthlyCharge = Check.money(monthlyCharge, false, "Mess charge");
    }

    @Override
    public double monthlyCharge() {
        return monthlyCharge;
    }

    @Override
    public String billLabel() {
        return "Mess: " + name + " (" + mealsPerDay + " meal" + (mealsPerDay > 1 ? "s" : "") + "/day)";
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getMealsPerDay() {
        return mealsPerDay;
    }
}
