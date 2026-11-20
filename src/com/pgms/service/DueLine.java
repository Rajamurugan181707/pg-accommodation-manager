package com.pgms.service;

import com.pgms.model.Billable;
import com.pgms.model.Tenant;
import com.pgms.util.Check;

/** One tenant's bill for one month: what they are charged and what they have paid. */
public class DueLine {

    private final Tenant tenant;
    private final String month;
    private final Billable[] items;   // a plain array: the bed, and the mess plan if any
    private final double paid;

    public DueLine(Tenant tenant, String month, Billable[] items, double paid) {
        this.tenant = tenant;
        this.month = month;
        this.items = items;
        this.paid = paid;
    }

    /** Polymorphism: each item works out its own charge. */
    public double due() {
        double total = 0;
        for (Billable item : items) {
            total += item.monthlyCharge();
        }
        return Check.paise(total);
    }

    public double paid() {
        return Check.paise(paid);
    }

    public double pending() {
        return Math.max(0, Check.paise(due() - paid));
    }

    public String status() {
        if (pending() == 0) {
            return "PAID";
        }
        return paid > 0 ? "PARTIAL" : "UNPAID";
    }

    public Tenant getTenant() {
        return tenant;
    }

    public String getMonth() {
        return month;
    }

    public Billable[] getItems() {
        return items;
    }
}
