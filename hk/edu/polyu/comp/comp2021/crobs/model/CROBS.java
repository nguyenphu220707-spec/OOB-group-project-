package hk.edu.polyu.comp.comp2021.crobs.model;


import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Stack;

public class CROBS {
    private UserOper userOper;
    private RoomManager roomManager;
    private BookingManager bookingManager;

    private String htmlPath;
    private String txtPath;
    private final List<String> commandHistory = new ArrayList<>();

    private final List<String> undoableCommands = new ArrayList<>();
    private final Stack<String> redoStack = new Stack<>();

    public CROBS() {
        this.userOper = new UserOper();
        this.bookingManager = new BookingManager();
        this.roomManager = new RoomManager(userOper, bookingManager);

        this.userOper.setBookingManager(bookingManager);
        this.bookingManager.setRoomManager(roomManager);
    }

    public void parseArgs(String[] args) {
        if (args == null) {
            return;
        }
        for (int i = 0; i < args.length - 1; i++) {
            if ("-html".equals(args[i])) {
                htmlPath = args[i + 1];
            } else if ("-txt".equals(args[i])) {
                txtPath = args[i + 1];
            }
        }
    }

    public void start(String[] args) {
        parseArgs(args);
        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }

            recordCommand(line);

            if ("quit".equals(line)) {
                saveLogs();
                break;
            }

