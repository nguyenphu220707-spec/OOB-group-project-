package hk.edu.polyu.comp.comp2021.crobs.model;
import java.util.HashMap;
public class UserOper{
    private String currentUserId = null;
    private final HashMap<String, User> users = new HashMap<>();  //ID and user obj.
    public int userCnt = 0;


    public UserOper(){
        User Superadmin = new User(String.valueOf(userCnt++) , null,Role.superadmin);
        users.put(Superadmin.getUserId(), Superadmin);
    }

    public boolean hasPermission(int level){
        if (this.currentUserId == null){
            return false;
        }
        int currentLevel = users.get(this.currentUserId).getRole().getLevel();
        return currentLevel >= level;
    }
    public void login(String userId){
        if (!users.containsKey(userId)) {
            System.out.println("No such user");
            return;
        }
        if(currentUserId != null){
            System.out.println("You must logout " + currentUserId + " first." );
            return;
        }

        this.currentUserId = userId;
    }

    public void logout(){
        if(currentUserId == null){
            System.out.println("No one is logging in!");
            return;
        }

        this.currentUserId = null;
    }

    public void addUser(String name){    // input name to create new user obj
        if(!hasPermission(2)){
            System.out.println("You don't have the permission to add new user!");
            return;
        }

        if(name == ""){
            System.out.println("Name is missing");
            return;
        }

        User newUser = new User(String.valueOf(userCnt++), name, Role.regular);
        users.put(newUser.getUserId(), newUser);

    }

    public void removeUser(String userId){  // TODO: cancel bookings for userId
        if(!hasPermission(2)){
            System.out.println("You don't have the permission to remove user!");
            return;
        }
        if(!users.containsKey(userId)){
            System.out.println("Your input is wrong! No such user.");
            return;
        }

        if(users.get(userId).getRole() != Role.regular){
            System.out.println("You only allow to remove a regular user.");
            return;
        }


        users.remove(userId);
    }

    public void changeRole(String userId, Role newRole) {
        if(!hasPermission(3)){
            System.out.println("You are not allowed to change your role");
            return;
        }

        if (userId == null || !users.containsKey(userId)) {
            System.out.println("The userId does not exists.");
            return;
        }

        if (users.get(userId).getRole() == Role.superadmin){
            System.out.println("You cannot remove superadmin!");
            return;
        }

        if(newRole == null || newRole.equals(Role.superadmin)){
            System.out.println("This system can only have one super admin");
            return;
        }


        User userObj = users.get(userId);
        userObj.setRole(newRole);
    }

    public String getCurrentUserId(){
        return currentUserId;
    }
}
