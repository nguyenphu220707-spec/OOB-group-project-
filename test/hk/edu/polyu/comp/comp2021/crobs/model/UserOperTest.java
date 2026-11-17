package hk.edu.polyu.comp.comp2021.crobs.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserOperTest {
    private UserOper userOper;

    @BeforeEach
    public void setUp() {
        userOper = new UserOper();
    }

    @Test
    public void testUserAndRole() {
        User u = new User("99", "CaoUser", Role.regular);
        assertEquals("U99", u.getUserId());
        assertEquals("CaoUser", u.getName());
        assertEquals(Role.regular, u.getRole());

        u.setRole(Role.admin);
        assertEquals(Role.admin, u.getRole());

        assertEquals(1, Role.regular.getLevel());
        assertEquals(2, Role.admin.getLevel());
        assertEquals(3, Role.superadmin.getLevel());
    }

    @Test
    public void testLoginAndLogout() {
        userOper.logout();
        userOper.login(null);
        userOper.login("U999");
        assertNull(userOper.getCurrentUserId());
        assertFalse(userOper.hasPermission(1));

        userOper.login("U0");
        assertEquals("U0", userOper.getCurrentUserId());
        assertTrue(userOper.hasPermission(3));

        userOper.login("U0");
        assertEquals("U0", userOper.getCurrentUserId());

        userOper.logout();
        assertNull(userOper.getCurrentUserId());
    }

    @Test
    public void testAddUser() {
        userOper.addUser("Alice");

        userOper.login("U0");
        userOper.addUser(null);
        userOper.addUser("   ");
        userOper.addUser("Alice");
        userOper.addUser("Bob");

        userOper.logout();
        userOper.login("U1");
        assertFalse(userOper.hasPermission(2));
        userOper.addUser("Charlie");
    }

    @Test
    public void testChangeRole() {
        userOper.changeRole("U1", Role.admin);

        userOper.login("U0");
        userOper.addUser("Alice");

        userOper.changeRole(null, Role.admin);
        userOper.changeRole("U999", Role.admin);
        userOper.changeRole("U0", Role.regular);
        userOper.changeRole("U1", null);
        userOper.changeRole("U1", Role.superadmin);

        userOper.changeRole("U1", Role.admin);
        userOper.logout();

        userOper.login("U1");
        assertFalse(userOper.hasPermission(3));
        userOper.changeRole("U1", Role.regular);
    }

    @Test
    public void testRemoveUser() {
        userOper.removeUser("U1");

        userOper.login("U0");
        userOper.addUser("Alice");
        userOper.addUser("Bob");
        userOper.addUser("Charlie");
        userOper.changeRole("U1", Role.admin);

        userOper.removeUser(null);
        userOper.removeUser("U999");
        userOper.removeUser("U0");
        userOper.removeUser("U1");

        userOper.removeUser("U2");

        BookingManager bm = new BookingManager();
        userOper.setBookingManager(bm);
        userOper.removeUser("U3");
    }
}
