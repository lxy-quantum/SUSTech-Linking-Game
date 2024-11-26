package org.linkingGame;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class GameServer {
    public static void main(String[] args) {
        ConcurrentHashMap<String, Player> players;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("players.ser"))) {
            players = (ConcurrentHashMap<String, Player>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            players = new ConcurrentHashMap<>();
            System.out.println("cannot load players data!");
        }

        ConcurrentMap<String, Socket> onlineMatchingClients = new ConcurrentHashMap<>();
        ConcurrentMap<String, Socket> onlinePickingClients = new ConcurrentHashMap<>();

        try(ServerSocket serverSocket = new ServerSocket(1234)) {
            System.out.println("Server started ......");

            Thread matchingThread = new Thread(new MatchingService(players, onlineMatchingClients, onlinePickingClients));
            matchingThread.start();

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected.");

                BeginningService service = new BeginningService(socket, players, onlineMatchingClients, onlinePickingClients);
                Thread beginningThread = new Thread(service);
                beginningThread.start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
