package com.pgms.service;

import com.pgms.dao.TenantDAO;
import com.pgms.ds.DynamicArray;
import com.pgms.model.Gender;
import com.pgms.model.MessPlan;
import com.pgms.model.Room;
import com.pgms.model.Tenant;
import com.pgms.model.TenantStatus;
import com.pgms.util.Check;

import java.sql.SQLException;
import java.util.Date;

/** TENANT module rules. */
public class TenantService {

    private final TenantDAO dao = new TenantDAO();
    private final RoomService rooms = new RoomService();
    private final MessService mess = new MessService();

    public DynamicArray<Tenant> activeTenants() throws SQLException {
        return dao.findByStatus(TenantStatus.ACTIVE);
    }

    public DynamicArray<Tenant> pastTenants() throws SQLException {
        return dao.findByStatus(TenantStatus.LEFT);
    }

    public DynamicArray<Tenant> allTenants() throws SQLException {
        return dao.findAll();
    }

    public Tenant requireTenant(int tenantId) throws SQLException {
        Tenant t = dao.findById(tenantId);
        if (t == null) {
            throw new IllegalArgumentException("No tenant with ID " + tenantId);
        }
        return t;
    }

    public Tenant requireActive(int tenantId) throws SQLException {
        Tenant t = requireTenant(tenantId);
        if (!t.isActive()) {
            throw new IllegalArgumentException(t.getName() + " has already checked out");
        }
        return t;
    }

    public boolean phoneTaken(String phone) throws SQLException {
        return dao.phoneExists(phone);
    }

    /** Linear search on the array: names containing the text, ignoring case. */
    public DynamicArray<Tenant> searchByName(String text) throws SQLException {
        final String needle = text.trim().toLowerCase();
        return dao.findAll().filter(new DynamicArray.Matcher<Tenant>() {
            @Override
            public boolean matches(Tenant t) {
                return t.getName().toLowerCase().contains(needle);
            }
        });
    }

    public Tenant register(String name, String phone, String email, Gender gender, String occupation,
                           String roomNo, Integer messPlanId, Date joinDate, double deposit)
            throws SQLException {
        Room room = rooms.requireRoom(roomNo);
        if (room.isFull()) {
            throw new IllegalArgumentException("Room " + room.getRoomNo() + " is full");
        }
        if (dao.phoneExists(phone)) {
            throw new IllegalArgumentException("A current tenant already has phone " + phone);
        }
        if (messPlanId != null) {
            mess.requirePlan(messPlanId);
        }
        Check.notBeyond(joinDate, 31, "Joining date");
        Tenant t = new Tenant(0, name, phone, email, gender, occupation,
                room.getId(), messPlanId, joinDate, deposit);
        dao.register(t);
        t.setRoomNo(room.getRoomNo());
        return t;
    }

    public void shiftRoom(int tenantId, String newRoomNo) throws SQLException {
        Tenant t = requireActive(tenantId);
        Room target = rooms.requireRoom(newRoomNo);
        if (target.getId() == t.getRoomId()) {
            throw new IllegalArgumentException(t.getName() + " is already in room " + target.getRoomNo());
        }
        if (target.isFull()) {
            throw new IllegalArgumentException("Room " + target.getRoomNo() + " is full");
        }
        dao.shiftRoom(tenantId, t.getRoomId(), target.getId());
    }

    public void checkout(int tenantId, java.sql.Date leaveDate) throws SQLException {
        Tenant t = requireActive(tenantId);
        if (leaveDate.before(t.getJoinDate())) {
            throw new IllegalArgumentException("Leaving date is before the joining date");
        }
        // checking out frees the bed straight away, so it must not be a date still to come
        Check.notFuture(leaveDate, "Leaving date");
        dao.checkout(tenantId, t.getRoomId(), leaveDate);
    }

    public void changeMessPlan(int tenantId, Integer planId) throws SQLException {
        requireActive(tenantId);
        if (planId != null) {
            MessPlan plan = mess.requirePlan(planId);
            planId = plan.getId();
        }
        dao.updateMessPlan(tenantId, planId);
    }

    /**
     * Edit a tenant's personal details. The phone must not clash with another
     * current tenant; the joining date follows the same rules as registering.
     * Room, mess plan and check-out have their own actions.
     */
    public Tenant editDetails(int tenantId, String name, String phone, String email, Gender gender,
                              String occupation, double deposit, Date joinDate) throws SQLException {
        Tenant old = requireTenant(tenantId);
        Tenant t = new Tenant(tenantId, name, phone, email, gender, occupation,
                old.getRoomId(), old.getMessPlanId(), joinDate, deposit);   // constructor checks every field
        if (old.isActive() && dao.phoneUsedByOther(t.getPhone(), tenantId)) {
            throw new IllegalArgumentException("Another current tenant already has phone " + t.getPhone());
        }
        Check.notBeyond(joinDate, 31, "Joining date");
        if (old.getLeaveDate() != null && joinDate.after(old.getLeaveDate())) {
            throw new IllegalArgumentException("Joining date cannot be after the leaving date ("
                    + com.pgms.util.Dates.format(old.getLeaveDate()) + ")");
        }
        dao.updateDetails(t);
        return requireTenant(tenantId);
    }

    public int tenantsOnPlan(int planId) throws SQLException {
        return dao.countOnPlan(planId);
    }
}
