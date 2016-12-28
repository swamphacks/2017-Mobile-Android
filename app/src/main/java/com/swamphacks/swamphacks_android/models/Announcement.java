package com.swamphacks.swamphacks_android.models;

public class Announcement {
    public String name;
    public String description;
    public long time;

    public String type;
//    public boolean approved;
//    public boolean deleted;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long date) {
        this.time = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

//    public boolean isApproved() {
//        return approved;
//    }
//
//    public void setApproved(boolean approved) {
//        this.approved = approved;
//    }
//
//    public boolean isDeleted() {
//        return deleted;
//    }
//
//    public void setDeleted(boolean deleted) {
//        this.deleted = deleted;
//    }
}