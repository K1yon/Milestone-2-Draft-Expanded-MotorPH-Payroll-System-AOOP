package motorph.controller;

import motorph.model.User;

public final class UserSession {

    private static UserSession instance;
    private User currentUser;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void login(User user)  { this.currentUser = user; }

    public void logout() {
        if (currentUser != null)
            System.out.println("[Session] " + currentUser.getDisplayName() + " logged out.");
        this.currentUser = null;
    }

    public User    getCurrentUser() { return currentUser; }
    public boolean isHRAdmin()      { return currentUser != null && currentUser.isHRAdmin(); }
    public boolean isLoggedIn()     { return currentUser != null; }
}