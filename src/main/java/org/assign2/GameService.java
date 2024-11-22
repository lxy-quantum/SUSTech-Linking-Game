package org.assign2;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ConcurrentMap;

public class GameService implements Runnable {
    private final Socket socket;
    private final Scanner in;
    private final PrintWriter out;
    private final ConcurrentMap<String, Player> players;
    private String clientId;

    public GameService(Socket socket, ConcurrentMap<String, Player> players) throws IOException {
        this.socket = socket;
        this.players = players;
        this.in = new Scanner(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void run() {
        String command = in.next();
        while (true) {
            switch (command) {
                case "REGISTER":
                    out.println("200 OK");
                    break;
                case "LOGIN":
                    break;
                case "RANDOM_RIVAL":
                case "PICK_RIVAL":
            }
        }
    }
}
