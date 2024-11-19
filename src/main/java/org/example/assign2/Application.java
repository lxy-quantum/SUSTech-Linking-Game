package org.example.assign2;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

import static org.example.assign2.Game.setUpBoard;

public class Application extends javafx.application.Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("welcome.fxml"));
        AnchorPane root = fxmlLoader.load();

        Scene scene = new Scene(root);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();

        // TODO: handle the game logic

    }

    // let user choose board size
    private int[] getBoardSizeFromUser() {
        // TODO: let user choose board size

        return new int[]{4, 4};
    }

    public static void main(String[] args) {
        launch();
    }
}