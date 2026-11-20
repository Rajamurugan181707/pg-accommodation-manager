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

    private static boolean menuChecked;

    private void upgradeMenu() throws SQLException {
        if (!menuChecked) {
            dao.ensureSnacksColumn();
            menuChecked = true;
        }
    }

    public DynamicArray<DayMenu> weeklyMenu() throws SQLException {
        upgradeMenu();
        return dao.findMenu();
    }

    /** Replace a whole day's menu. Snacks may be empty (none that day). */
    public void updateMenu(String day, String breakfast, String lunch, String snacks, String dinner)
            throws SQLException {
        upgradeMenu();
        dao.updateMenu(new DayMenu(day, breakfast, lunch, snacks, dinner));
    }

    /** Change just one meal of one day; meal: 0 breakfast, 1 lunch, 2 snacks, 3 dinner. */
    public DayMenu updateMeal(final String day, int meal, String text) throws SQLException {
        DynamicArray<DayMenu> week = weeklyMenu();
        int i = week.indexOf(new DynamicArray.Matcher<DayMenu>() {
            @Override
            public boolean matches(DayMenu d) {
                return d.getDay().equals(day);
            }
        });
        if (i < 0) {
            throw new IllegalArgumentException("No menu for " + day);
        }
        DayMenu changed = week.get(i).withMeal(meal, text);
        dao.updateMenu(changed);
        return changed;
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

    public MessPlan editPlan(final int planId, String name, int mealsPerDay, double charge) throws SQLException {
        requirePlan(planId);
        final MessPlan checked = new MessPlan(planId, name, mealsPerDay, charge);   // checks every field
        if (plans().indexOf(new DynamicArray.Matcher<MessPlan>() {
            @Override
            public boolean matches(MessPlan p) {
                return p.getId() != planId && p.getName().equalsIgnoreCase(checked.getName());
            }
        }) >= 0) {
            throw new IllegalArgumentException("A plan called " + checked.getName() + " already exists");
        }
        dao.updatePlan(planId, checked.getName(), mealsPerDay, checked.monthlyCharge());
        return checked;
    }

    /** Only a plan that no tenant (current or past) has ever used can be deleted. */
    public void deletePlan(int planId) throws SQLException {
        MessPlan p = requirePlan(planId);
        int used = new TenantDAO().countOnPlan(planId);
        if (used > 0) {
            throw new IllegalArgumentException("'" + p.getName() + "' is on " + used
                    + " tenant record(s), so it is kept. Move them to another plan first.");
        }
        dao.deletePlan(planId);
    }
}
