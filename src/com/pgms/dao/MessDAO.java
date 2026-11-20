package com.pgms.dao;

import com.pgms.db.DBConnection;
import com.pgms.ds.DynamicArray;
import com.pgms.model.DayMenu;
import com.pgms.model.MessPlan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** JDBC access for mess_plans and mess_menu. */
public class MessDAO {

    public DynamicArray<MessPlan> findAllPlans() throws SQLException {
        DynamicArray<MessPlan> plans = new DynamicArray<MessPlan>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT plan_id, plan_name, meals_per_day, monthly_charge FROM mess_plans ORDER BY plan_id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                plans.add(new MessPlan(rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getDouble(4)));
            }
        }
        return plans;
    }

    public int insertPlan(MessPlan plan) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO mess_plans (plan_name, meals_per_day, monthly_charge) VALUES (?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, plan.getName());
            ps.setInt(2, plan.getMealsPerDay());
            ps.setDouble(3, plan.monthlyCharge());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    public DynamicArray<DayMenu> findMenu() throws SQLException {
        DynamicArray<DayMenu> week = new DynamicArray<DayMenu>(7);
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT day_of_week, breakfast, lunch, dinner FROM mess_menu "
                     + "ORDER BY FIELD(day_of_week,'MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY')");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                week.add(new DayMenu(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)));
            }
        }
        return week;
    }

    public void updateMenu(DayMenu menu) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "REPLACE INTO mess_menu (day_of_week, breakfast, lunch, dinner) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, menu.getDay());
            ps.setString(2, menu.getBreakfast());
            ps.setString(3, menu.getLunch());
            ps.setString(4, menu.getDinner());
            ps.executeUpdate();
        }
    }
}
