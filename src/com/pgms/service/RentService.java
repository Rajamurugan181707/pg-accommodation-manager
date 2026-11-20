package com.pgms.service;

import com.pgms.dao.BillDAO;
import com.pgms.dao.RentDAO;
import com.pgms.dao.RoomDAO;
import com.pgms.ds.DynamicArray;
import com.pgms.model.Billable;
import com.pgms.model.MessPlan;
import com.pgms.model.PaymentMode;
import com.pgms.model.RentPayment;
import com.pgms.model.Room;
import com.pgms.model.Tenant;
import com.pgms.util.Check;
import com.pgms.util.Dates;
import com.pgms.util.Money;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.Date;

/**
 * RENT module rules.
 *
 * Monthly bill = bed rent + mess plan charge (if the tenant is in mess).
 * A tenant is billed for every month from the month they joined up to the
 * month they left (or the current month if they are still staying).
 *
 * The first time a month is looked at, each tenant's bill for it is saved in
 * monthly_bills using the Room and MessPlan objects (both Billable). After
 * that the saved amounts are used, so changing a rent, shifting a room or
 * changing a mess plan only affects bills made from then on.
 */
public class RentService {

    private final RentDAO rentDao = new RentDAO();
    private final RoomDAO roomDao = new RoomDAO();
    private final BillDAO billDao = new BillDAO();
    private final TenantService tenants = new TenantService();
    private final MessService mess = new MessService();

    /** Is the tenant billed for this month? */
    public static boolean billableIn(Tenant t, String month) {
        boolean joined = Dates.compareMonths(Dates.monthOf(t.getJoinDate()), month) <= 0;
        boolean stillHere = t.getLeaveDate() == null
                || Dates.compareMonths(Dates.monthOf(t.getLeaveDate()), month) >= 0;
        return joined && stillHere;
    }

    /** Bills are frozen once made, so a month that has not started yet must not be billed early. */
    private static void notFutureMonth(String month) {
        if (Dates.compareMonths(month, Dates.currentMonth()) > 0) {
            throw new IllegalArgumentException("Bills for " + month + " are not made yet. They are made when "
                    + month + " starts, at the rent and mess charges of that time.");
        }
    }

    /** Every billed tenant's dues for the month, biggest pending amount first. */
    public DynamicArray<DueLine> duesForMonth(String month) throws SQLException {
        notFutureMonth(month);
        DynamicArray<Tenant> billed = new DynamicArray<Tenant>();
        for (Tenant t : tenants.allTenants()) {
            if (billableIn(t, month)) {
                billed.add(t);
            }
        }
        makeMissingBills(billed, month);

        DynamicArray<BillDAO.SavedBill> bills = billDao.findByMonth(month);
        DynamicArray<RentPayment> payments = rentDao.findByMonth(month);
        DynamicArray<DueLine> lines = new DynamicArray<DueLine>();
        for (Tenant t : billed) {
            lines.add(new DueLine(t, month, itemsOf(t.getId(), month, bills), paidBy(t.getId(), month, payments)));
        }
        lines.sort(new Comparator<DueLine>() {
            @Override
            public int compare(DueLine a, DueLine b) {
                return Double.compare(b.pending(), a.pending());
            }
        });
        return lines;
    }

    public DueLine dueFor(int tenantId, String month) throws SQLException {
        notFutureMonth(month);
        Tenant t = tenants.requireTenant(tenantId);
        if (!billableIn(t, month)) {
            throw new IllegalArgumentException(t.getName() + " was not staying in " + month);
        }
        DynamicArray<Tenant> one = new DynamicArray<Tenant>(1);
        one.add(t);
        makeMissingBills(one, month);
        return new DueLine(t, month, itemsOf(tenantId, month, billDao.findByTenant(tenantId)),
                paidBy(tenantId, month, rentDao.findByTenant(tenantId)));
    }

    public RentPayment recordPayment(int tenantId, String month, double amount, Date paidOn,
                                     PaymentMode mode, String note) throws SQLException {
        Check.notFuture(paidOn, "Payment date");
        amount = Check.money(amount, false, "Amount");
        DueLine line = dueFor(tenantId, month);
        if (line.pending() == 0) {
            throw new IllegalArgumentException(line.getTenant().getName() + " has already paid in full for " + month);
        }
        if (amount > line.pending()) {
            throw new IllegalArgumentException("Only " + Money.inr(line.pending())
                    + " is pending for " + month + "; amount is more than that");
        }
        RentPayment p = new RentPayment(0, tenantId, month, amount, paidOn, mode, note);
        rentDao.record(p);
        return p;
    }

    /** Month-by-month bill for one tenant, from joining to now (or leaving). */
    public DynamicArray<DueLine> statement(int tenantId) throws SQLException {
        Tenant t = tenants.requireTenant(tenantId);
        String last = t.getLeaveDate() != null ? Dates.monthOf(t.getLeaveDate()) : Dates.currentMonth();

        DynamicArray<Tenant> one = new DynamicArray<Tenant>(1);
        one.add(t);
        for (String m = Dates.monthOf(t.getJoinDate()); Dates.compareMonths(m, last) <= 0; m = Dates.nextMonth(m)) {
            makeMissingBills(one, m);
        }

        DynamicArray<BillDAO.SavedBill> bills = billDao.findByTenant(tenantId);
        DynamicArray<RentPayment> payments = rentDao.findByTenant(tenantId);
        DynamicArray<DueLine> months = new DynamicArray<DueLine>();
        for (String m = Dates.monthOf(t.getJoinDate()); Dates.compareMonths(m, last) <= 0; m = Dates.nextMonth(m)) {
            months.add(new DueLine(t, m, itemsOf(tenantId, m, bills), paidBy(tenantId, m, payments)));
        }
        return months;
    }

