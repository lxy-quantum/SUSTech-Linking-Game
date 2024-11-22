package org.assign2;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class GameServer {
    public static void main(String[] args) {
        ConcurrentMap<String, Player> players = new ConcurrentHashMap<>();

        try(ServerSocket serverSocket = new ServerSocket(1234)) {
            System.out.println("Server started ......");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected.");

                GameService service = new GameService(socket, players);
                Thread thread = new Thread(service);
                thread.start();
//                ClientHandler clientHandler = new ClientHandler(socket, clients);
//                clients.add(clientHandler);
//                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
