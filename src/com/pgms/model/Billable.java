package com.pgms.model;

/**
 * OOPS: INTERFACE + POLYMORPHISM.
 * Anything a tenant is charged for every month (a bed, a mess plan).
 * The rent module adds up an array of Billable items without caring
 * which concrete class each one is.
 */
public interface Billable {

    /** Amount charged per month, in rupees. */
    double monthlyCharge();

    /** Short label for bills, e.g. "Room 102 (bed)" or "Mess: Full board". */
    String billLabel();
}
