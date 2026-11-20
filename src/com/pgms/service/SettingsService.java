package com.pgms.service;

import com.pgms.dao.SettingsDAO;
import com.pgms.util.Check;

import java.sql.SQLException;
import java.util.Map;

/** Names shown in the app (PG name, tagline, owner name, footer line), all editable. */
public class SettingsService {

    public static final String PG_NAME = "pg_name";
    public static final String PG_TAGLINE = "pg_tagline";
    public static final String OWNER_NAME = "owner_name";
    public static final String OWNER_LINE = "owner_line";

    private final SettingsDAO dao = new SettingsDAO();

    public String get(String key) throws SQLException {
        dao.ensureTable();
        Map<String, String> all = dao.all();
        String v = all.get(key);
        return v != null ? v : defaultFor(key);
    }

    public void save(String pgName, String tagline, String ownerName, String ownerLine) throws SQLException {
        String a = Check.text(pgName, 30, "PG name", true);
        String b = Check.text(tagline, 40, "Tagline", false);
        String c = Check.text(ownerName, 40, "Owner / manager name", false);
        String d = Check.text(ownerLine, 40, "Footer line", false);
        dao.ensureTable();
        dao.put(PG_NAME, a);
        dao.put(PG_TAGLINE, b == null ? "" : b);
        dao.put(OWNER_NAME, c == null ? "" : c);
        dao.put(OWNER_LINE, d == null ? "" : d);
    }

    static String defaultFor(String key) {
        if (PG_NAME.equals(key)) {
            return "PG Manager";
        }
        if (PG_TAGLINE.equals(key)) {
            return "ACCOMMODATION";
        }
        return "";
    }
}
