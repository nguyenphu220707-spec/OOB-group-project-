package hk.edu.polyu.comp.comp2021.crobs.model;

public enum Role {
    admin(2), regular(1), superadmin(3);

    private final int level;

    private Role(int level){
        this.level = level;
    }

    public int getLevel(){
        return level;
    }

}
