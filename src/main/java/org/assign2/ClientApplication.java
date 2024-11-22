package org.assign2;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;

public class ClientApplication extends javafx.application.Application {
    private Socket clientSocket;

    @Override
    public void start(Stage stage) throws IOException {
        clientSocket = new Socket("localhost", 1234);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("welcome.fxml"));
        AnchorPane root = fxmlLoader.load();

        WelcomeController welcomeController = fxmlLoader.getController();
        welcomeController.setClientSocket(clientSocket);

        Scene scene = new Scene(root);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();

        // TODO: handle the game logic

    }

    public static void main(String[] args) {
        try {
            Socket clientSocket = new Socket("localhost", 1234);
            System.out.println("Connected to server");

            launch();
        } catch (IOException e) {
            System.out.println("Disconnected from server");
        }
    }
}