package com.pgms.model;

import com.pgms.util.Check;

import java.util.Date;

/**
 * TENANT module entity.
 * OOPS: INHERITANCE - a Tenant is a Person with a room, a mess plan and a stay.
 */
public class Tenant extends Person {

    private String occupation;
    private int roomId;
    private String roomNo;          // filled from a JOIN, for display
    private Integer messPlanId;     // null = not in mess
    private String messPlanName;    // filled from a JOIN, for display
    private Date joinDate;
    private Date leaveDate;
    private double deposit;
    private TenantStatus status;

    public Tenant(int id, String name, String phone, String email, Gender gender,
                  String occupation, int roomId, Integer messPlanId,
                  Date joinDate, double deposit) {
        super(id, name, phone, email, gender);
        this.occupation = Check.text(occupation, 40, "Occupation", false);
        this.roomId = roomId;
        this.messPlanId = messPlanId;
        this.joinDate = joinDate;
        this.deposit = Check.money(deposit, true, "Deposit");
        this.status = TenantStatus.ACTIVE;
    }

    @Override
    public String role() {
        return "Tenant";
    }

    public boolean isActive() {
        return status == TenantStatus.ACTIVE;
    }

    public String getOccupation() {
        return occupation;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }

    public Integer getMessPlanId() {
        return messPlanId;
    }

    public void setMessPlanId(Integer messPlanId) {
        this.messPlanId = messPlanId;
    }

    public String getMessPlanName() {
        return messPlanName;
    }

    public void setMessPlanName(String messPlanName) {
        this.messPlanName = messPlanName;
    }

    public Date getJoinDate() {
        return joinDate;
    }

    public Date getLeaveDate() {
        return leaveDate;
    }

    public void setLeaveDate(Date leaveDate) {
        this.leaveDate = leaveDate;
    }

    public double getDeposit() {
        return deposit;
    }

    public TenantStatus getStatus() {
        return status;
    }

    public void setStatus(TenantStatus status) {
        this.status = status;
    }
}
