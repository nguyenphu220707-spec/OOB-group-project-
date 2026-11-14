
package hk.edu.polyu.comp.comp2021.crobs.model;

import java.util.Set;
import java.util.HashSet;

public class Room {

    private final String roomId;
    private String roomType;
    private int capacity;
    private Set<String> features;

    // Constructor
    public Room(String roomId, String roomType,
                int capacity, Set<String> features) {

        this.roomId = roomId;
        this.roomType = roomType;
        this.capacity = capacity;
        this.features = new HashSet<>(features);
    }

    public String getRoomId() {
        return roomId;
    }


    public String getRoomType() {
        return roomType;
    }

    public int getCapacity() {
        return capacity;
    }

    public Set<String> getFeatures() {
        return new HashSet<>(features);
    }


    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setFeatures(Set<String> features) {
        this.features = new HashSet<>(features);
    }
}