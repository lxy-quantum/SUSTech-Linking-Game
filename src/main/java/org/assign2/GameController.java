package org.assign2;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import java.util.Objects;

public class GameController {

    @FXML
    public Button welcomeButton;

    @FXML
    private Label scoreLabel;

    @FXML
    public Button resetButton;

    @FXML
    private GridPane gameBoard;

    public static Game game;

    int[] position = new int[3];
    Button lastButton;

    int score = 0;

    @FXML
    public void initialize() {

    }

    public void createGameBoard() {
        gameBoard.getChildren().clear();

        for (int row = 0; row < game.row; row++) {
            for (int col = 0; col < game.col; col++) {
                Button button = new Button();
                button.setPrefSize(40, 40);
                ImageView imageView = addContent(game.board[row][col]);
                imageView.setFitWidth(30);
                imageView.setFitHeight(30);
                imageView.setPreserveRatio(true);
                button.setGraphic(imageView);
                int finalRow = row;
                int finalCol = col;
                button.setOnAction( _ -> handleButtonPress(button, finalRow, finalCol));
                gameBoard.add(button, col, row);
            }
        }
    }

    private void handleButtonPress(Button button, int row, int col) {
        System.out.println("Button pressed at: " + row + ", " + col);
        if (position[0] == 0) {
            position[1] = row;
            position[2] = col;
            position[0] = 1;
            lastButton = button;
        } else {
            boolean change = game.judge(position[1], position[2], row, col);
            position[0] = 0;
            if (change) {
                //handle the grid deletion logic
                game.board[position[1]][position[2]] = 0;
                game.board[row][col] = 0;
                deleteGrid(button, row, col);
                deleteGrid(lastButton, position[1], position[2]);
                score++;
                scoreLabel.setText(Integer.toString(score));
            }
        }
    }

    @FXML
    private void handleReset() {
        System.out.println("Reset");
        score = 0;
        scoreLabel.setText("0");
        game.board = Game.setUpBoard(game.row, game.col);
        createGameBoard();
    }

    public void deleteGrid(Button button, int row, int col) {
        ((GridPane) button.getParent()).getChildren().remove(button);
        Button newButton = new Button();
        newButton.setPrefSize(40, 40);
        ImageView imageView = new ImageView(imageCarambola);
        imageView.setFitWidth(30);
        imageView.setFitHeight(30);
        imageView.setPreserveRatio(true);
        newButton.setGraphic(imageView);
        newButton.setOnAction( _ -> handleButtonPress(button, row, col));
        gameBoard.add(newButton, col, row);
    }

    public ImageView addContent(int content){
        return switch (content) {
            case 0 -> new ImageView(imageCarambola);
            case 1 -> new ImageView(imageApple);
            case 2 -> new ImageView(imageMango);
            case 3 -> new ImageView(imageBlueberry);
            case 4 -> new ImageView(imageCherry);
            case 5 -> new ImageView(imageGrape);
            case 6 -> new ImageView(imageKiwi);
            case 7 -> new ImageView(imageOrange);
            case 8 -> new ImageView(imagePeach);
            case 9 -> new ImageView(imagePear);
            case 10 -> new ImageView(imagePineapple);
            case 11 -> new ImageView(imageWatermelon);
            default -> null;
        };
    }

    public static Image imageApple = new Image(Objects.requireNonNull(Game.class.getResource("/org/assign2/apple.png")).toExternalForm());
    public static Image imageMango = new Image(Objects.requireNonNull(Game.class.getResource("/org/assign2/mango.png")).toExternalForm());
    public static Image imageBlueberry = new Image(Objects.requireNonNull(Game.class.getResource("/org/assign2/blueberry.png")).toExternalForm());
    public static Image imageCherry = new Image(Objects.requireNonNull(Game.class.getResource("/org/assign2/cherry.png")).toExternalForm());
    public static Image imageGrape = new Image(Objects.requireNonNull(Game.class.getResource("/org/assign2/grape.png")).toExternalForm());
    public static Image imageCarambola = new Image(Objects.requireNonNull(Game.class.getResource("/org/assign2/carambola.png")).toExternalForm());
    public static Image imageKiwi = new Image(Objects.requireNonNull(Game.class.getResource("/org/assign2/kiwi.png")).toExternalForm());
    public static Image imageOrange = new Image(Objects.requireNonNull(Game.class.getResource("/org/assign2/orange.png")).toExternalForm());
    public static Image imagePeach = new Image(Objects.requireNonNull(Game.class.getResource("/org/assign2/peach.png")).toExternalForm());
    public static Image imagePear = new Image(Objects.requireNonNull(Game.class.getResource("/org/assign2/pear.png")).toExternalForm());
    public static Image imagePineapple = new Image(Objects.requireNonNull(Game.class.getResource("/org/assign2/pineapple.png")).toExternalForm());
    public static Image imageWatermelon = new Image(Objects.requireNonNull(Game.class.getResource("/org/assign2/watermelon.png")).toExternalForm());

}
