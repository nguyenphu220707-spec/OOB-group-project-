package hk.edu.polyu.comp.comp2021.crobs.view;

import hk.edu.polyu.comp.comp2021.crobs.Application;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class CROBSGUITest {

    @Test
    public void testCROBSGUIInitializesAndRunsCommands(@TempDir Path tempDir) throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }

        String[] args = {
                "-html", tempDir.resolve("gui.html").toString(),
                "-txt", tempDir.resolve("gui.txt").toString()
        };

        SwingUtilities.invokeAndWait(() -> {
            CROBSGUI gui = new CROBSGUI(args);
            assertEquals("CROBS - Campus Room Booking System", gui.getTitle());
            assertEquals(JFrame.DO_NOTHING_ON_CLOSE, gui.getDefaultCloseOperation());

            // Verify all 3 tabs (Users, Rooms, Bookings) are created
            JTabbedPane tabs = findComponent(gui, JTabbedPane.class);
            assertNotNull(tabs);
            assertEquals(3, tabs.getTabCount());
            assertEquals("Users", tabs.getTitleAt(0));
            assertEquals("Rooms", tabs.getTitleAt(1));
            assertEquals("Bookings", tabs.getTitleAt(2));

            // Find the bottom CLI input field via the "Run Command" button
            JButton runBtn = findButton(gui, "Run Command");
            assertNotNull(runBtn);
            JTextField cliInput = (JTextField) ((Container) runBtn.getParent().getParent()).getComponent(1);

            cliInput.setText("login U0");
            runBtn.doClick();

            JTextArea outputArea = findComponent(gui, JTextArea.class);
            assertNotNull(outputArea);
            assertTrue(outputArea.getText().contains("> login U0"));

            gui.dispose();
        });
    }

    @Test
    public void testGUIUndoAndRedoButtons(@TempDir Path tempDir) throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }

        String[] args = {
                "-html", tempDir.resolve("undo_gui.html").toString(),
                "-txt", tempDir.resolve("undo_gui.txt").toString()
        };

        SwingUtilities.invokeAndWait(() -> {
            CROBSGUI gui = new CROBSGUI(args);
            JButton runBtn = findButton(gui, "Run Command");
            JButton undoBtn = findButton(gui, "Undo");
            JButton redoBtn = findButton(gui, "Redo");
            JTextArea outputArea = findComponent(gui, JTextArea.class);

            assertNotNull(runBtn);
            assertNotNull(undoBtn);
            assertNotNull(redoBtn);
            assertNotNull(outputArea);

            JTextField cliInput = (JTextField) ((Container) runBtn.getParent().getParent()).getComponent(1);

            // Log in and add a room via CLI bar
            cliInput.setText("login U0");
            runBtn.doClick();
            cliInput.setText("addRoom meetingRoom 10 tv");
            runBtn.doClick();

            // Click Undo button
            undoBtn.doClick();
            assertTrue(outputArea.getText().contains("Undid: addRoom meetingRoom 10 tv"));

            // Click Redo button
            redoBtn.doClick();
            assertTrue(outputArea.getText().contains("Redid: addRoom meetingRoom 10 tv"));

            gui.dispose();
        });
    }

    @Test
    public void testApplicationCLIMode(@TempDir Path tempDir) {
        String[] args = {
                "-html", tempDir.resolve("app.html").toString(),
                "-txt", tempDir.resolve("app.txt").toString()
        };

        InputStream originalIn = System.in;
        try {
            System.setIn(new ByteArrayInputStream("quit\n".getBytes()));
            Application.main(args);
        } finally {
            System.setIn(originalIn);
        }
    }

    private <T extends Component> T findComponent(Container root, Class<T> type) {
        for (Component c : root.getComponents()) {
            if (type.isInstance(c)) {
                return type.cast(c);
            }
            if (c instanceof Container) {
                T found = findComponent((Container) c, type);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private JButton findButton(Container root, String text) {
        for (Component c : root.getComponents()) {
            if (c instanceof JButton && text.equals(((JButton) c).getText())) {
                return (JButton) c;
            }
            if (c instanceof Container) {
                JButton found = findButton((Container) c, text);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
