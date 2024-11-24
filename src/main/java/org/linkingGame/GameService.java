package org.linkingGame;

import java.net.Socket;

public class GameService implements Runnable{
    private final String player1, player2;
    private final Socket playerSocket1, playerSocket2;

    public GameService(String player1, String player2, Socket playerSocket1, Socket playerSocket2) {
        this.player1 = player1;
        this.player2 = player2;
        this.playerSocket1 = playerSocket1;
        this.playerSocket2 = playerSocket2;
    }

    @Override
    public void run() {

    }
}
