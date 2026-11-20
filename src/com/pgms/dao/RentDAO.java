package com.pgms.dao;

import com.pgms.db.DBConnection;
import com.pgms.ds.DynamicArray;
import com.pgms.model.PaymentMode;
import com.pgms.model.RentPayment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** JDBC access for the rent_payments table. */
public class RentDAO {

    private static final String SELECT =
            "SELECT p.*, t.name FROM rent_payments p JOIN tenants t ON t.tenant_id = p.tenant_id ";

    public int record(RentPayment p) throws SQLException {
        String sql = "INSERT INTO rent_payments (tenant_id, bill_month, amount, paid_on, mode, note) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getTenantId());
            ps.setString(2, p.getBillMonth());
            ps.setDouble(3, p.getAmount());
            ps.setDate(4, new java.sql.Date(p.getPaidOn().getTime()));
            ps.setString(5, p.getMode().name());
            ps.setString(6, p.getNote());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                p.setId(keys.getInt(1));
                return p.getId();
            }
        }
    }

    public DynamicArray<RentPayment> findByMonth(String month) throws SQLException {
        return query(SELECT + "WHERE p.bill_month = ? ORDER BY p.paid_on, p.payment_id", month);
    }

    public DynamicArray<RentPayment> findByTenant(int tenantId) throws SQLException {
        return query(SELECT + "WHERE p.tenant_id = ? ORDER BY p.bill_month, p.paid_on", tenantId);
    }

    private DynamicArray<RentPayment> query(String sql, Object param) throws SQLException {
        DynamicArray<RentPayment> list = new DynamicArray<RentPayment>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RentPayment p = new RentPayment(
                            rs.getInt("payment_id"),
                            rs.getInt("tenant_id"),
                            rs.getString("bill_month"),
                            rs.getDouble("amount"),
                            rs.getDate("paid_on"),
                            PaymentMode.valueOf(rs.getString("mode")),
                            rs.getString("note"));
                    p.setTenantName(rs.getString("name"));
                    list.add(p);
                }
            }
        }
        return list;
    }
}
