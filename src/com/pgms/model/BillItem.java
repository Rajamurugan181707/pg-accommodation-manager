package com.pgms.model;

/**
 * One line of a saved monthly bill (the bed, or the mess plan), with the
 * amount frozen when the bill was made. Also Billable, so a bill can be
 * totalled the same way whether it holds live Room / MessPlan objects or
 * saved lines.
 */
public class BillItem implements Billable {

    private final String label;
    private final double amount;

    public BillItem(String label, double amount) {
        this.label = label;
        this.amount = amount;
    }

    @Override
    public double monthlyCharge() {
        return amount;
    }

    @Override
    public String billLabel() {
        return label;
    }
}
