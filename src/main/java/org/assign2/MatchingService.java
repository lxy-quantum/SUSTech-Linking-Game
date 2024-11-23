package org.assign2;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.ConcurrentMap;

public class MatchingService implements Runnable{
    private final ConcurrentMap<String, Socket> clientMap;

    public MatchingService(ConcurrentMap<String, Socket> clientMap) {
        this.clientMap = clientMap;
    }

    @Override
    public void run() {
        while (true) {
            while (clientMap.size() > 1) {
                Iterator<ConcurrentMap.Entry<String, Socket>> iterator = clientMap.entrySet().iterator();
                ConcurrentMap.Entry<String, Socket> clientEntry1 = iterator.next();
                iterator.remove();
                ConcurrentMap.Entry<String, Socket> clientEntry2 = iterator.next();
                iterator.remove();

                String player1 = clientEntry1.getKey();
                String player2 = clientEntry2.getKey();
                Socket playerSocket1 = clientEntry1.getValue();
                Socket playerSocket2 = clientEntry2.getValue();

                try {
                    PrintWriter out1 = new PrintWriter(playerSocket1.getOutputStream(), true);
                    PrintWriter out2 = new PrintWriter(playerSocket2.getOutputStream(), true);
                    out1.println("200 OK matched");
                    out2.println("200 OK matched");
                    out1.println(player2);
                    out2.println(player1);
                    out1.println("choose row and column");
                    out2.println("no need to choose");

                    Scanner in1 = new Scanner(playerSocket1.getInputStream());
                    String command = in1.next();
                    if (command.equals("BOARD_SIZE")) {
                        int row = in1.nextInt();
                        int col = in1.nextInt();
                        //settle the board with required size

                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                Thread gameThread = new Thread(new GameService(player1, player2, playerSocket1, playerSocket2));
                gameThread.start();
            }
        }
    }
}
