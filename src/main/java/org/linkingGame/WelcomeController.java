package org.linkingGame;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
    public void handleRegister() throws IOException {
        String id = regIdField.getText();
        String password = regPwdField.getText();
        if (id.isEmpty() || password.isEmpty()) {
            Button button = new Button("ID and password mustn't be empty!");
            button.setOnAction(e -> root.getChildren().remove(button));
            root.getChildren().add(button);
        } else {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            registerTask(in, out, id, password);
        }
    }

    @FXML
    public void gotoLogin() {
        hideNode(registerButton);
        hideNode(loginButton);
        showNode(loginInfoBox);
    }

    @FXML
    public void handleLogin() throws IOException {
        String id = loginIdField.getText();
        String password = loginPwdField.getText();
        if (id.isEmpty() || password.isEmpty()) {
            Button button = new Button("ID and password mustn't be empty!");
            button.setOnAction(e -> root.getChildren().remove(button));
            root.getChildren().add(button);
        } else {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            loginTask(in, out, id, password);
        }
    }

    @FXML
    public void gotoMatching() throws IOException {
        hideNode(matchOrPickBox);

        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        out.println("MATCH");

        waitForMatching(in, out);
    }

    private void registerTask(BufferedReader in, PrintWriter out, String id, String password) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                out.println("REGISTER " + id + " " + password);
                String response = in.readLine();
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
                    Platform.runLater(() -> {
                        Button button = new Button(response);
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
            protected Void call() throws Exception {
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                out.println("LOGIN " + id + " " + password);
                String response = in.readLine();
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
                    //TODO
                    Platform.runLater(() -> {
                        Button button = new Button("Account ID already exists!");
                        button.setOnAction(e -> root.getChildren().remove(button));
                        root.getChildren().add(button);
                    });
                }
                return null;
            }
        };

        new Thread(task).start();
    }

    private void waitForMatching(BufferedReader in, PrintWriter out) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws IOException {
                String response = in.readLine();
                if (response.equals("200 OK matching")) {
                    Platform.runLater(() -> showNode(waitingBox));
                    String matchingResult = in.readLine();
                    if (matchingResult.equals("200 OK matched")) {
                        String matchedID = in.readLine();
                        String rowColResponse = in.readLine();
                        Platform.runLater(() -> {
                            hideNode(waitingBox);
                            Button button = new Button("Matched with " + matchedID);
                            button.setOnAction(e -> {
                                root.getChildren().remove(button);
                                if (rowColResponse.equals("choose row and column")) {
                                    showNode(rowColBox);
                                } else if (rowColResponse.equals("no need to choose")) {
                                    showNode(waitingForBoardBox);
                                    waitForBoard(in, true);
                                } else {
                                    System.out.println("error!");
                                }
                            });
                            root.getChildren().add(button);
                        });
                    }
                }
                return null;
            }
        };

        new Thread(task).start();
    }

    private void waitForBoard(BufferedReader in, boolean myTurn) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws IOException {
                String boardReadyResult = in.readLine();
                if (boardReadyResult.equals("board settled")) {
                    Platform.runLater(() -> hideNode(waitingForBoardBox));
                    //receive the board
                    int row = Integer.parseInt(in.readLine());
                    int col = Integer.parseInt(in.readLine());
                    int[][] gameBoard = new int[row][col];
                    for (int i = 0; i < row; i++) {
                        for (int j = 0; j < col; j++) {
                            gameBoard[i][j] = Integer.parseInt(in.readLine());
                        }
                    }
                    Platform.runLater(() -> {
                        GameController.game = new Game(gameBoard);
                        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("board.fxml"));
                        Scene gameScene;
                        try {
                            gameScene = new Scene(fxmlLoader.load());
                        } catch (IOException e) {
                            //TODO: exception?
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
                            //TODO: exception?
                            throw new RuntimeException(e);
                        }
                        gameController.createGameBoard();

                        Stage stage = (Stage) root.getScene().getWindow();
                        stage.setTitle("Game");
                        stage.setScene(gameScene);
                    });
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
    public void handleStart() throws IOException {
        int row, col;
        try {
            row = Integer.parseInt(rowField.getText());
            col = Integer.parseInt(colField.getText());
        } catch (NumberFormatException e) {
            row = 5;
            col = 5;
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

        out.println("SET_SIZE " + row + " " + col);

        waitForBoard(in, false);
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
}