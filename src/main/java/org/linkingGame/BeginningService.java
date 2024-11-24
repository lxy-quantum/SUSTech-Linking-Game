package org.linkingGame;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

public class BeginningService implements Runnable {
    private final Socket socket;
    private final Scanner in;
    private final PrintWriter out;
    private final ConcurrentMap<String, Player> players;
    private final ConcurrentMap<String, Socket> onlineMatchingClients;
    private final Set<String> onlinePickingClients;
    private String clientId;

    public BeginningService(Socket socket, ConcurrentMap<String, Player> players, ConcurrentMap<String, Socket> onlineMatchingClients,
                            Set<String> onlinePickingClients) throws IOException {
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
            if (!in.hasNext()) return;
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
                    out.println("200 OK matching");
                    return;
                }
                case "PICK": {
                    onlinePickingClients.add(clientId);
                    out.println("200 OK");
                    //

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
        clientId = id;
        out.println("200 OK logged in");
    }
}
