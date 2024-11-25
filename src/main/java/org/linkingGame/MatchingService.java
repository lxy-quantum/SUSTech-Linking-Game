package org.linkingGame;

import java.io.IOException;
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

                int[][] board = null;
                try {
                    PrintWriter out1 = new PrintWriter(playerSocket1.getOutputStream(), true);
                    PrintWriter out2 = new PrintWriter(playerSocket2.getOutputStream(), true);
                    out1.println("200 OK matched");
                    out2.println("200 OK matched");
                    out1.println(player2ID);
                    out2.println(player1ID);
                    out1.println("choose row and column");
                    out2.println("no need to choose");

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

                        out1.println("board settled");
                        out1.println(row);
                        out1.println(col);
                        out2.println("board settled");
                        out2.println(row);
                        out2.println(col);

                        for (int i = 0; i < row; i++) {
                            for (int j = 0; j < col; j++) {
                                int chess = board[i][j];
                                out1.println(chess);
                                out2.println(chess);
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                Player player1 = players.get(player1ID);
                Player player2 = players.get(player2ID);
                Thread gameThread = new Thread(new GameService(players, matchingClientMap, pickingClientMap,
                        player1, player2, playerSocket1, playerSocket2, board));
                gameThread.start();
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
