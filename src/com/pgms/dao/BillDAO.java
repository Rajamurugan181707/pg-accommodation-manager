package com.pgms.dao;

import com.pgms.db.DBConnection;
import com.pgms.ds.DynamicArray;
import com.pgms.model.Billable;
import com.pgms.model.BillItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/** JDBC access for monthly_bills: saved, never-changing bills. */
public class BillDAO {

    /** A saved bill read back from the table. */
    public static class SavedBill {
        public final int tenantId;
        public final String month;
        public final Billable[] items;

        SavedBill(int tenantId, String month, Billable[] items) {
            this.tenantId = tenantId;
            this.month = month;
            this.items = items;
        }
    }

    /**
     * Save a bill unless one already exists for that tenant and month.
     * INSERT IGNORE keeps the first bill, so old bills are never overwritten.
     */
    public void saveIfMissing(int tenantId, String month, Billable room, Billable mess) throws SQLException {
        String sql = "INSERT IGNORE INTO monthly_bills "
                + "(tenant_id, bill_month, room_label, room_rent, mess_label, mess_charge, created_on) "
                + "VALUES (?, ?, ?, ?, ?, ?, CURDATE())";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, tenantId);
            ps.setString(2, month);
            ps.setString(3, room.billLabel());
            ps.setDouble(4, room.monthlyCharge());
            if (mess == null) {
                ps.setNull(5, Types.VARCHAR);
                ps.setDouble(6, 0);
            } else {
                ps.setString(5, mess.billLabel());
                ps.setDouble(6, mess.monthlyCharge());
            }
            ps.executeUpdate();
        }
    }

    public DynamicArray<SavedBill> findByMonth(String month) throws SQLException {
        return query("SELECT * FROM monthly_bills WHERE bill_month = ?", month);
    }

    public DynamicArray<SavedBill> findByTenant(int tenantId) throws SQLException {
        return query("SELECT * FROM monthly_bills WHERE tenant_id = ? ORDER BY bill_month", tenantId);
    }

    private DynamicArray<SavedBill> query(String sql, Object param) throws SQLException {
        DynamicArray<SavedBill> list = new DynamicArray<SavedBill>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Billable roomLine = new BillItem(rs.getString("room_label"), rs.getDouble("room_rent"));
                    String messLabel = rs.getString("mess_label");
                    Billable[] items = messLabel == null
                            ? new Billable[] {roomLine}
                            : new Billable[] {roomLine, new BillItem(messLabel, rs.getDouble("mess_charge"))};
                    list.add(new SavedBill(rs.getInt("tenant_id"), rs.getString("bill_month"), items));
                }
            }
        }
        return list;
    }
}
