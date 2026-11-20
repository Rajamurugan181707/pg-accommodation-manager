package com.pgms.dao;

import com.pgms.db.DBConnection;
import com.pgms.ds.DynamicArray;
import com.pgms.model.Room;
import com.pgms.model.RoomType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** JDBC access for the rooms table. */
public class RoomDAO {

    private static final String COLUMNS =
            "room_id, room_no, floor_no, room_type, occupied, rent_per_bed, has_ac";

    public DynamicArray<Room> findAll() throws SQLException {
        DynamicArray<Room> rooms = new DynamicArray<Room>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT " + COLUMNS + " FROM rooms ORDER BY room_id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rooms.add(map(rs));
            }
        }
        return rooms;
    }

    public Room findById(int roomId) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT " + COLUMNS + " FROM rooms WHERE room_id = ?")) {
            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public int insert(Room room) throws SQLException {
        String sql = "INSERT INTO rooms (room_no, floor_no, room_type, capacity, occupied, rent_per_bed, has_ac) "
                + "VALUES (?, ?, ?, ?, 0, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, room.getRoomNo());
            ps.setInt(2, room.getFloor());
            ps.setString(3, room.getType().name());
            ps.setInt(4, room.getCapacity());
            ps.setDouble(5, room.getRentPerBed());
            ps.setBoolean(6, room.hasAc());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                int id = keys.getInt(1);
                room.setId(id);
                return id;
            }
        }
    }

    public void updateRent(int roomId, double rentPerBed) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE rooms SET rent_per_bed = ? WHERE room_id = ?")) {
            ps.setDouble(1, rentPerBed);
            ps.setInt(2, roomId);
            ps.executeUpdate();
        }
    }

    /** Number of tenants (current or past) who have ever stayed in the room. */
    public int tenantHistoryCount(int roomId) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM tenants WHERE room_id = ?")) {
            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void delete(int roomId) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM rooms WHERE room_id = ? AND occupied = 0")) {
            ps.setInt(1, roomId);
            ps.executeUpdate();
        }
    }

    /**
     * Add or remove one bed's occupancy, inside the caller's transaction.
     * The WHERE clause refuses to go below 0 or above capacity, so two
     * people can never be given the last bed at the same time.
     * Returns false if the room is full (or already empty).
     */
    static boolean changeOccupancy(Connection c, int roomId, int delta) throws SQLException {
        String sql = "UPDATE rooms SET occupied = occupied + ? "
                + "WHERE room_id = ? AND occupied + ? BETWEEN 0 AND capacity";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, roomId);
            ps.setInt(3, delta);
            return ps.executeUpdate() == 1;
        }
    }

    private static Room map(ResultSet rs) throws SQLException {
        return new Room(
                rs.getInt("room_id"),
                rs.getString("room_no"),
                rs.getInt("floor_no"),
                RoomType.valueOf(rs.getString("room_type")),
                rs.getInt("occupied"),
                rs.getDouble("rent_per_bed"),
                rs.getBoolean("has_ac"));
    }

    /**
     * Change every detail of a room. The type can only shrink the room if the
     * people already in it still fit (occupied <= new capacity), checked in SQL.
     * Returns false if they would not fit.
     */
    public boolean update(int roomId, String roomNo, int floor, com.pgms.model.RoomType type,
                          double rentPerBed, boolean ac) throws SQLException {
        String sql = "UPDATE rooms SET room_no = ?, floor_no = ?, room_type = ?, capacity = ?, "
                + "rent_per_bed = ?, has_ac = ? WHERE room_id = ? AND occupied <= ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, roomNo);
            ps.setInt(2, floor);
            ps.setString(3, type.name());
            ps.setInt(4, type.beds());
            ps.setDouble(5, rentPerBed);
            ps.setBoolean(6, ac);
            ps.setInt(7, roomId);
            ps.setInt(8, type.beds());
            return ps.executeUpdate() == 1;
        }
    }
}
