package org.linkingGame;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

public class MatchingService implements Runnable{
    private final ConcurrentMap<String, Player> players;
    private final ConcurrentMap<String, Socket> matchingClientMap;
    private final ConcurrentMap<String, Socket> pickingClientMap;

    public MatchingService(ConcurrentMap<String, Player> players, ConcurrentMap<String, Socket> matchingClientMap,
                           ConcurrentMap<String, Socket> pickingClientMap) {
        this.players = players;
        this.matchingClientMap = matchingClientMap;
        this.pickingClientMap = pickingClientMap;
    }

    @Override
    public void run() {
        while (true) {
            while (matchingClientMap.size() > 1) {
                Iterator<ConcurrentMap.Entry<String, Socket>> iterator = matchingClientMap.entrySet().iterator();
                ConcurrentMap.Entry<String, Socket> clientEntry1 = iterator.next();
                iterator.remove();
                ConcurrentMap.Entry<String, Socket> clientEntry2 = iterator.next();
                iterator.remove();

                String player1ID = clientEntry1.getKey();
                String player2ID = clientEntry2.getKey();
                Socket playerSocket1 = clientEntry1.getValue();
                Socket playerSocket2 = clientEntry2.getValue();

                int[][] board;
                try {
                    OutputStream out1 = playerSocket1.getOutputStream();
                    OutputStream out2 = playerSocket2.getOutputStream();
                    out1.write("200 OK matched\n".getBytes());
                    out1.flush();
                    out2.write("200 OK matched\n".getBytes());
                    out2.flush();
                    out1.write((player2ID + "\n").getBytes());
                    out1.flush();
                    out2.write((player1ID + "\n").getBytes());
                    out2.flush();
                    out1.write("choose row and column\n".getBytes());
                    out1.flush();
                    out2.write("no need to choose\n".getBytes());
                    out2.flush();

                    Scanner in1 = new Scanner(playerSocket1.getInputStream());
                    String command = in1.next();
                    if (command.equals("SET_SIZE")) {
                        int row = in1.nextInt();
                        int col = in1.nextInt();
                        //generate board and check before sending
                        board = generateBoard(row, col);
                        Game game = new Game(board);
                        while (!game.hasAnyLinkingPairs()) {
                            board = generateBoard(row, col);
                            game = new Game(board);
                        }

                        out1.write("board settled\n".getBytes());
                        out1.flush();
                        out1.write((row + "\n").getBytes());
                        out1.flush();
                        out1.write((col + "\n").getBytes());
                        out1.flush();
                        out2.write("board settled\n".getBytes());
                        out2.flush();
                        out2.write((row + "\n").getBytes());
                        out2.flush();
                        out2.write((col + "\n").getBytes());
                        out2.flush();

                        for (int i = 0; i < row; i++) {
                            for (int j = 0; j < col; j++) {
                                int chess = board[i][j];
                                out1.write((chess + "\n").getBytes());
                                out1.flush();
                                out2.write((chess + "\n").getBytes());
                                out2.flush();
                            }
                        }

                        Player player1 = players.get(player1ID);
                        Player player2 = players.get(player2ID);
                        Thread gameThread = new Thread(new GameService(players, matchingClientMap, pickingClientMap,
                                player1, player2, playerSocket1, playerSocket2, board));
                        gameThread.start();
                    }
                } catch (Exception e) {
                    try {
                        //assume player2 lost
                        OutputStream out1 = playerSocket1.getOutputStream();
                        out1.write("LOST THE OTHER PARTY\n".getBytes());
                        out1.flush();
                        //confirm player2 lost
                        players.get(player2ID).setLoggedOut();
                        //put player1 back to waiting queue
                        matchingClientMap.put(player1ID, playerSocket1);
                    } catch (IOException ex) {
                        try {
                            //player1 lost
                            players.get(player1ID).setLoggedOut();
                            OutputStream out2 = playerSocket2.getOutputStream();
                            out2.write("LOST THE OTHER PARTY\n".getBytes());
                            out2.flush();
                            //put player2 back to waiting queue
                            matchingClientMap.put(player2ID, playerSocket2);
                        } catch (IOException exception) {
                            //both players are lost
                            players.get(player2ID).setLoggedOut();
                        }
                    }
                }
            }
        }
    }

    private int[][] generateBoard(int row, int col) {
        int totalCells = row * col;
        Random random = new Random();
        int[][] board = new int[row][col];

        List<Integer> pool = new ArrayList<>();
        while (pool.size() < totalCells) {
            if (pool.size() == totalCells - 1) {
                pool.add(random.nextInt(12));
            } else {
                int chess = random.nextInt(12);
                pool.add(chess);
                pool.add(chess);
            }
        }
        Collections.shuffle(pool);

        Iterator<Integer> boardIterator = pool.iterator();
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                board[i][j] = boardIterator.next();
            }
        }
        return board;
    }
}
