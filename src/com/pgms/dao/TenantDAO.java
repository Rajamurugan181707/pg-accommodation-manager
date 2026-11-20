package com.pgms.dao;

import com.pgms.db.DBConnection;
import com.pgms.ds.DynamicArray;
import com.pgms.model.Gender;
import com.pgms.model.Tenant;
import com.pgms.model.TenantStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

/**
 * JDBC access for the tenants table.
 * Moving a tenant in, out or between rooms changes two tables, so those
 * methods run inside a transaction: either both changes happen or neither.
 */
public class TenantDAO {

    private static final String SELECT =
            "SELECT t.*, r.room_no, m.plan_name FROM tenants t "
            + "JOIN rooms r ON r.room_id = t.room_id "
            + "LEFT JOIN mess_plans m ON m.plan_id = t.mess_plan_id ";

    public DynamicArray<Tenant> findByStatus(TenantStatus status) throws SQLException {
        return query(SELECT + "WHERE t.status = ? ORDER BY t.tenant_id", status.name());
    }

    public DynamicArray<Tenant> findAll() throws SQLException {
        return query(SELECT + "ORDER BY t.tenant_id");
    }

    public Tenant findById(int tenantId) throws SQLException {
        DynamicArray<Tenant> found = query(SELECT + "WHERE t.tenant_id = ?", tenantId);
        return found.isEmpty() ? null : found.get(0);
    }

    public boolean phoneExists(String phone) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT 1 FROM tenants WHERE phone = ? AND status = 'ACTIVE'")) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Register a tenant and take one bed in their room, as one transaction. */
    public int register(Tenant t) throws SQLException {
        String sql = "INSERT INTO tenants (name, phone, email, gender, occupation, room_id, "
                + "mess_plan_id, join_date, deposit) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection c = DBConnection.getConnection();
        try {
            c.setAutoCommit(false);
            if (!RoomDAO.changeOccupancy(c, t.getRoomId(), +1)) {
                throw new SQLException("Room is already full");
            }
            int id;
            try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, t.getName());
                ps.setString(2, t.getPhone());
                ps.setString(3, t.getEmail());
                ps.setString(4, t.getGender().name());
                ps.setString(5, t.getOccupation());
                ps.setInt(6, t.getRoomId());
                setPlan(ps, 7, t.getMessPlanId());
                ps.setDate(8, new java.sql.Date(t.getJoinDate().getTime()));
                ps.setDouble(9, t.getDeposit());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    id = keys.getInt(1);
                }
            }
            c.commit();
            t.setId(id);
            return id;
        } catch (SQLException e) {
            c.rollback();
            throw e;
        } finally {
            c.close();
        }
    }

    /** Move an active tenant to another room: free the old bed, take the new one. */
    public void shiftRoom(int tenantId, int fromRoomId, int toRoomId) throws SQLException {
        Connection c = DBConnection.getConnection();
        try {
            c.setAutoCommit(false);
            if (!RoomDAO.changeOccupancy(c, toRoomId, +1)) {
                throw new SQLException("The new room is already full");
            }
            RoomDAO.changeOccupancy(c, fromRoomId, -1);
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE tenants SET room_id = ? WHERE tenant_id = ? AND status = 'ACTIVE'")) {
                ps.setInt(1, toRoomId);
                ps.setInt(2, tenantId);
                if (ps.executeUpdate() != 1) {
                    throw new SQLException("Tenant not found or not active");
                }
            }
            c.commit();
        } catch (SQLException e) {
            c.rollback();
            throw e;
        } finally {
            c.close();
        }
    }

    /** Mark the tenant as left and free their bed. Their records are kept. */
    public void checkout(int tenantId, int roomId, java.sql.Date leaveDate) throws SQLException {
        Connection c = DBConnection.getConnection();
        try {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE tenants SET status = 'LEFT', leave_date = ? "
                    + "WHERE tenant_id = ? AND status = 'ACTIVE'")) {
                ps.setDate(1, leaveDate);
                ps.setInt(2, tenantId);
                if (ps.executeUpdate() != 1) {
                    throw new SQLException("Tenant not found or already checked out");
                }
            }
            RoomDAO.changeOccupancy(c, roomId, -1);
            c.commit();
        } catch (SQLException e) {
            c.rollback();
            throw e;
        } finally {
            c.close();
        }
    }

    public void updateMessPlan(int tenantId, Integer planId) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE tenants SET mess_plan_id = ? WHERE tenant_id = ? AND status = 'ACTIVE'")) {
            setPlan(ps, 1, planId);
            ps.setInt(2, tenantId);
            ps.executeUpdate();
        }
    }

    private static void setPlan(PreparedStatement ps, int index, Integer planId) throws SQLException {
        if (planId == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, planId);
        }
    }

    private DynamicArray<Tenant> query(String sql, Object... params) throws SQLException {
        DynamicArray<Tenant> list = new DynamicArray<Tenant>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    private static Tenant map(ResultSet rs) throws SQLException {
        int planId = rs.getInt("mess_plan_id");
        Integer plan = rs.wasNull() ? null : planId;
        Tenant t = new Tenant(
                rs.getInt("tenant_id"),
                rs.getString("name"),
                rs.getString("phone"),
                rs.getString("email"),
                Gender.valueOf(rs.getString("gender")),
                rs.getString("occupation"),
                rs.getInt("room_id"),
                plan,
                rs.getDate("join_date"),
                rs.getDouble("deposit"));
        t.setLeaveDate(rs.getDate("leave_date"));
        t.setStatus(TenantStatus.valueOf(rs.getString("status")));
        t.setRoomNo(rs.getString("room_no"));
        t.setMessPlanName(rs.getString("plan_name"));
        return t;
    }
}
