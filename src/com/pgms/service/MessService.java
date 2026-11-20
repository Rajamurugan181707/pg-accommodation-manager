package com.pgms.service;

import com.pgms.dao.MessDAO;
import com.pgms.dao.TenantDAO;
import com.pgms.ds.DynamicArray;
import com.pgms.model.DayMenu;
import com.pgms.model.MessPlan;
import com.pgms.model.Tenant;
import com.pgms.model.TenantStatus;

import java.sql.SQLException;

/** MESS module rules. */
public class MessService {

    private final MessDAO dao = new MessDAO();

    public DynamicArray<MessPlan> plans() throws SQLException {
        return dao.findAllPlans();
    }

    public MessPlan findPlan(final int planId) throws SQLException {
        DynamicArray<MessPlan> all = plans();
        int i = all.indexOf(new DynamicArray.Matcher<MessPlan>() {
            @Override
            public boolean matches(MessPlan p) {
                return p.getId() == planId;
            }
        });
        return i < 0 ? null : all.get(i);
    }

    public MessPlan requirePlan(int planId) throws SQLException {
        MessPlan p = findPlan(planId);
        if (p == null) {
            throw new IllegalArgumentException("No mess plan with ID " + planId);
        }
        return p;
    }

    public MessPlan addPlan(String name, int mealsPerDay, double monthlyCharge) throws SQLException {
        final String clean = name.trim();
        if (clean.isEmpty()) {
            throw new IllegalArgumentException("Plan name cannot be empty");
        }
        if (plans().indexOf(new DynamicArray.Matcher<MessPlan>() {
            @Override
            public boolean matches(MessPlan p) {
                return p.getName().equalsIgnoreCase(clean);
            }
        }) >= 0) {
            throw new IllegalArgumentException("A plan called " + clean + " already exists");
        }
        MessPlan plan = new MessPlan(0, clean, mealsPerDay, monthlyCharge);
        int id = dao.insertPlan(plan);
        return new MessPlan(id, clean, mealsPerDay, monthlyCharge);
    }

    public DynamicArray<DayMenu> weeklyMenu() throws SQLException {
        return dao.findMenu();
    }

    public void updateMenu(String day, String breakfast, String lunch, String dinner) throws SQLException {
        dao.updateMenu(new DayMenu(day, breakfast.trim(), lunch.trim(), dinner.trim()));
    }

    /**
     * Headcount per plan for the kitchen. Uses a plain int[] whose
     * positions line up with the plans array: counts[i] belongs to plans.get(i).
     */
    public int[] headcount(DynamicArray<MessPlan> plans) throws SQLException {
        int[] counts = new int[plans.size()];
        for (Tenant t : new TenantDAO().findByStatus(TenantStatus.ACTIVE)) {
            if (t.getMessPlanId() == null) {
                continue;
            }
            for (int i = 0; i < plans.size(); i++) {
                if (plans.get(i).getId() == t.getMessPlanId()) {
                    counts[i]++;
                }
            }
        }
        return counts;
    }
}
