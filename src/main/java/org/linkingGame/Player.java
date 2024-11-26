package org.linkingGame;

public class Player {
    String ID;
    String password;
    private boolean loggedIn = false;

    public Player(String ID, String password) {
        this.ID = ID;
        this.password = password;
    }

    public void setLoggedIn() {
        loggedIn = true;
    }

    public void setLoggedOut() {
        loggedIn = false;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }
}
