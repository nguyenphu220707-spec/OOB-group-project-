package hk.edu.polyu.comp.comp2021.crobs.view;

import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class CROBSGUI extends JFrame {
    private final CROBS crobs;
    private final JLabel statusLabel;
    private final JTextArea outputArea;
    private final JTextField rawCommandField;

    public CROBSGUI(String[] args) {
        this.crobs = new CROBS();
        this.crobs.parseArgs(args);

        setTitle("CROBS - Campus Room Booking System");
        setSize(920, 680);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        // Top bar: user status + Undo / Redo / Quit
        JPanel topPanel = new JPanel(new BorderLayout(8, 8));
        topPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 4, 10));

        statusLabel = new JLabel("Current User: None");
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        topPanel.add(statusLabel, BorderLayout.WEST);

        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        JButton undoBtn = new JButton("Undo");
        JButton redoBtn = new JButton("Redo");
        JButton quitBtn = new JButton("Save Logs & Quit");

        undoBtn.addActionListener(e -> runCommand("undo"));
        redoBtn.addActionListener(e -> runCommand("redo"));
        quitBtn.addActionListener(e -> quitApplication());

        actionBar.add(undoBtn);
        actionBar.add(redoBtn);
        actionBar.add(quitBtn);
        topPanel.add(actionBar, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center: Tabs for Users, Rooms, Bookings
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Users", createUserPanel());
        tabs.addTab("Rooms", createRoomPanel());
        tabs.addTab("Bookings", createBookingPanel());
        add(tabs, BorderLayout.CENTER);

        // Bottom: Direct CLI command bar + Output Console
        JPanel bottomPanel = new JPanel(new BorderLayout(6, 6));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(4, 10, 10, 10));

        JPanel cliBar = new JPanel(new BorderLayout(6, 0));
        rawCommandField = new JTextField();
        JButton runRawBtn = new JButton("Run Command");
        JButton clearBtn = new JButton("Clear Output");

        runRawBtn.addActionListener(e -> {
            String cmd = rawCommandField.getText().trim();
            if (!cmd.isEmpty()) {
                rawCommandField.setText("");
                runCommand(cmd);
            }
        });
        rawCommandField.addActionListener(e -> runRawBtn.doClick());

        JPanel cliButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        cliButtons.add(runRawBtn);
        cliButtons.add(clearBtn);

        cliBar.add(new JLabel("CLI Input: "), BorderLayout.WEST);
        cliBar.add(rawCommandField, BorderLayout.CENTER);
        cliBar.add(cliButtons, BorderLayout.EAST);

        outputArea = new JTextArea(11, 70);
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        clearBtn.addActionListener(e -> outputArea.setText(""));
        JScrollPane scrollPane = new JScrollPane(outputArea);

        bottomPanel.add(cliBar, BorderLayout.NORTH);
        bottomPanel.add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                quitApplication();
            }
        });
    }

    private JPanel createUserPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 6, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Login / Logout
        JPanel loginRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField loginIdField = new JTextField(10);
        JButton loginBtn = new JButton("Login");
        JButton logoutBtn = new JButton("Logout");
        loginBtn.addActionListener(e -> runCommand("login " + loginIdField.getText().trim()));
        logoutBtn.addActionListener(e -> runCommand("logout"));
        loginRow.add(new JLabel("User ID:"));
        loginRow.add(loginIdField);
        loginRow.add(loginBtn);
        loginRow.add(logoutBtn);

        // Add User
        JPanel addRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField addNameField = new JTextField(15);
        JButton addUserBtn = new JButton("Add User");
        addUserBtn.addActionListener(e -> runCommand("addUser " + addNameField.getText().trim()));
        addRow.add(new JLabel("Name:"));
        addRow.add(addNameField);
        addRow.add(addUserBtn);

        // Remove User
        JPanel removeRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField removeIdField = new JTextField(10);
        JButton removeUserBtn = new JButton("Remove User");
        removeUserBtn.addActionListener(e -> runCommand("removeUser " + removeIdField.getText().trim()));
        removeRow.add(new JLabel("User ID:"));
        removeRow.add(removeIdField);
        removeRow.add(removeUserBtn);

        // Change Role
        JPanel roleRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField roleUserIdField = new JTextField(8);
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"regular", "admin"});
        JButton changeRoleBtn = new JButton("Change Role");
        changeRoleBtn.addActionListener(e ->
                runCommand("changeRole " + roleUserIdField.getText().trim() + " " + roleBox.getSelectedItem()));
        roleRow.add(new JLabel("User ID:"));
        roleRow.add(roleUserIdField);
        roleRow.add(new JLabel("Role:"));
        roleRow.add(roleBox);
        roleRow.add(changeRoleBtn);

        panel.add(loginRow);
        panel.add(addRow);
        panel.add(removeRow);
        panel.add(roleRow);
        return panel;
    }

    private JPanel createRoomPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 6, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add Room
        JPanel addRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<String> addTypeBox = new JComboBox<>(
                new String[]{"lectureTheatre", "computerLab", "seminarRoom", "meetingRoom"});
        JTextField addCapField = new JTextField(5);
        JTextField addFeatField = new JTextField(14);
        JButton addRoomBtn = new JButton("Add Room");
        addRoomBtn.addActionListener(e -> runCommand(
                "addRoom " + addTypeBox.getSelectedItem() + " "
                        + addCapField.getText().trim() + " " + addFeatField.getText().trim()));
        addRow.add(new JLabel("Type:"));
        addRow.add(addTypeBox);
        addRow.add(new JLabel("Capacity:"));
        addRow.add(addCapField);
        addRow.add(new JLabel("Features:"));
        addRow.add(addFeatField);
        addRow.add(addRoomBtn);

        // Modify Room
        JPanel modRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField modIdField = new JTextField(4);
        JComboBox<String> modTypeBox = new JComboBox<>(
                new String[]{"lectureTheatre", "computerLab", "seminarRoom", "meetingRoom"});
        JTextField modCapField = new JTextField(5);
        JTextField modFeatField = new JTextField(12);
        JButton modRoomBtn = new JButton("Modify Room");
        modRoomBtn.addActionListener(e -> runCommand(
                "modifyRoom " + modIdField.getText().trim() + " " + modTypeBox.getSelectedItem()
                        + " " + modCapField.getText().trim() + " " + modFeatField.getText().trim()));
        modRow.add(new JLabel("ID:"));
        modRow.add(modIdField);
        modRow.add(new JLabel("Type:"));
        modRow.add(modTypeBox);
        modRow.add(new JLabel("Cap:"));
        modRow.add(modCapField);
        modRow.add(new JLabel("Feat:"));
        modRow.add(modFeatField);
        modRow.add(modRoomBtn);

        // Delete / List Room / List All
        JPanel listRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField roomIdField = new JTextField(6);
        JButton listRoomBtn = new JButton("List Room");
        JButton deleteRoomBtn = new JButton("Delete Room");
        JButton listAllBtn = new JButton("List All Rooms");
        listRoomBtn.addActionListener(e -> runCommand("listRoom " + roomIdField.getText().trim()));
        deleteRoomBtn.addActionListener(e -> runCommand("deleteRoom " + roomIdField.getText().trim()));
        listAllBtn.addActionListener(e -> runCommand("listAllRooms"));
        listRow.add(new JLabel("Room ID:"));
        listRow.add(roomIdField);
        listRow.add(listRoomBtn);
        listRow.add(deleteRoomBtn);
        listRow.add(listAllBtn);

        // Search Available Rooms
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField sDate = new JTextField("2025-05-01", 8);
        JTextField sStart = new JTextField("10:00", 5);
        JTextField sEnd = new JTextField("11:00", 5);
        JTextField sCap = new JTextField("10", 4);
        JTextField sFeat = new JTextField(10);
        JButton searchBtn = new JButton("Search Rooms");
        searchBtn.addActionListener(e -> {
            String feat = sFeat.getText().trim();
            String cmd = "searchAvailableRooms " + sDate.getText().trim() + " "
                    + sStart.getText().trim() + " " + sEnd.getText().trim() + " " + sCap.getText().trim();
            if (!feat.isEmpty()) {
                cmd += " " + feat;
            }
            runCommand(cmd);
        });
        searchRow.add(new JLabel("Date:"));
        searchRow.add(sDate);
        searchRow.add(new JLabel("Start:"));
        searchRow.add(sStart);
        searchRow.add(new JLabel("End:"));
        searchRow.add(sEnd);
        searchRow.add(new JLabel("Cap:"));
        searchRow.add(sCap);
        searchRow.add(new JLabel("Feat:"));
        searchRow.add(sFeat);
        searchRow.add(searchBtn);

        panel.add(addRow);
        panel.add(modRow);
        panel.add(listRow);
        panel.add(searchRow);
        return panel;
    }

    private JPanel createBookingPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 6, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create Booking
        JPanel createRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField cRoom = new JTextField(4);
        JTextField cDate = new JTextField("2025-05-01", 8);
        JTextField cStart = new JTextField("10:00", 5);
        JTextField cEnd = new JTextField("11:00", 5);
        JTextField cPurpose = new JTextField(12);
        JButton createBtn = new JButton("Create Booking");
        createBtn.addActionListener(e -> runCommand(
                "createBooking " + cRoom.getText().trim() + " " + cDate.getText().trim() + " "
                        + cStart.getText().trim() + " " + cEnd.getText().trim() + " "
                        + cPurpose.getText().trim()));
        createRow.add(new JLabel("Room:"));
        createRow.add(cRoom);
        createRow.add(new JLabel("Date:"));
        createRow.add(cDate);
        createRow.add(new JLabel("Start:"));
        createRow.add(cStart);
        createRow.add(new JLabel("End:"));
        createRow.add(cEnd);
        createRow.add(new JLabel("Purpose:"));
        createRow.add(cPurpose);
        createRow.add(createBtn);

        // Modify Booking
        JPanel modRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField mBookId = new JTextField(5);
        JTextField mRoom = new JTextField(4);
        JTextField mDate = new JTextField("2025-05-01", 8);
        JTextField mStart = new JTextField("11:00", 5);
        JTextField mEnd = new JTextField("12:00", 5);
        JTextField mPurpose = new JTextField(10);
        JButton modBtn = new JButton("Modify Booking");
        modBtn.addActionListener(e -> runCommand(
                "modifyBooking " + mBookId.getText().trim() + " " + mRoom.getText().trim() + " "
                        + mDate.getText().trim() + " " + mStart.getText().trim() + " "
                        + mEnd.getText().trim() + " " + mPurpose.getText().trim()));
        modRow.add(new JLabel("Booking ID:"));
        modRow.add(mBookId);
        modRow.add(new JLabel("Room:"));
        modRow.add(mRoom);
        modRow.add(new JLabel("Date:"));
        modRow.add(mDate);
        modRow.add(new JLabel("Start:"));
        modRow.add(mStart);
        modRow.add(new JLabel("End:"));
        modRow.add(mEnd);
        modRow.add(new JLabel("Purpose:"));
        modRow.add(mPurpose);
        modRow.add(modBtn);

        // Delete / List My Bookings
        JPanel delRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField dBookId = new JTextField(8);
        JButton delBtn = new JButton("Delete Booking");
        JButton listBtn = new JButton("List My Bookings");
        delBtn.addActionListener(e -> runCommand("deleteBooking " + dBookId.getText().trim()));
        listBtn.addActionListener(e -> runCommand("listBookings"));
        delRow.add(new JLabel("Booking ID:"));
        delRow.add(dBookId);
        delRow.add(delBtn);
        delRow.add(listBtn);

        // Weekly Schedule
        JPanel schedRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField wRoom = new JTextField(6);
        JTextField wDate = new JTextField("2025-05-01", 8);
        JButton schedBtn = new JButton("Weekly Schedule");
        schedBtn.addActionListener(e ->
                runCommand("weeklySchedule " + wRoom.getText().trim() + " " + wDate.getText().trim()));
        schedRow.add(new JLabel("Room ID:"));
        schedRow.add(wRoom);
        schedRow.add(new JLabel("Date:"));
        schedRow.add(wDate);
        schedRow.add(schedBtn);

        panel.add(createRow);
        panel.add(modRow);
        panel.add(delRow);
        panel.add(schedRow);
        return panel;
    }

    private void runCommand(String cmd) {
        if (cmd == null || cmd.trim().isEmpty()) {
            return;
        }
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        try {
            crobs.recordCommand(cmd);
            if ("quit".equals(cmd.trim())) {
                quitApplication();
                return;
            }
            crobs.executeCommand(cmd);
        } finally {
            System.setOut(originalOut);
        }

        outputArea.append("> " + cmd + "\n");
        String result = baos.toString();
        if (!result.isEmpty()) {
            outputArea.append(result);
            if (!result.endsWith("\n")) {
                outputArea.append("\n");
            }
        }
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
        updateStatusLabel();
    }

    private void updateStatusLabel() {
        String current = crobs.getUserOper().getCurrentUserId();
        statusLabel.setText("Current User: " + (current == null ? "None" : current));
    }

    private void quitApplication() {
        crobs.recordCommand("quit");
        dispose();
        System.exit(0);
    }

    public static void launch(String[] args) {
        SwingUtilities.invokeLater(() -> new CROBSGUI(args).setVisible(true));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
