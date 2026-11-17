package hk.edu.polyu.comp.comp2021.crobs.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class CROBSTest {

    @Test
    public void testParseArgsAndLogs(@TempDir Path tempDir) throws Exception {
        Path htmlFile = tempDir.resolve("log.html");
        Path txtFile = tempDir.resolve("log.txt");

        CROBS crobs = new CROBS();
        crobs.parseArgs(null);
        crobs.parseArgs(new String[]{"-html", htmlFile.toString(), "-txt", txtFile.toString()});

        crobs.recordCommand("login U0");
        crobs.recordCommand("quit");

        String txtContent = new String(Files.readAllBytes(txtFile));
        String htmlContent = new String(Files.readAllBytes(htmlFile));

        assertTrue(txtContent.contains("login U0"));
        assertTrue(txtContent.contains("quit"));
        assertTrue(htmlContent.contains("<td>1</td><td>login U0</td>"));
        assertTrue(htmlContent.contains("<td>2</td><td>quit</td>"));
        assertEquals(2, crobs.getCommandHistory().size());
    }

    @Test
    public void testSaveLogsInvalidPath(@TempDir Path tempDir) {
        CROBS crobs = new CROBS();
        // Passing a directory instead of a file triggers the IOException catch block
        crobs.parseArgs(new String[]{"-html", tempDir.toString(), "-txt", tempDir.toString()});
        crobs.recordCommand("login U0");
    }
    @Test
    public void testRoomAndBookingCommands() {
        CROBS crobs = new CROBS();
        crobs.executeCommand("login U0");

        // Room commands + usage/error branches
        crobs.executeCommand("addRoom");
        crobs.executeCommand("addRoom meetingRoom badCap tv");
        crobs.executeCommand("addRoom meetingRoom 10 tv");
        crobs.executeCommand("addRoom seminarRoom 20 mic");

        crobs.executeCommand("modifyRoom");
        crobs.executeCommand("modifyRoom 1 meetingRoom badCap tv");
        crobs.executeCommand("modifyRoom 1 meetingRoom 15 tv");

        crobs.executeCommand("listRoom");
        crobs.executeCommand("listRoom 1");
        crobs.executeCommand("listAllRooms");

        crobs.executeCommand("searchAvailableRooms");
        crobs.executeCommand("searchAvailableRooms bad-date 10:00 11:00 5");
        crobs.executeCommand("searchAvailableRooms 2025-05-01 10:00 11:00 5");
        crobs.executeCommand("searchAvailableRooms 2025-05-01 10:00 11:00 5 tv");

        // Booking commands + usage/error branches
        crobs.executeCommand("createBooking");
        crobs.executeCommand("createBooking 1 bad-date 10:00 11:00 Meeting");
        crobs.executeCommand("createBooking 1 2025-05-01 10:00 11:00 Team Meeting");

        crobs.executeCommand("modifyBooking");
        crobs.executeCommand("modifyBooking B001 1 bad-date 10:00 11:00 New");
        crobs.executeCommand("modifyBooking B001 1 2025-05-01 11:00 12:00 Updated Meeting");

        crobs.executeCommand("listBookings");

        crobs.executeCommand("weeklySchedule");
        crobs.executeCommand("weeklySchedule 1 bad-date");
        crobs.executeCommand("weeklySchedule 1 2025-05-01");

        crobs.executeCommand("deleteBooking");
        crobs.executeCommand("deleteBooking B001");

        crobs.executeCommand("deleteRoom");
        crobs.executeCommand("deleteRoom 2");
    }
    @Test
    public void testUndoAndRedo() {
        CROBS crobs = new CROBS();

        // Empty undo / redo stacks
        crobs.executeCommand("undo");
        crobs.executeCommand("redo");

        // Log in and run state-changing commands
        crobs.executeCommand("login U0");
        crobs.executeCommand("addUser Alice");
        crobs.executeCommand("changeRole U1 admin");
        crobs.executeCommand("addRoom meetingRoom 10 tv");
        crobs.executeCommand("modifyRoom 1 seminarRoom 20 mic");
        crobs.executeCommand("createBooking 1 2025-05-01 10:00 11:00 Meeting");
        crobs.executeCommand("modifyBooking B001 1 2025-05-01 11:00 12:00 Updated");

        assertTrue(crobs.getRoomManager().roomExists("1"));
        assertEquals(1, crobs.getBookingManager().getAllBookings().size());

        // Undo modifyBooking and createBooking
        crobs.executeCommand("undo");
        crobs.executeCommand("undo");
        assertTrue(crobs.getBookingManager().getAllBookings().isEmpty());

        // Redo createBooking
        crobs.executeCommand("redo");
        assertEquals(1, crobs.getBookingManager().getAllBookings().size());

        // New undoable command clears redoStack
        crobs.executeCommand("deleteBooking B001");
        crobs.executeCommand("redo");
        assertTrue(crobs.getBookingManager().getAllBookings().isEmpty());

        // Cover remaining undoable commands
        crobs.executeCommand("deleteRoom 1");
        assertFalse(crobs.getRoomManager().roomExists("1"));
        crobs.executeCommand("undo");
        assertTrue(crobs.getRoomManager().roomExists("1"));

        crobs.executeCommand("removeUser U1");
        crobs.executeCommand("undo");
        assertEquals("U0", crobs.getUserOper().getCurrentUserId());
    }

    @Test
    public void testStartAndUserCommands(@TempDir Path tempDir) {
        Path htmlFile = tempDir.resolve("run.html");
        Path txtFile = tempDir.resolve("run.txt");

        String input = String.join("\n",
                "",
                "login",
                "login U0",
                "addUser",
                "addUser Cao",
                "changeRole",
                "changeRole U1 badrole",
                "changeRole U1 admin",
                "removeUser",
                "removeUser U1",
                "logout",
                "unknownCmd",
                "quit"
        ) + "\n";

        InputStream originalIn = System.in;
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            CROBS crobs = new CROBS();
            crobs.start(new String[]{"-html", htmlFile.toString(), "-txt", txtFile.toString()});

            assertNotNull(crobs.getUserOper());
            assertNotNull(crobs.getRoomManager());
            assertNotNull(crobs.getBookingManager());
            assertNull(crobs.getUserOper().getCurrentUserId());
        } finally {
            System.setIn(originalIn);
        }
    }
}
