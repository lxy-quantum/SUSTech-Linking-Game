package org.linkingGame;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class BeginningService implements Runnable {
    private final Socket socket;
    private final Scanner in;
    private final PrintWriter out;
    private final ConcurrentHashMap<String, Player> players;
    private final ConcurrentMap<String, Socket> onlineMatchingClients;
    private final ConcurrentMap<String, Socket> onlinePickingClients;
    private String clientId = null;

    public BeginningService(Socket socket, ConcurrentHashMap<String, Player> players, ConcurrentMap<String, Socket> onlineMatchingClients,
                            ConcurrentMap<String, Socket> onlinePickingClients) throws IOException {
        this.socket = socket;
        this.players = players;
        this.onlineMatchingClients = onlineMatchingClients;
        this.onlinePickingClients = onlinePickingClients;
        this.in = new Scanner(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void run() {
        while (true) {
            try {
                String command = in.next();
                switch (command) {
                    case "REGISTER": {
                        String id = in.next();
                        String password = in.next();
                        handleRegister(id, password);
                        break;
                    }
                    case "LOGIN": {
                        String id = in.next();
                        String password = in.next();
                        handleLogin(id, password);
                        break;
                    }
                    case "MATCH": {
                        onlineMatchingClients.put(clientId, socket);
                        return;
                    }
                    case "PICK": {
                        onlinePickingClients.put(clientId, socket);
                        out.println("200 OK");
                        //

                    }
                    case "GET_RECORD": {
                        ArrayList<Player.GameRecord> gameRecords = players.get(clientId).gameRecords;
                        out.println(gameRecords.size());
                        for (Player.GameRecord gameRecord : gameRecords) {
                            out.println(gameRecord.rivalID);
                            if (gameRecord.won) {
                                out.println("won");
                            } else if (gameRecord.tie) {
                                out.println("tie");
                            } else {
                                out.println("lost");
                            }
                            out.println(gameRecord.myScore);
                            out.println(gameRecord.rivalScore);
                        }
                    }
                }
            } catch (Exception e) {
                if (clientId != null) {
                    players.get(clientId).setLoggedOut();
                    //save the logged-out info
                    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("players.ser"))) {
                        oos.writeObject(players);
                    } catch (IOException ioe) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void handleRegister(String id, String password) {
        if (players.containsKey(id)) {
            out.println("400 already registered");
            return;
        }
        players.put(id, new Player(id, password));
        //save the player info
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("players.ser"))) {
            oos.writeObject(players);
        } catch (IOException e) {
            e.printStackTrace();
        }
        out.println("200 OK registered");
    }

    private void handleLogin(String id, String password) {
        if (!players.containsKey(id)) {
            out.println("400 wrong ID");
            return;
        }
        if (!players.get(id).password.equals(password)) {
            out.println("400 wrong password");
            return;
        }
        if (players.get(id).isLoggedIn()) {
            out.println("400 already logged in");
            return;
        }
        players.get(id).setLoggedIn();
        clientId = id;
        out.println("200 OK logged in");
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
