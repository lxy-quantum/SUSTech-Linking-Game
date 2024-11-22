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
    public Button startButton;

    @FXML
    public VBox rowBox;
    @FXML
    public TextField rowField;

    @FXML
    public VBox colBox;
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

    Socket clientSocket;

    public void setClientSocket(Socket socket) {
        this.clientSocket = socket;
    }

    @FXML
    public void gotoRegister(ActionEvent event) {
        hideNode(registerButton);
        hideNode(loginButton);
        showNode(regInfoBox);
    }

    @FXML
    public void handleRegister(ActionEvent event) throws IOException {
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
            if (response.equals("200 OK")) {
                Button button = new Button("Registered!");
                button.setOnAction(e -> {
                    root.getChildren().remove(button);
                    hideNode(regInfoBox);
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
    public void gotoLogin(ActionEvent event) {
        hideNode(registerButton);
        hideNode(loginButton);
        showNode(regInfoBox);
    }

    @FXML
    public void handleLogin(ActionEvent event) {

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