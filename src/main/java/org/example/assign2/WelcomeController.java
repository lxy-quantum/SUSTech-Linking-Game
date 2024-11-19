package org.example.assign2;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

import static org.example.assign2.Game.setUpBoard;

public class WelcomeController {
    @FXML
    public Button rowButton;

    @FXML
    public Button colButton;

    @FXML
    public Button startButton;

    @FXML
    public void handleStart(ActionEvent event) throws IOException {
        int[] size = getBoardSizeFromUser();

        GameController.game = new Game(setUpBoard(size[0], size[1]));

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("board.fxml"));
        Scene gameScene = new Scene(fxmlLoader.load());
        GameController gameController = fxmlLoader.getController();
        gameController.createGameBoard();

        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(gameScene);
        stage.setTitle("Game");
    }

    // let user choose board size
    private int[] getBoardSizeFromUser() {
        // TODO: let user choose board size

        return new int[]{4, 4};
    }
}
