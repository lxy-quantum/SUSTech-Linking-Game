package org.assign2;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class GameServer {
    public static void main(String[] args) {
        ConcurrentMap<String, Player> players = new ConcurrentHashMap<>();
        ConcurrentMap<String, Socket> onlineMatchingClients = new ConcurrentHashMap<>();
        Set<String> onlinePickingClients = ConcurrentHashMap.newKeySet();

        try(ServerSocket serverSocket = new ServerSocket(1234)) {
            System.out.println("Server started ......");

            Thread matchingThread = new Thread(new MatchingService(onlineMatchingClients));
            matchingThread.start();

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected.");

                BeginningService service = new BeginningService(socket, players, onlineMatchingClients, onlinePickingClients);
                Thread beginningThread = new Thread(service);
                beginningThread.start();
//                ClientHandler clientHandler = new ClientHandler(socket, clients);
//                clients.add(clientHandler);
//                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
