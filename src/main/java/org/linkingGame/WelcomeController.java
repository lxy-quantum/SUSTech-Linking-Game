package org.linkingGame;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

public class WelcomeController {
    @FXML
    public AnchorPane root;
    @FXML
    public Label title;

    @FXML
    public Button registerButton;
    @FXML
    public Button loginButton;

    @FXML
    public VBox rowColBox;
    @FXML
    public TextField rowField;
    @FXML
    public TextField colField;

    @FXML
    public VBox regInfoBox;
    @FXML
    public TextField regIdField;
    @FXML
    public TextField regPwdField;

    @FXML
    public VBox loginInfoBox;
    @FXML
    public TextField loginIdField;
    @FXML
    public TextField loginPwdField;

    @FXML
    public VBox matchOrPickBox;
    @FXML
    public VBox waitingBox;
    @FXML
    public VBox waitingForBoardBox;

    Socket clientSocket;

    public void setClientSocket(Socket socket) {
        this.clientSocket = socket;
    }

    @FXML
    public void gotoRegister() {
        hideNode(registerButton);
        hideNode(loginButton);
        showNode(regInfoBox);
    }

    @FXML
    public void handleRegister() {
        String id = regIdField.getText();
        String password = regPwdField.getText();
        if (id.isEmpty() || password.isEmpty()) {
            Button button = new Button("ID and password mustn't be empty!");
            button.setOnAction(e -> root.getChildren().remove(button));
            root.getChildren().add(button);
        } else {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                registerTask(in, out, id, password);
            } catch (IOException e) {
                dealWithConnLoss();
            }
        }
    }

    @FXML
    public void gotoLogin() {
        hideNode(registerButton);
        hideNode(loginButton);
        showNode(loginInfoBox);
    }

    @FXML
    public void handleLogin() {
        String id = loginIdField.getText();
        String password = loginPwdField.getText();
        if (id.isEmpty() || password.isEmpty()) {
            Button button = new Button("ID and password mustn't be empty!");
            button.setOnAction(e -> root.getChildren().remove(button));
            root.getChildren().add(button);
        } else {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                loginTask(in, out, id, password);
            } catch (IOException e) {
                dealWithConnLoss();
            }
        }
    }

    @FXML
    public void gotoMatching() {
        hideNode(matchOrPickBox);
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            out.println("MATCH");
            waitForMatching(in);
        } catch (IOException e) {
            dealWithConnLoss();
        }
    }

    private void registerTask(BufferedReader in, PrintWriter out, String id, String password) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                String response = "";
                try {
                    out.println("REGISTER " + id + " " + password);
                    response = in.readLine();
                } catch (IOException e) {
                    dealWithConnLoss();
                }
                if (response.equals("200 OK registered")) {
                    Platform.runLater(() -> {
                        hideNode(regInfoBox);
                        Button button = new Button("registered successfully");
                        button.setOnAction(e -> {
                            root.getChildren().remove(button);
                            showNode(registerButton);
                            showNode(loginButton);
                        });
                        root.getChildren().add(button);
                    });
                } else {
                    String finalResponse = response;
                    Platform.runLater(() -> {
                        Button button = new Button(finalResponse);
                        button.setOnAction(e -> root.getChildren().remove(button));
                        root.getChildren().add(button);
                    });
                }
                return null;
            }
        };

        new Thread(task).start();
    }

    private void loginTask(BufferedReader in, PrintWriter out, String id, String password) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                String response = "";
                try {
                    out.println("LOGIN " + id + " " + password);
                    response = in.readLine();
                } catch (IOException e) {
                    dealWithConnLoss();
                }
                if (response.equals("200 OK logged in")) {
                    Platform.runLater(() -> {
                        hideNode(loginInfoBox);
                        Button button = new Button("successful");
                        button.setOnAction(e -> {
                            root.getChildren().remove(button);
                            showNode(matchOrPickBox);
                        });
                        root.getChildren().add(button);
                    });
                } else {
                    String finalResponse = response;
                    Platform.runLater(() -> {
                        Button button = new Button(finalResponse);
                        button.setOnAction(e -> root.getChildren().remove(button));
                        root.getChildren().add(button);
                    });
                }
                return null;
            }
        };

        new Thread(task).start();
    }

    private void waitForMatching(BufferedReader in) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                try {
                    Platform.runLater(() -> showNode(waitingBox));
                    String matchingResult = in.readLine();
                    while (matchingResult.equals("LOST THE OTHER PARTY")) {
                        matchingResult = in.readLine();
                    }
                    if (matchingResult.equals("200 OK matched")) {
                        handleMatched(in);
                    }
                } catch (IOException e) {
                    dealWithConnLoss();
                }
                return null;
            }
        };

        new Thread(task).start();
    }

    private void handleMatched(BufferedReader in) throws IOException {
        String matchedID = in.readLine();
        while (matchedID.equals("LOST THE OTHER PARTY")) {
            matchedID = in.readLine();
        }
        if (matchedID.equals("200 OK matched")) {
            handleMatched(in);
            return;
        }

        String rowColResponse = in.readLine();
        while (rowColResponse.equals("LOST THE OTHER PARTY")) {
            rowColResponse = in.readLine();
        }
        if (rowColResponse.equals("200 OK matched")) {
            handleMatched(in);
            return;
        }

        String finalMatchedID = matchedID;
        String finalRowColResponse = rowColResponse;
        Platform.runLater(() -> {
            hideNode(waitingBox);
            Button button = new Button("Matched with " + finalMatchedID);
            button.setOnAction(e -> {
                root.getChildren().remove(button);
                if (finalRowColResponse.equals("choose row and column")) {
                    showNode(rowColBox);
                } else if (finalRowColResponse.equals("no need to choose")) {
                    showNode(waitingForBoardBox);
                    waitForBoard(in, true);
                } else {
                    System.out.println("error!");
                }
            });
            root.getChildren().add(button);
        });
    }

    private void waitForBoard(BufferedReader in, boolean myTurn) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                try {
                    String boardReadyResult = in.readLine();
                    if (boardReadyResult.equals("LOST THE OTHER PARTY")) {
                        handleMatchedRivalLost(in, boardReadyResult);
                        return null;
                    }
                    if (boardReadyResult.equals("board settled")) {
                        Platform.runLater(() -> hideNode(waitingForBoardBox));
                        //receive the board
                        String rowStr = in.readLine();
                        if (rowStr.equals("LOST THE OTHER PARTY")) {
                            handleMatchedRivalLost(in, rowStr);
                            return null;
                        }
                        int row = Integer.parseInt(rowStr);

                        String colStr = in.readLine();
                        if (colStr.equals("LOST THE OTHER PARTY")) {
                            handleMatchedRivalLost(in, colStr);
                            return null;
                        }
                        int col = Integer.parseInt(colStr);

                        int[][] gameBoard = new int[row][col];
                        for (int i = 0; i < row; i++) {
                            for (int j = 0; j < col; j++) {
                                String chessStr = in.readLine();
                                if (chessStr.equals("LOST THE OTHER PARTY")) {
                                    handleMatchedRivalLost(in, chessStr);
                                    return null;
                                }
                                gameBoard[i][j] = Integer.parseInt(chessStr);
                            }
                        }

                        Platform.runLater(() -> {
                            GameController.game = new Game(gameBoard);
                            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("board.fxml"));
                            Scene gameScene;
                            try {
                                gameScene = new Scene(fxmlLoader.load());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                            GameController gameController = fxmlLoader.getController();
                            gameController.myTurn = myTurn;
                            if (!myTurn) {
                                gameController.roundReminderLabel.setText("Rival's Round");
                                gameController.rivalPlaying();
                            } else {
                                gameController.roundReminderLabel.setText("Your Round");
                            }
                            try {
                                gameController.setClientSocket(clientSocket);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            gameController.createGameBoard();

                            Stage stage = (Stage) root.getScene().getWindow();
                            stage.setTitle("Game");
                            stage.setScene(gameScene);
                        });
                    }
                } catch (IOException e) {
                    dealWithConnLoss();
                }
                return null;
            }
        };

        new Thread(task).start();
    }

    private void handleMatchedRivalLost(BufferedReader in, String response) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws IOException {
                Platform.runLater(() -> {
                    hideNode(rowColBox);
                    hideNode(waitingForBoardBox);
                    Button button = new Button("The other party quit. Start matching again.");
                    button.setOnAction(e -> {
                        root.getChildren().remove(button);
                        showNode(waitingBox);
                    });
                    root.getChildren().add(button);
                });

                String finalResponse = response;
                while (finalResponse.equals("LOST THE OTHER PARTY")) {
                    finalResponse = in.readLine();
                }
                if (finalResponse.equals("200 OK matched")) {
                    handleMatched(in);
                }
                return null;
            }
        };

        new Thread(task).start();
    }

    @FXML
    public void gotoPicking() {
        hideNode(matchOrPickBox);

    }

    @FXML
    public void gotoRecords() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    out.println("GET_RECORD");
                    StringBuilder recordsSb = new StringBuilder();
                    int recordsNum = Integer.parseInt(in.readLine());
                    for (int i = 0; i < recordsNum; i++) {
                        String rivalName = in.readLine();
                        recordsSb.append("Game with: ");
                        recordsSb.append(rivalName);
                        recordsSb.append(", Result: ");
                        recordsSb.append(in.readLine());
                        recordsSb.append(", Your score: ");
                        recordsSb.append(in.readLine());
                        recordsSb.append(", ").append(rivalName).append("'s score: ");
                        recordsSb.append(in.readLine()).append("\n");
                    }
                    String records = recordsSb.toString();
                    Platform.runLater(() -> addPopup(root, records));
                } catch (IOException e) {
                    dealWithConnLoss();
                }
                return null;
            }
        };
        new Thread(task).start();
    }

    @FXML
    public void goBackToGame(ActionEvent event) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    out.println("BACK");
                    String response = in.readLine();
                    if (response.equals("nothing")) {
                        Platform.runLater(() -> addPopup(root, "No games to go back"));
                    } else if (response.equals("back")) {
                        boolean myTurn = false;
                        String turn = in.readLine();
                        if (turn.equals("my turn")) {
                            myTurn = true;
                        }
                        waitForBoard(in, myTurn);
                    }
                } catch (IOException e) {
                    dealWithConnLoss();
                }
                return null;
            }
        };
        new Thread(task).start();
    }

    @FXML
    public void handleStart() {
        int row, col;
        try {
            row = Integer.parseInt(rowField.getText());
            col = Integer.parseInt(colField.getText());
            if (row < 3 || col < 3) {
                Button button = new Button("size too small");
                button.setOnAction(e -> root.getChildren().remove(button));
                root.getChildren().add(button);
                return;
            }
        } catch (NumberFormatException e) {
            Button button = new Button("invalid row or column");
            button.setOnAction(event -> root.getChildren().remove(button));
            root.getChildren().add(button);
            return;
        }

        int finalRow = row;
        int finalCol = col;
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                    out.println("SET_SIZE " + finalRow + " " + finalCol);

                    waitForBoard(in, false);
                } catch (IOException e) {
                    dealWithConnLoss();
                }
                return null;
            }
        };
        new Thread(task).start();
    }

    @FXML
    public void backToBeginning() {
        hideNode(regInfoBox);
        hideNode(loginInfoBox);
        showNode(registerButton);
        showNode(loginButton);
    }

    public void hideNode(Node node) {
        node.setVisible(false);
        node.setManaged(false);
    }

    public void showNode(Node node) {
        node.setVisible(true);
        node.setManaged(true);
    }

    public void addPopup(AnchorPane root, String info) {
        VBox popupBox = new VBox(10);
        popupBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #ff8c00; -fx-border-width: 2px; -fx-padding: 10;");
        popupBox.setAlignment(Pos.CENTER);
        popupBox.setPrefSize(20, 14);

        Text popupText = new Text(info);
        Button closeButton = new Button("close");
        closeButton.setOnAction(event -> root.getChildren().remove(popupBox));
        popupBox.getChildren().addAll(popupText, closeButton);

        root.getChildren().add(popupBox);
    }

    private void dealWithConnLoss() {
        Platform.runLater(() -> {
            root.getChildren().clear();
            Button button = new Button("Connection lost");
            button.setOnAction(e -> ((Stage) root.getScene().getWindow()).close());
            root.getChildren().add(button);
        });
    }
}