            executeCommand(line);
        }

        scanner.close();
    }

    public void recordCommand(String command) {
        commandHistory.add(command);
        saveLogs();
    }

    private void executeCommandInternal(String line) {
        String[] parts = line.split("\\s+");
        String cmd = parts[0];

        switch (cmd) {
            // --- User commands (REQ2 - REQ6) ---
            case "addUser":
                if (parts.length < 2) {
                    System.out.println("Usage: addUser <name>");
                } else {
                    userOper.addUser(parts[1]);
                }
                break;

            case "removeUser":
                if (parts.length < 2) {
                    System.out.println("Usage: removeUser <userId>");
                } else {
                    userOper.removeUser(parts[1]);
                }
                break;

            case "changeRole":
                if (parts.length < 3) {
                    System.out.println("Usage: changeRole <userId> <newRole>");
                } else {
                    try {
                        Role newRole = Role.valueOf(parts[2].toLowerCase());
                        userOper.changeRole(parts[1], newRole);
                    } catch (IllegalArgumentException e) {
                        System.out.println("Invalid role: " + parts[2]);
                    }
                }
                break;

            case "login":
                if (parts.length < 2) {
                    System.out.println("Usage: login <userId>");
                } else {
                    userOper.login(parts[1]);
                }
                break;

            case "logout":
                userOper.logout();
                break;

            // --- Room commands (REQ7 - REQ9, REQ13, REQ15 - REQ16) ---
            case "addRoom":
                if (parts.length < 4) {
                    System.out.println("Usage: addRoom <roomType> <capacity> <features>");
                } else {
                    try {
                        int capacity = Integer.parseInt(parts[2]);
                        System.out.println(roomManager.addRoom(parts[1], capacity, parts[3]));
                    } catch (NumberFormatException e) {
                        System.out.println("Error: Capacity must be a number.");
                    }
                }
                break;

            case "modifyRoom":
                if (parts.length < 5) {
                    System.out.println("Usage: modifyRoom <roomId> <roomType> <capacity> <features>");
                } else {
                    try {
                        int capacity = Integer.parseInt(parts[3]);
                        System.out.println(roomManager.modifyRoom(parts[1], parts[2], capacity, parts[4]));
                    } catch (NumberFormatException e) {
                        System.out.println("Error: Capacity must be a number.");
                    }
                }
                break;

            case "deleteRoom":
                if (parts.length < 2) {
                    System.out.println("Usage: deleteRoom <roomId>");
                } else {
                    System.out.println(roomManager.deleteRoom(parts[1]));
                }
                break;

            case "listRoom":
                if (parts.length < 2) {
                    System.out.println("Usage: listRoom <roomId>");
                } else {
                    System.out.println(roomManager.listRoom(parts[1]));
                }
                break;

            case "listAllRooms":
                System.out.println(roomManager.listAllRooms());
                break;

            case "searchAvailableRooms":
                if (parts.length < 5) {
                    System.out.println("Usage: searchAvailableRooms <date> <startTime> <endTime> <minCapacity> [features]");
                } else {
                    try {
                        LocalDate date = LocalDate.parse(parts[1]);
                        LocalTime start = LocalTime.parse(parts[2]);
                        LocalTime end = LocalTime.parse(parts[3]);
                        int minCap = Integer.parseInt(parts[4]);
                        String features = (parts.length >= 6) ? parts[5] : "";
                        System.out.println(roomManager.searchAvailableRooms(date, start, end, minCap, features));
                    } catch (Exception e) {
                        System.out.println("Error: Invalid date, time, or capacity format.");
                    }
                }
                break;

            // --- Booking commands (REQ10 - REQ12, REQ14, REQ17) ---
            case "createBooking":
                if (parts.length < 6) {
                    System.out.println("Usage: createBooking <roomId> <date> <startTime> <endTime> <purpose>");
                } else {
                    try {
                        LocalDate date = LocalDate.parse(parts[2]);
                        LocalTime start = LocalTime.parse(parts[3]);
                        LocalTime end = LocalTime.parse(parts[4]);
                        String purpose = String.join(" ", Arrays.copyOfRange(parts, 5, parts.length));
                        System.out.println(bookingManager.createBooking(userOper.getCurrentUserId(), parts[1], date, start, end, purpose));
                    } catch (Exception e) {
                        System.out.println("Error: Invalid date or time format.");
                    }
                }
                break;

            case "modifyBooking":
                if (parts.length < 7) {
                    System.out.println("Usage: modifyBooking <bookingId> <roomId> <date> <startTime> <endTime> <purpose>");
                } else {
                    try {
                        LocalDate date = LocalDate.parse(parts[3]);
                        LocalTime start = LocalTime.parse(parts[4]);
                        LocalTime end = LocalTime.parse(parts[5]);
                        String purpose = String.join(" ", Arrays.copyOfRange(parts, 6, parts.length));
                        System.out.println(bookingManager.modifyBooking(userOper.getCurrentUserId(), parts[1], parts[2], date, start, end, purpose));
                    } catch (Exception e) {
                        System.out.println("Error: Invalid date or time format.");
                    }
                }
                break;

            case "deleteBooking":
                if (parts.length < 2) {
                    System.out.println("Usage: deleteBooking <bookingId>");
                } else {
                    System.out.println(bookingManager.deleteBooking(userOper.getCurrentUserId(), parts[1]));
                }
                break;

            case "listBookings":
                System.out.println(bookingManager.getListBookingsString(userOper.getCurrentUserId()));
                break;

            case "weeklySchedule":
                if (parts.length < 3) {
                    System.out.println("Usage: weeklySchedule <roomId> <weekStartDate>");
                } else {
                    try {
                        LocalDate weekStart = LocalDate.parse(parts[2]);
                        boolean isAdmin = userOper.hasPermission(2);
                        System.out.println(bookingManager.getWeeklyScheduleString(userOper.getCurrentUserId(), isAdmin, parts[1], weekStart));
                    } catch (Exception e) {
                        System.out.println("Error: Invalid date format.");
                    }
                }
                break;

            default:
                System.out.println("Unknown command: " + cmd);
                break;
        }
    }

    public void executeCommand(String line) {
        String[] parts = line.split("\\s+");
        String cmd = parts[0];

        if ("undo".equals(cmd)) {
            if (undoableCommands.isEmpty()) {
                System.out.println("Nothing to undo.");
            } else {
                String last = undoableCommands.remove(undoableCommands.size() - 1);
                redoStack.push(last);
                replayCommands();
                System.out.println("Undid: " + last);
            }
            return;
        }

        if ("redo".equals(cmd)) {
            if (redoStack.isEmpty()) {
                System.out.println("Nothing to redo.");
            } else {
                String next = redoStack.pop();
                undoableCommands.add(next);
                replayCommands();
                System.out.println("Redid: " + next);
            }
            return;
        }

        if (isUndoable(cmd)) {
            undoableCommands.add(line);
            redoStack.clear();
        }

        executeCommandInternal(line);
    }


    public void saveLogs() {
        if (txtPath != null) {
            try (PrintWriter txtWriter = new PrintWriter(new FileWriter(txtPath))) {
                for (String cmd : commandHistory) {
                    txtWriter.println(cmd);
                }
            } catch (IOException e) {
                System.out.println("Failed to write TXT log: " + e.getMessage());
            }
        }

        if (htmlPath != null) {
            try (PrintWriter htmlWriter = new PrintWriter(new FileWriter(htmlPath))) {
                htmlWriter.println("<html><body><table border=\"1\">");
                htmlWriter.println("<tr><th>No.</th><th>Command</th></tr>");
                for (int i = 0; i < commandHistory.size(); i++) {
                    htmlWriter.println("<tr><td>" + (i + 1) + "</td><td>" + commandHistory.get(i) + "</td></tr>");
                }
                htmlWriter.println("</table></body></html>");
            } catch (IOException e) {
                System.out.println("Failed to write HTML log: " + e.getMessage());
            }
        }
    }

    private boolean isUndoable(String cmd) {
        return "addUser".equals(cmd) || "removeUser".equals(cmd) || "changeRole".equals(cmd)
                || "addRoom".equals(cmd) || "modifyRoom".equals(cmd) || "deleteRoom".equals(cmd)
                || "createBooking".equals(cmd) || "modifyBooking".equals(cmd) || "deleteBooking".equals(cmd);
    }

    private void replayCommands() {
        String savedUser = userOper.getCurrentUserId();
        this.userOper = new UserOper();
        this.bookingManager = new BookingManager();
        this.roomManager = new RoomManager(userOper, bookingManager);
        this.userOper.setBookingManager(bookingManager);
        this.bookingManager.setRoomManager(roomManager);
        if (savedUser != null) {
            userOper.login(savedUser);
        }
        // Replay all undoable commands silently
        java.io.PrintStream originalOut = System.out;
        System.setOut(new java.io.PrintStream(new java.io.OutputStream() {
            @Override public void write(int b) {}
        }));
        try {
            for (String c : undoableCommands) {
                executeCommandInternal(c);
            }
        } finally {
            System.setOut(originalOut);
        }


    }

    public UserOper getUserOper() {
        return userOper;
    }

    public RoomManager getRoomManager() {
        return roomManager;
    }

    public BookingManager getBookingManager() {
        return bookingManager;
    }

    public List<String> getCommandHistory() {
        return commandHistory;
    }
}