package hk.edu.polyu.comp.comp2021.crobs.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class BookingManagerTest {
    private UserOper userOper;
    private RoomManager roomManager;
    private BookingManager bookingManager;

    private final LocalDate date = LocalDate.of(2025, 5, 1);
    private final LocalTime t10 = LocalTime.of(10, 0);
    private final LocalTime t11 = LocalTime.of(11, 0);
    private final LocalTime t12 = LocalTime.of(12, 0);

    @BeforeEach
    public void setUp() {
        userOper = new UserOper();
        bookingManager = new BookingManager();
        roomManager = new RoomManager(userOper, bookingManager);
        bookingManager.setRoomManager(roomManager);

        userOper.login("U0");
        roomManager.addRoom("meetingRoom", 10, "tv");
        roomManager.addRoom("seminarRoom", 20, "mic");
    }

    @Test
    public void testConstructorsAndRoomExists() {
        BookingManager bmNoRoom = new BookingManager(new ArrayList<>());
        assertFalse(bmNoRoom.roomExists("1"));

        assertTrue(bookingManager.roomExists("1"));
        assertFalse(bookingManager.roomExists("999"));
    }

    @Test
    public void testBookingEntityMethods() {
        Booking b = new Booking("B001", "U0", "1", date, t10, t11, "Test");
        b.getbooking();
        assertEquals("B001", b.getBookingId());
        assertEquals("U0", b.getUserId());
        assertEquals("1", b.getRoomId());
        assertEquals(date, b.getDate());
        assertEquals(t10, b.getStartTime());
        assertEquals(t11, b.getEndTime());
        assertEquals("Test", b.getPurpose());

        assertTrue(b.hasConflict("1", date, t10, t11));
        assertFalse(b.hasConflict("2", date, t10, t11));
        assertFalse(b.hasConflict("1", date.plusDays(1), t10, t11));
        assertFalse(b.hasConflict("1", date, t11, t12));
    }

    @Test
    public void testCreateBookingAllBranches() {
        assertTrue(bookingManager.createBooking(null, "1", date, t10, t11, "Meeting").startsWith("Error: You must be logged in"));
        assertTrue(bookingManager.createBooking("  ", "1", date, t10, t11, "Meeting").startsWith("Error: You must be logged in"));
        assertTrue(bookingManager.createBooking("U0", "1", null, t10, t11, "Meeting").startsWith("Error: Start time"));
        assertTrue(bookingManager.createBooking("U0", "1", date, t11, t10, "Meeting").startsWith("Error: Start time"));
        assertTrue(bookingManager.createBooking("U0", "1", date, t10, t10, "Meeting").startsWith("Error: Start time"));
        assertTrue(bookingManager.createBooking("U0", "1", date, t10, t11, "  ").contains("purpose is empty"));
        assertTrue(bookingManager.createBooking("U0", "999", date, t10, t11, "Meeting").contains("does not exist"));

        String ok = bookingManager.createBooking("U0", "1", date, t10, t11, "Meeting");
        assertTrue(ok.contains("ID: B001"));

        String conflict = bookingManager.createBooking("U0", "1", date, t10, t11, "Overlap");
        assertTrue(conflict.contains("conflict"));
    }

    @Test
    public void testModifyBookingAllBranches() {
        bookingManager.createBooking("U0", "1", date, t10, t11, "Meeting");
        bookingManager.createBooking("U0", "1", date, t11, t12, "Meeting 2");

        assertTrue(bookingManager.modifyBooking("", "B001", "1", date, t10, t11, "New").startsWith("Error: You must be logged in"));
        assertTrue(bookingManager.modifyBooking("U0", "", "1", date, t10, t11, "New").contains("Booking ID is empty"));
        assertTrue(bookingManager.modifyBooking("U0", "B001", "1", date, t11, t10, "New").startsWith("Error: Start time"));
        assertTrue(bookingManager.modifyBooking("U0", "B001", "1", date, t10, t11, "").contains("purpose is empty"));
        assertTrue(bookingManager.modifyBooking("U0", "B001", "999", date, t10, t11, "New").contains("does not exist"));
        assertTrue(bookingManager.modifyBooking("U0", "B999", "1", date, t10, t11, "New").contains("does not exist"));
        assertTrue(bookingManager.modifyBooking("U1", "B001", "1", date, t10, t11, "New").contains("only modify your own"));
        assertTrue(bookingManager.modifyBooking("U0", "B001", "1", date, t11, t12, "New").contains("Time conflict"));

        String ok = bookingManager.modifyBooking("U0", "B001", "2", date.plusDays(1), t10, t11, "Updated");
        assertTrue(ok.startsWith("Success:"));
    }

    @Test
    public void testDeleteAndCancelBookings() {
        bookingManager.createBooking("U0", "1", date, t10, t11, "Meeting");

        assertTrue(bookingManager.deleteBooking("", "B001").startsWith("Error: You must be logged in"));
        assertTrue(bookingManager.deleteBooking("U0", "").contains("cannot be empty"));
        assertTrue(bookingManager.deleteBooking("U0", "B999").contains("does not exist"));
        assertTrue(bookingManager.deleteBooking("U1", "B001").contains("only delete your own"));

        assertTrue(bookingManager.deleteBooking("U0", "B001").startsWith("Success:"));

        bookingManager.createBooking("U1", "1", date, t10, t11, "User1");
        bookingManager.cancelBookingsByUser(null);
        bookingManager.cancelBookingsByUser("U1");
        assertTrue(bookingManager.getAllBookings().isEmpty());
    }

    @Test
    public void testListAndWeeklySchedule() {
        assertTrue(bookingManager.listBookings("").isEmpty());
        assertTrue(bookingManager.getListBookingsString("").startsWith("Error:"));
        assertTrue(bookingManager.getListBookingsString("U0").startsWith("No bookings found"));

        assertTrue(bookingManager.getWeeklySchedule(null, date).isEmpty());
        assertTrue(bookingManager.getWeeklyScheduleString("", false, "1", date).startsWith("Error:"));
        assertTrue(bookingManager.getWeeklyScheduleString("U0", false, "999", date).contains("does not exist"));
        assertTrue(bookingManager.getWeeklyScheduleString("U0", false, "1", null).contains("null"));
        assertTrue(bookingManager.getWeeklyScheduleString("U0", false, "1", date).startsWith("No bookings found"));

        bookingManager.createBooking("U0", "1", date, t10, t11, "M1");
        bookingManager.createBooking("U1", "1", date.plusDays(1), t10, t11, "M2");

        assertTrue(bookingManager.getListBookingsString("U0").contains("B001"));
        assertTrue(bookingManager.getWeeklyScheduleString("U0", true, "1", date).contains("U1"));
        assertTrue(bookingManager.getWeeklyScheduleString("U0", false, "1", date).contains("N/A"));
    }
}
