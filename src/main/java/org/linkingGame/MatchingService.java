package org.linkingGame;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;
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

                int[][] board = null;
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

                Thread gameThread = new Thread(new GameService(player1, player2, playerSocket1, playerSocket2, board));
                gameThread.start();
            }
        }
    }

    private int[][] generateBoard(int row, int col) {
        int totalCells = row * col;
        Random random = new Random();
        int[][] board = new int[row][col];

        List<Integer> pool = new ArrayList<>();
        int maxNum = 12;
        for (int i = 0; i < maxNum; i++) {
            int pairCount = totalCells / maxNum;
            for (int j = 0; j < pairCount; j++) {
                pool.add(i);
                pool.add(i);
            }
        }
        if (totalCells % 2 != 0) {
            pool.add(random.nextInt(maxNum));
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
