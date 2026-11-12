package hk.edu.polyu.comp.comp2021.crobs.model;

public class User {

    private final String userId;
    private String name;
    private Role role;

    public User(String userId, String name, Role role){
        this.userId = "U"+ String.valueOf(userId);
        this.name = name;
        this.role = role;
    }

    public String getUserId() {return this.userId;}
    public String getName() {return this.name;}
    public Role getRole() {return this.role;}

    public void setRole(Role role) {
        this.role = role;
    }


}
