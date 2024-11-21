package org.assign2;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class WelcomeController {
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
    public void handleStart(ActionEvent event) throws IOException {
        int[] size = getBoardSizeFromUser();

        GameController.game = new Game(Game.setUpBoard(size[0], size[1]));

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
        int row = Integer.parseInt(rowField.getText());
        int col = Integer.parseInt(colField.getText());
        return new int[]{row, col};
    }

}