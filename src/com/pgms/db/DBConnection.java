package com.pgms.db;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Opens JDBC connections to MySQL using the settings in db.properties
 * (looked for in the folder the app is started from).
 */
public final class DBConnection {

    private static final String CONFIG_FILE = "db.properties";
    private static final Properties CONFIG = loadConfig();

    private DBConnection() {
    }

    private static Properties loadConfig() {
        Properties p = new Properties();
        p.setProperty("db.url", "jdbc:mysql://localhost:3306/pg_accommodation"
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata");
        p.setProperty("db.user", "root");
        p.setProperty("db.password", "");
        InputStream in = null;
        try {
            in = new FileInputStream(CONFIG_FILE);
            p.load(in);
        } catch (IOException e) {
            System.out.println("[warn] " + CONFIG_FILE + " not found, using root with no password.");
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                    // nothing useful to do
                }
            }
        }
        return p;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                CONFIG.getProperty("db.url"),
                CONFIG.getProperty("db.user"),
                CONFIG.getProperty("db.password"));
    }

    /** Quick check used at start-up so the user gets a clear message. */
    public static void testConnection() throws SQLException {
        Connection c = getConnection();
        c.close();
    }
}
