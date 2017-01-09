package data.models;

import java.util.Map;

public class Event {
    public String name;
    public String description;
    public String location;
    public long startTime;
    public long endTime;
    public Map<String, Long> ratings;
    public String type;

    //Todo Do image stuff with Bitmap,
    public String map;

    public int numAttendees;
    public double avgRating;
//    public boolean  approved;


    public Map<String, Long> getRatings() {
        return ratings;
    }

    public void setRatings(Map<String, Long> ratings) {
        this.ratings = ratings;
    }

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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public long getStart() {
        return startTime * 1000;
    }

    public void setStart(long start) {
        this.startTime = start;
    }

    public long getEnd() {
        return endTime;
    }

    public void setEnd(long duration) {
        this.endTime = duration;
    }

    public String getCategory() {
        return type;
    }

    public void setCategory(String category) {
        this.type = category;
    }
}