    public DynamicArray<RentPayment> paymentsOf(int tenantId) throws SQLException {
        return rentDao.findByTenant(tenantId);
    }

    public DynamicArray<RentPayment> paymentsIn(String month) throws SQLException {
        return rentDao.findByMonth(month);
    }

    /**
     * Called once when the app starts: makes every missing bill, for every
     * tenant, from their joining month up to now (or their leaving month).
     * This freezes the past before anyone can change a rent, room or plan.
     * Returns how many new bills were made.
     */
    public int billEveryoneUpToNow() throws SQLException {
        DynamicArray<Room> rooms = roomDao.findAll();
        DynamicArray<MessPlan> plans = mess.plans();
        String now = Dates.currentMonth();
        int made = 0;
        for (Tenant t : tenants.allTenants()) {
            DynamicArray<BillDAO.SavedBill> have = billDao.findByTenant(t.getId());
            String last = t.getLeaveDate() != null ? Dates.monthOf(t.getLeaveDate()) : now;
            for (String m = Dates.monthOf(t.getJoinDate()); Dates.compareMonths(m, last) <= 0; m = Dates.nextMonth(m)) {
                if (itemsOf(t.getId(), m, have) == null) {
                    billDao.saveIfMissing(t.getId(), m, roomOf(t, rooms), planOf(t, plans));
                    made++;
                }
            }
        }
        return made;
    }

    /** Save a bill for each tenant that does not have one for the month yet. */
    private void makeMissingBills(DynamicArray<Tenant> list, String month) throws SQLException {
        DynamicArray<Room> rooms = null;
        DynamicArray<MessPlan> plans = null;
        DynamicArray<BillDAO.SavedBill> existing = billDao.findByMonth(month);
        for (Tenant t : list) {
            if (itemsOf(t.getId(), month, existing) != null) {
                continue;
            }
            if (rooms == null) {
                rooms = roomDao.findAll();
                plans = mess.plans();
            }
            billDao.saveIfMissing(t.getId(), month, roomOf(t, rooms), planOf(t, plans));
        }
    }

    /** The saved bill's lines for this tenant and month, or null if there is none. */
    private static Billable[] itemsOf(int tenantId, String month, DynamicArray<BillDAO.SavedBill> bills) {
        for (BillDAO.SavedBill b : bills) {
            if (b.tenantId == tenantId && b.month.equals(month)) {
                return b.items;
            }
        }
        return null;
    }

    private static Room roomOf(Tenant t, DynamicArray<Room> rooms) {
        for (Room r : rooms) {
            if (r.getId() == t.getRoomId()) {
                return r;
            }
        }
        throw new IllegalStateException("Room missing for tenant " + t.getId());
    }

    private static MessPlan planOf(Tenant t, DynamicArray<MessPlan> plans) {
        if (t.getMessPlanId() == null) {
            return null;
        }
        for (MessPlan p : plans) {
            if (p.getId() == t.getMessPlanId()) {
                return p;
            }
        }
        return null;
    }

    private static double paidBy(int tenantId, String month, DynamicArray<RentPayment> payments) {
        double sum = 0;
        for (RentPayment p : payments) {
            if (p.getTenantId() == tenantId && p.getBillMonth().equals(month)) {
                sum += p.getAmount();
            }
        }
        return sum;
    }

    public RentPayment requirePayment(int paymentId) throws SQLException {
        RentPayment p = rentDao.findById(paymentId);
        if (p == null) {
            throw new IllegalArgumentException("No receipt #" + paymentId);
        }
        return p;
    }

    /** Correct a payment. The month's payments may still not add up to more than its bill. */
    public RentPayment editPayment(int paymentId, double amount, Date paidOn, PaymentMode mode, String note)
            throws SQLException {
        RentPayment old = requirePayment(paymentId);
        RentPayment checked = new RentPayment(paymentId, old.getTenantId(), old.getBillMonth(), amount,
                paidOn, mode, note);   // checks amount and note
        Check.notFuture(paidOn, "Payment date");
        DueLine line = dueFor(old.getTenantId(), old.getBillMonth());
        double otherPayments = Check.paise(line.paid() - old.getAmount());
        if (Check.paise(otherPayments + checked.getAmount()) > line.due()) {
            throw new IllegalArgumentException("That would make " + Money.inr(otherPayments + checked.getAmount())
                    + " paid against a bill of " + Money.inr(line.due()) + " for " + old.getBillMonth()
                    + ". The most this receipt can be is " + Money.inr(line.due() - otherPayments) + ".");
        }
        rentDao.update(paymentId, checked.getAmount(), paidOn, mode, checked.getNote());
        return requirePayment(paymentId);
    }

    public void deletePayment(int paymentId) throws SQLException {
        requirePayment(paymentId);
        rentDao.delete(paymentId);
    }

    /**
     * Correct one month's saved bill (e.g. a discount, or a wrong charge).
     * The new total cannot be less than what has already been paid for that month.
     */
    public DueLine editBill(int tenantId, String month, double roomRent, double messCharge) throws SQLException {
        DueLine line = dueFor(tenantId, month);   // makes the bill if missing; refuses future months
        double rent = Check.money(roomRent, false, "Room rent");
        double mess = Check.money(messCharge, true, "Mess charge");
        if (Check.paise(rent + mess) < line.paid()) {
            throw new IllegalArgumentException(line.getTenant().getName() + " has already paid "
                    + Money.inr(line.paid()) + " for " + month + ", so the bill cannot be less than that");
        }
        billDao.updateAmounts(tenantId, month, rent, mess);
        return dueFor(tenantId, month);
    }
}
