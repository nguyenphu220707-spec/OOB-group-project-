package hk.edu.polyu.comp.comp2021.crobs.model;

import java.util.HashMap;

public class UserOper {
    private String currentUserId = null;
    private final HashMap<String, User> users = new HashMap<>();
    public int userCnt = 0;
    private BookingManager bookingManager;

    public UserOper() {
        User superAdmin = new User(String.valueOf(userCnt++), null, Role.superadmin);
        users.put(superAdmin.getUserId(), superAdmin);
    }

    public void setBookingManager(BookingManager bookingManager) {
        this.bookingManager = bookingManager;
    }

    // level 2 = admin/superadmin, level 3 = superadmin
    public boolean hasPermission(int level) {
        if (this.currentUserId == null) {
            return false;
        }
        int currentLevel = users.get(this.currentUserId).getRole().getLevel();
        return currentLevel >= level;
    }

    public void login(String userId) {
        if (userId == null || !users.containsKey(userId)) {
            System.out.println("No such user");
            return;
        }
        if (currentUserId != null) {
            System.out.println("You must logout " + currentUserId + " first.");
            return;
        }
        this.currentUserId = userId;
    }

    public void logout() {
        if (currentUserId == null) {
            System.out.println("No one is logging in!");
            return;
        }
        this.currentUserId = null;
    }

    public void addUser(String name) {
        if (!hasPermission(2)) {
            System.out.println("You don't have the permission to add new user!");
            return;
        }
        if (name == null || name.trim().isEmpty()) {
            System.out.println("Name is missing");
            return;
        }
        User newUser = new User(String.valueOf(userCnt++), name, Role.regular);
        users.put(newUser.getUserId(), newUser);
    }

    public void removeUser(String userId) {
        if (!hasPermission(2)) {
            System.out.println("You don't have the permission to remove user!");
            return;
        }
        if (userId == null || !users.containsKey(userId)) {
            System.out.println("Your input is wrong! No such user.");
            return;
        }
        if (users.get(userId).getRole() != Role.regular) {
            System.out.println("You only allow to remove a regular user.");
            return;
        }
        users.remove(userId);
        if (bookingManager != null) {
            bookingManager.cancelBookingsByUser(userId);
        }
    }

    public void changeRole(String userId, Role newRole) {
        if (!hasPermission(3)) {
            System.out.println("You are not allowed to change your role");
            return;
        }
        if (userId == null || !users.containsKey(userId)) {
            System.out.println("The userId does not exists.");
            return;
        }
        if (users.get(userId).getRole() == Role.superadmin) {
            System.out.println("You cannot remove superadmin!");
            return;
        }
        if (newRole == null || newRole.equals(Role.superadmin)) {
            System.out.println("This system can only have one super admin");
            return;
        }
        User userObj = users.get(userId);
        userObj.setRole(newRole);
    }

    public String getCurrentUserId() {
        return currentUserId;
    }
}
