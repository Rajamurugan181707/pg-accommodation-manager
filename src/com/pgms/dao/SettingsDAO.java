package com.pgms.dao;

import com.pgms.db.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/** Key-value settings (PG name, owner name, ...) stored in the settings table. */
public class SettingsDAO {

    /** Creates the table on databases made before settings existed. Safe to call every start. */
    public void ensureTable() throws SQLException {
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS settings ("
                    + "setting_key VARCHAR(40) PRIMARY KEY, setting_value VARCHAR(80) NOT NULL)");
        }
    }

    public Map<String, String> all() throws SQLException {
        Map<String, String> map = new HashMap<String, String>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT setting_key, setting_value FROM settings");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString(1), rs.getString(2));
            }
        }
        return map;
    }

    public void put(String key, String value) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "REPLACE INTO settings (setting_key, setting_value) VALUES (?, ?)")) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        }
    }
}
