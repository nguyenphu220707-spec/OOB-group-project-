package hk.edu.polyu.comp.comp2021.crobs.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

public class RoomManagerTest {
    private UserOper userOper;
    private BookingManager bookingManager;
    private RoomManager roomManager;

    @BeforeEach
    public void setUp() {
        userOper = new UserOper();
        bookingManager = new BookingManager();
        roomManager = new RoomManager(userOper, bookingManager);
        bookingManager.setRoomManager(roomManager);
    }

    @Test
    public void testConstructorNullManagers() {
        assertThrows(IllegalArgumentException.class, () -> new RoomManager(null, bookingManager));
        assertThrows(IllegalArgumentException.class, () -> new RoomManager(userOper, null));
    }

    @Test
    public void testAddRoomAllBranches() {
        assertEquals("Error: Admin permission required.", roomManager.addRoom("lectureTheatre", 50, "projector"));

        userOper.login("U0");
        assertEquals("Error: Invalid room type.", roomManager.addRoom("invalidType", 50, "projector"));
        assertEquals("Error: Capacity must be positive.", roomManager.addRoom("computerLab", 0, "projector"));
        assertEquals("Error: Invalid features format.", roomManager.addRoom("seminarRoom", 30, ""));
        assertEquals("Error: Invalid features format.", roomManager.addRoom("seminarRoom", 30, null));
        assertEquals("Error: Invalid features format.", roomManager.addRoom("seminarRoom", 30, ",projector"));
        assertEquals("Error: Invalid features format.", roomManager.addRoom("seminarRoom", 30, "projector,"));
        assertEquals("Error: Invalid features format.", roomManager.addRoom("seminarRoom", 30, "a,,b"));
        assertEquals("Error: Invalid features format.", roomManager.addRoom("seminarRoom", 30, "white board"));

        assertEquals("Room added successfully! ID: 1", roomManager.addRoom("lectureTheatre", 100, "projector,mic"));
        assertEquals("Room added successfully! ID: 2", roomManager.addRoom("computerLab", 40, "pc"));
        assertEquals("Room added successfully! ID: 3", roomManager.addRoom("seminarRoom", 25, "whiteboard"));
        assertEquals("Room added successfully! ID: 4", roomManager.addRoom("meetingRoom", 10, "tv"));

        assertTrue(roomManager.roomExists("1"));
        assertFalse(roomManager.roomExists("999"));
        assertFalse(roomManager.roomExists(null));
    }

    @Test
    public void testModifyRoomAllBranches() {
        assertEquals("Error: Admin permission required.", roomManager.modifyRoom("1", "meetingRoom", 20, "tv"));

        userOper.login("U0");
        roomManager.addRoom("meetingRoom", 10, "tv");

        assertEquals("Error: Room not found.", roomManager.modifyRoom("999", "meetingRoom", 20, "tv"));
        assertEquals("Error: Invalid room type.", roomManager.modifyRoom("1", "badType", 20, "tv"));
        assertEquals("Error: Capacity must be positive.", roomManager.modifyRoom("1", "meetingRoom", -5, "tv"));
        assertEquals("Error: Invalid features format.", roomManager.modifyRoom("1", "meetingRoom", 20, ""));

        assertEquals("Room modified successfully!", roomManager.modifyRoom("1", "seminarRoom", 25, "projector,mic"));
    }

    @Test
    public void testDeleteRoomAllBranches() {
        assertEquals("Error: Admin permission required.", roomManager.deleteRoom("1"));

        userOper.login("U0");
        roomManager.addRoom("meetingRoom", 10, "tv");
        roomManager.addRoom("seminarRoom", 20, "mic");

        assertEquals("Error: Room not found.", roomManager.deleteRoom("999"));

        bookingManager.createBooking("U0", "1", LocalDate.of(2025, 5, 1), LocalTime.of(10, 0), LocalTime.of(11, 0), "Meeting");
        assertEquals("Error: Cannot delete a room with existing bookings.", roomManager.deleteRoom("1"));

        assertEquals("Room deleted successfully!", roomManager.deleteRoom("2"));
        assertFalse(roomManager.roomExists("2"));
    }

    @Test
    public void testListRoomAndListAllRooms() {
        assertEquals("No rooms available", roomManager.listAllRooms());

        userOper.login("U0");
        roomManager.addRoom("meetingRoom", 10, "tv");

        assertEquals("Error: Room not found.", roomManager.listRoom("999"));
        assertTrue(roomManager.listRoom("1").contains("Room ID: 1"));
        assertTrue(roomManager.listAllRooms().contains("Room ID: 1"));
    }

    @Test
    public void testSearchAvailableRooms() {
        userOper.login("U0");
        roomManager.addRoom("meetingRoom", 10, "tv");
        roomManager.addRoom("lectureTheatre", 100, "projector,mic");

        LocalDate date = LocalDate.of(2025, 5, 1);
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(11, 0);

        assertEquals("Error: Invalid search date, time or capacity.", roomManager.searchAvailableRooms(null, start, end, 5, ""));
        assertEquals("Error: Invalid search date, time or capacity.", roomManager.searchAvailableRooms(date, null, end, 5, ""));
        assertEquals("Error: Invalid search date, time or capacity.", roomManager.searchAvailableRooms(date, start, null, 5, ""));
        assertEquals("Error: Invalid search date, time or capacity.", roomManager.searchAvailableRooms(date, end, start, 5, ""));
        assertEquals("Error: Invalid search date, time or capacity.", roomManager.searchAvailableRooms(date, start, end, 0, ""));
        assertEquals("Error: Invalid features format.", roomManager.searchAvailableRooms(date, start, end, 5, ",bad"));

        bookingManager.createBooking("U0", "2", date, start, end, "Lecture");

        assertEquals("No available rooms found.", roomManager.searchAvailableRooms(date, start, end, 50, "projector"));

        String res1 = roomManager.searchAvailableRooms(date, start, end, 5, "tv");
        assertTrue(res1.contains("Room ID: 1"));

        assertEquals("No available rooms found.", roomManager.searchAvailableRooms(date, start, end, 200, null));
    }
}
