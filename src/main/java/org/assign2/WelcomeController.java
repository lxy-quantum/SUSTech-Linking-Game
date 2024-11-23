package org.assign2;

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
            out.println("REGISTER " + id + " " + password);
            String response = in.readLine();
            if (response.equals("200 OK registered")) {
                hideNode(regInfoBox);
                Button button = new Button("registered");
                button.setOnAction(e -> {
                    root.getChildren().remove(button);
                    showNode(registerButton);
                    showNode(loginButton);
                });
                root.getChildren().add(button);
            } else {
                Button button = new Button("Account ID already exists!");
                button.setOnAction(e -> root.getChildren().remove(button));
                root.getChildren().add(button);
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
            out.println("LOGIN " + id + " " + password);
            String response = in.readLine();
            if (response.equals("200 OK logged in")) {
                hideNode(loginInfoBox);
                Button button = new Button("successful");
                button.setOnAction(e -> {
                    root.getChildren().remove(button);
                    showNode(matchOrPickBox);
                });
                root.getChildren().add(button);

            } else {
                Button button = new Button("Account ID already exists!");
                button.setOnAction(e -> root.getChildren().remove(button));
                root.getChildren().add(button);
            }
        }
    }

    @FXML
    public void gotoMatching() throws IOException {
        hideNode(matchOrPickBox);
        matching();
    }

    public void matching() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        out.println("MATCH");
        String response = in.readLine();
        if (response.equals("200 OK matching")) {
            showNode(waitingBox);
            String matchingResult = in.readLine();
            if (matchingResult.equals("200 OK matched")) {
                String matchedID = in.readLine();
                String rowColResponse = in.readLine();
                hideNode(waitingBox);
                Button button = new Button("Matched with " + matchedID);
                button.setOnAction(e -> {
                    root.getChildren().remove(button);
                    //start picking row and cols
                    if (rowColResponse.equals("choose row and column")) {
                        showNode(rowColBox);
                    } else if (rowColResponse.equals("no need to choose")) {
                        showNode(waitingForBoardBox);
                        try {
                            String rowColResult = in.readLine();
                            if (rowColResult.equals("board settled")) {
                                hideNode(waitingForBoardBox);
                                //receive the board
                            }
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    } else {
                        System.out.println("error!");
                    }
                });
                root.getChildren().add(button);
            }
        }
    }

    @FXML
    public void gotoPicking() {
        hideNode(matchOrPickBox);
    }

    @FXML
    public void handleStart(ActionEvent event) throws IOException {
        int[] size = getBoardSizeFromUser();

        GameController.game = new Game(Game.setUpBoard(size[0], size[1]));

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("board.fxml"));
        Scene gameScene = new Scene(fxmlLoader.load());

        GameController gameController = fxmlLoader.getController();
        gameController.setClientSocket(clientSocket);
        gameController.createGameBoard();

        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(gameScene);
        stage.setTitle("Game");
    }

    @FXML
    public void backToBeginning() {
        hideNode(regInfoBox);
        hideNode(loginInfoBox);
        showNode(registerButton);
        showNode(loginButton);
    }

    // let user choose board size
    private int[] getBoardSizeFromUser() {
        int row, col;
        try {
            row = Integer.parseInt(rowField.getText());
            col = Integer.parseInt(colField.getText());
        } catch (NumberFormatException e) {
            row = 5;
            col = 5;
        }
        return new int[]{row, col};
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

class MessageSender implements Runnable {
//    private final PrintWriter out;

    @Override
    public void run() {

    }
}