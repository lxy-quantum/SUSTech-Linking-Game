package org.linkingGame;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;

public class ClientApplication extends javafx.application.Application {

    @Override
    public void start(Stage stage) {
        try {
            Socket clientSocket = new Socket("localhost", 1234);

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("welcome.fxml"));
            AnchorPane root = fxmlLoader.load();

            WelcomeController welcomeController = fxmlLoader.getController();
            welcomeController.setClientSocket(clientSocket);

            Scene scene = new Scene(root);
            stage.setTitle("Hello!");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.out.println("Disconnected from server");
            //
        }

        // TODO: handle the game logic

    }

    public static void main(String[] args) {
        launch();
    }
}