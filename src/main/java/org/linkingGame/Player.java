package org.linkingGame;

import java.io.Serializable;
import java.util.ArrayList;

public class Player implements Serializable {
    static class GameRecord {
        String rivalID;
        boolean won;
        boolean tie;
        int myScore, rivalScore;

        GameRecord(String rivalID, boolean won, boolean tie, int myScore, int rivalScore) {
            this.rivalID = rivalID;
            this.won = won;
            this.tie = tie;
            this.myScore = myScore;
            this.rivalScore = rivalScore;
        }
    }

    String ID;
    String password;
    private boolean loggedIn = false;
    protected final ArrayList<GameRecord> gameRecords = new ArrayList<>();

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

    public void addGameRecord(String rivalID, boolean won, boolean tie, int myScore, int rivalScore) {
        gameRecords.add(new GameRecord(rivalID, won, tie, myScore, rivalScore));
    }
}
