package org.linkingGame;

import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;

public class Player implements Serializable {
    static class GameRecord implements Serializable {
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
    GameService ongoingGameService;
    String ongoingRival;
    boolean myTurnOngoing = false;
    boolean disconnected = false;

    public Player(String ID, String password) {
        this.ID = ID;
        this.password = password;
    }

    public void setLoggedIn() {
        loggedIn = true;
    }

    public void setLoggedOut() {
        loggedIn = false;
        ongoingGameService = null;
        ongoingRival = null;
        disconnected = false;
    }

    public void setDisconnectedFromGame(boolean myTurnOngoing) {
        loggedIn = false;
        disconnected = true;
        this.myTurnOngoing = myTurnOngoing;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void addGameRecord(String rivalID, boolean won, boolean tie, int myScore, int rivalScore) {
        gameRecords.add(new GameRecord(rivalID, won, tie, myScore, rivalScore));
    }
}
