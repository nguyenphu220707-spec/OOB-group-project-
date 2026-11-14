
package hk.edu.polyu.comp.comp2021.crobs.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RoomManager {

    // Attributes
    private final List<Room> rooms;
    private int nextRoomId;
    private final UserOper users;
    private final BookingManager bookings;

    // Constructor
    public RoomManager(UserOper users, BookingManager bookings) {

        if (users == null || bookings == null) {
            throw new IllegalArgumentException("Managers cannot be null");
        }

        this.users = users;
        this.bookings = bookings;
        this.rooms = new ArrayList<>();
        this.nextRoomId = 1;
    }


    private boolean isAdmin() {
        return users.hasPermission(2);

    }

    private boolean validRoomType(String type) {

        return "lectureTheatre". equals(type)
                ||  " computerLab". equals(type)
                || "seminarRoom". equals(type)
                || "meetingRoom". equals(type);

    }

    private boolean validFeatures(String features) {

            if (features == null || features.isEmpty()) {
                return false;
            }
            if (features.startsWith(",")  || features.endsWith(",")  || features.contains(",,")) {
                return false;
            }

            for (int i = 0; i < features.length(); i++) {
                if (Character.isWhitespace(features.charAt(i))) {
                    return false;
                }
            }

            return true;

    }

    // Convert features String into Set
    private Set<String> parseFeatures(String features) {

        Set<String> result = new HashSet<>();
        String[] parts = features.split(",");

        for (int i =0 ; i<parts.length; i++) {
            result.add(parts[i]);

        }

        return result;
    }


    public Room findRoom(String roomId) {

        for (int i= 0 ; i<rooms. size(); i++) {
            Room. room= rooms.get(i);
            if (room.get(i).equals(roomId)) {
                return room;
            }
        }
        return null;
    }

    // Check whether a room exists
    public boolean roomExists(String roomId) {
        if (roomId==null) {
            return false;
        }
        return findRoom(roomId)!=null;
    }

    // REQ7: Add a new room
    public String addRoom(String roomType, int capacity, String features) {

        if (!isAdmin()) {
            return "Error: Admin permission required.";
        }

        if (!validRoomType(roomType)) {
            return "Error: Invalid room type.";
        }

        if (capacity <= 0) {
            return "Error: Capacity must be positive.";
        }

        if (!validFeatures(features)) {
            return "Error: Invalid features format.";
        }
        String roomId= String. valueOf(nextRoomId);
        Room room= new Room(roomId, roomType, capacity, parseFeatures(features));

        rooms.add(room);
        nextRoomId++;


        return "Room added successfully! ID: " + roomId;

    }

    // REQ8: Modify room information
    public String modifyRoom(String roomId, String roomType, int capacity, String features) {

        if (!isAdmin()) {
            return "Error: Admin permission required.";
        }
        Room room = findRoom(roomId);

        if (room == null) {
            return "Error: Room not found.";
        }
        if (!validRoomType(roomType)) {
            return "Error: Invalid room type.";
        }
        if (capacity <= 0) {
            return "Error: Capacity must be positive.";
        }

        if (!validFeatures(features)) {
            return "Error: Invalid features format.";
        }
        room.setRoomType(roomType);
        room.setCapacity(capacity);
        room.setFeatures(parseFeatures(features));

        return "Room modified successfully!";
    }
    // REQ9: Delete a room
    public String deleteRoom(String roomId) {

        if (!isAdmin()) {
            return "Error: Admin permission required.";
        }
        Room room = findRoom(roomId);
        if (room== null) {
            return "Error: Invalid room type.";

        }
        List<Booking> allBookings = bookings.getAllBookings();

        for (int i = 0; i < allBookings.size(); i++) {

            Booking booking = allBookings.get(i);

            if (roomId.equals(booking.getRoomId())) {
                return "Error: Cannot delete a room with existing bookings.";
            }
        }

        rooms.remove(room);
        return "Room deleted successfully!";
    }
    // REQ15: Display one room
    public String listRoom(String roomId) {

            Room room = findRoom(roomId);

            if (room == null) {
                return "Error: Room not found.";
            }
            return formatRoom(room);
        }

        // REQ16: Display all rooms
    public String listAllRooms() {
        if (rooms.isEmpty()) {
            return "No rooms available";
        }
        String result="";

        for (int i = 0; i < rooms.size(); i++) {
            result += formatRoom(rooms.get(i)) + "\n";
        }
        return result.trim();
    }

    // Format room information
    private String formatRoom(Room room) {

        return "Room ID: " + room.getRoomId()
                + ", Type: " + room.getRoomType()
                + ", Capacity: " + room.getCapacity()
                + ", Features: " + room.getFeatures();
    }

    // REQ13: Search available rooms
    public String searchAvailableRooms(
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            int minCapacity,
            String requiredFeatures) {

        if (date == null || startTime == null
                || endTime == null
                || !startTime.isBefore(endTime)
                || minCapacity <= 0) {

            return "Error: Invalid search date, time or capacity.";
        }

        Set<String> required = new HashSet<>();

        if (requiredFeatures != null
                && !requiredFeatures.isEmpty()) {

            if (!validFeatures(requiredFeatures)) {
                return "Error: Invalid features format.";
            }
            required = parseFeatures(requiredFeatures);
        }
        String result = "";
        for (int i = 0; i < rooms.size(); i++) {

            Room room = rooms.get(i);
            if (room.getCapacity() < minCapacity || !room.getFeatures().containsAll(required)) {
                continue;
            }

            String roomId = room.getRoomId();
            List<Booking> schedule = bookings.getWeeklySchedule(roomId, date);

            boolean available = true;

            for (int j = 0; j < schedule.size(); j++) {

                Booking booking = schedule.get(j);

                if (booking.hasConflict(roomId, date, startTime, endTime)) {

                    available = false;
                    break;
                }
            }
            if (available) {
                result += formatRoom(room) + "\n";
            }
        }
        if (result.isEmpty()) {
            return "No available rooms found.";
        }
        return result.trim();
    }
}
