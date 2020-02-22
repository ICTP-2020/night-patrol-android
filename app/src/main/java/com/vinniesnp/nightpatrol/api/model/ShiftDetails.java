package com.vinniesnp.nightpatrol.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ShiftDetails {
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public float getStartTime() {
        return startTime;
    }

    public void setStartTime(float startTime) {
        this.startTime = startTime;
    }

    public ShiftLeader getShiftLeader() {
        return shiftLeader;
    }

    public void setShiftLeader(ShiftLeader shiftLeader) {
        this.shiftLeader = shiftLeader;
    }

    public ShiftDriver getShiftDriver() {
        return shiftDriver;
    }

    public void setShiftDriver(ShiftDriver shiftDriver) {
        this.shiftDriver = shiftDriver;
    }

    public List<ShiftUsers> getShiftUsers() {
        return shiftUsers;
    }

    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("startTime")
    @Expose
    private float startTime;

    @SerializedName("shiftLeader")
    @Expose
    ShiftLeader shiftLeader;

    @SerializedName("shiftDriver")
    @Expose
    ShiftDriver shiftDriver;

    @SerializedName("assignedUsers")
    @Expose
    List<ShiftUsers> shiftUsers;
}
