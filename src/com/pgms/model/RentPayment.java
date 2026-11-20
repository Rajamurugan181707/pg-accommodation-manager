package com.pgms.model;

import com.pgms.util.Check;

import java.util.Date;

/** RENT module entity: one payment made by a tenant towards a month's bill. */
public class RentPayment {

    private int id;
    private final int tenantId;
    private String tenantName;      // filled from a JOIN, for display
    private final String billMonth; // "YYYY-MM"
    private final double amount;
    private final Date paidOn;
    private final PaymentMode mode;
    private final String note;

    public RentPayment(int id, int tenantId, String billMonth, double amount,
                       Date paidOn, PaymentMode mode, String note) {
        if (billMonth == null || !billMonth.matches("[0-9]{4}-(0[1-9]|1[0-2])")) {
            throw new IllegalArgumentException("Month must look like 2026-11");
        }
        this.id = id;
        this.tenantId = tenantId;
        this.billMonth = billMonth;
        this.amount = Check.money(amount, false, "Amount");
        this.paidOn = paidOn;
        this.mode = mode;
        this.note = Check.text(note, 80, "Note", false);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTenantId() {
        return tenantId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getBillMonth() {
        return billMonth;
    }

    public double getAmount() {
        return amount;
    }

    public Date getPaidOn() {
        return paidOn;
    }

    public PaymentMode getMode() {
        return mode;
    }

    public String getNote() {
        return note;
    }
}
