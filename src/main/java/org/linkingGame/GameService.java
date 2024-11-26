package org.linkingGame;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameService implements Runnable {
    private final ConcurrentHashMap<String, Player> players;
    private final ConcurrentMap<String, Socket> matchingClientMap;
    private final ConcurrentMap<String, Socket> pickingClientMap;

    private static final Logger logger = Logger.getLogger(GameService.class.getName());

    private final Player player1, player2;
    private final Socket playerSocket1, playerSocket2;
    private Game game;
    private final int[] position = new int[2];
    private int score1 = 0, score2 = 0;

    public GameService(ConcurrentHashMap<String, Player> players, ConcurrentMap<String, Socket> matchingClientMap,
                       ConcurrentMap<String, Socket> pickingClientMap, Player player1, Player player2,
                       Socket playerSocket1, Socket playerSocket2, int[][] board) {
        this.players = players;
        this.matchingClientMap = matchingClientMap;
        this.pickingClientMap = pickingClientMap;

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
                if (!game.hasAnyLinkingPairs()) {
                    if (score1 > score2) {
                        player1.addGameRecord(player2.ID, true, false, score1, score2);
                        player2.addGameRecord(player1.ID, false, false, score2, score1);

                        out1.println("game ended");
                        out1.println("you won");

                        out2.println("game ended");
                        out2.println("you lost");

                        out1.println(score1);
                        out1.println(score2);

                        out2.println(score2);
                        out2.println(score1);
                    }
                    else if (score1 < score2) {
                        player1.addGameRecord(player2.ID, false, false, score1, score2);
                        player2.addGameRecord(player1.ID, true, false, score2, score1);

                        out1.println("game ended");
                        out1.println("you lost");

                        out2.println("game ended");
                        out2.println("you won");

                        out1.println(score1);
                        out1.println(score2);

                        out2.println(score2);
                        out2.println(score1);
                    }
                    else {
                        player1.addGameRecord(player2.ID, false, true, score1, score2);
                        player2.addGameRecord(player1.ID, false, true, score2, score1);

                        out1.println("game ended");
                        out1.println("tie");

                        out2.println("game ended");
                        out2.println("tie");

                        out1.println(score1);

                        out2.println(score2);
                    }
                    //switch to beginning service
                    BeginningService service1 = new BeginningService(playerSocket1, players, matchingClientMap, pickingClientMap);
                    service1.setClientId(player1.ID);
                    BeginningService service2 = new BeginningService(playerSocket2, players, matchingClientMap, pickingClientMap);
                    service2.setClientId(player2.ID);
                    new Thread(service1).start();
                    new Thread(service2).start();
                    //save the players game record
                    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("players.ser"))) {
                        oos.writeObject(players);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                if (player1Turn) {
                    out1.println("game continues");
//                    if (!in1.hasNext()) return;
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
                                ArrayList<Tuple> tuples = linkingResult.tuples;
                                out2.println(tuples.size());
                                for (Tuple tuple : tuples) {
                                    out2.println(tuple.row);
                                    out2.println(tuple.col);
                                }
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
                    out2.println("game continues");
//                    if (!in2.hasNext()) return;
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
        } catch (Exception e) {
            try {
                //assume player2 lost
                OutputStream out1 = playerSocket1.getOutputStream();
                out1.write("LOST RIVAL\n".getBytes());
                out1.flush();
                //confirm player2 lost
                player2.setLoggedOut();
                //save the logged-out info
                try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("players.ser"))) {
                    oos.writeObject(players);
                } catch (IOException ioe) {
                    e.printStackTrace();
                }
                //switch to beginning service for player1
                BeginningService service = new BeginningService(playerSocket1, players, matchingClientMap, pickingClientMap);
                service.setClientId(player1.ID);
                new Thread(service).start();
            } catch (IOException ex) {
                try {
                    //lost player1
                    player1.setLoggedOut();
                    //save the logged-out info
                    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("players.ser"))) {
                        oos.writeObject(players);
                    } catch (IOException ioe) {
                        e.printStackTrace();
                    }
                    OutputStream out2 = playerSocket2.getOutputStream();
                    out2.write("LOST RIVAL\n".getBytes());
                    out2.flush();
                    //switch to beginning service for player2
                    BeginningService service = new BeginningService(playerSocket2, players, matchingClientMap, pickingClientMap);
                    service.setClientId(player2.ID);
                    new Thread(service).start();
                } catch (IOException exception) {
                    //both clients are lost
                    player2.setLoggedOut();
                    //save the logged-out info
                    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("players.ser"))) {
                        oos.writeObject(players);
                    } catch (IOException ioe) {
                        e.printStackTrace();
                    }
                    //log the exception
                    logger.log(Level.SEVERE, "both clients are lost from connection: ", exception);
                }
            }
        }
    }
}
