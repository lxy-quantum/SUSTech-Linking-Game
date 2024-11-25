package org.linkingGame;

import javafx.animation.PauseTransition;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GameService implements Runnable {
    private final String player1, player2;
    private final Socket playerSocket1, playerSocket2;
    private Game game;
    private final int[] position = new int[2];
    private int score1 = 0, score2 = 0;

    public GameService(String player1, String player2, Socket playerSocket1, Socket playerSocket2, int[][] board) {
        this.player1 = player1;
        this.player2 = player2;
        this.playerSocket1 = playerSocket1;
        this.playerSocket2 = playerSocket2;
        this.game = new Game(board);
    }

    @Override
    public void run() {
        try {
            Scanner in1 = new Scanner(playerSocket1.getInputStream());
            Scanner in2 = new Scanner(playerSocket2.getInputStream());
            PrintStream out1 = new PrintStream(playerSocket1.getOutputStream(), true);
            PrintStream out2 = new PrintStream(playerSocket2.getOutputStream(), true);

            boolean player1Turn = false;
            while (true) {
                if (player1Turn) {
                    if (!in1.hasNext()) return;
                    String command = in1.next();
                    switch (command) {
                        case "FIRST": {
                            int row = in1.nextInt();
                            int col = in1.nextInt();
                            position[0] = row;
                            position[1] = col;
                            out2.println("the rival chose first chess");
                            out2.println(row);
                            out2.println(col);
                            break;
                        }
                        case "SECOND": {
                            int row = in1.nextInt();
                            int col = in1.nextInt();
                            LinkingResult linkingResult = game.judge(position[0], position[1], row, col);
                            boolean change = linkingResult.success;
                            if (change) {
                                game.board[position[0]][position[1]] = 0;
                                game.board[row][col] = 0;
                                score1++;
                                out2.println("the rival linked successfully");
                                out2.println(row);
                                out2.println(col);
                                //TODO: lines
                            }
                            else {
                                out2.println("the rival failed");
                                out2.println(row);
                                out2.println(col);
                            }
                            player1Turn = false;
                            break;
                        }
                    }
                }
                else {
                    if (!in2.hasNext()) return;
                    String command = in2.next();
                    switch (command) {
                        case "FIRST": {
                            int row = in2.nextInt();
                            int col = in2.nextInt();
                            position[0] = row;
                            position[1] = col;
                            out1.println("the rival chose first chess");
                            out1.println(row);
                            out1.println(col);
                            break;
                        }
                        case "SECOND": {
                            int row = in2.nextInt();
                            int col = in2.nextInt();
                            LinkingResult linkingResult = game.judge(position[0], position[1], row, col);
                            boolean change = linkingResult.success;
                            if (change) {
                                game.board[position[0]][position[1]] = 0;
                                game.board[row][col] = 0;
                                score2++;
                                out1.println("the rival linked successfully");
                                out1.println(row);
                                out1.println(col);
                                ArrayList<Tuple> tuples = linkingResult.tuples;
                                out1.println(tuples.size());
                                for (Tuple tuple : tuples) {
                                    out1.println(tuple.row);
                                    out1.println(tuple.col);
                                }
                            }
                            else {
                                out1.println("the rival failed");
                                out1.println(row);
                                out1.println(col);
                            }
                            player1Turn = true;
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
