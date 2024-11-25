package org.linkingGame;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Objects;

public class GameController {

    @FXML
    public StackPane gamePane;
    @FXML
    private GridPane gameBoard;

    @FXML
    private Label scoreLabel;

    @FXML
    public Button resetButton;

    public static Game game;

    Socket clientSocket;
    BufferedReader in;
    PrintWriter out;

    Button[][] buttons;
    int[] position = new int[3];
    Button lastButton;

    protected boolean myTurn;
    int score = 0;

    public void setClientSocket(Socket socket) throws IOException {
        this.clientSocket = socket;
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    @FXML
    public void initialize() {

    }

    public void createGameBoard() {
        gameBoard.getChildren().clear();
        buttons = new Button[game.rowSize][game.colSize];

        for (int row = 0; row < game.rowSize; row++) {
            for (int col = 0; col < game.colSize; col++) {
                Button button = new Button();
                button.setMinSize(40, 40);
                button.setPrefSize(40, 40);
                ImageView imageView = addContent(game.board[row][col]);
                imageView.setFitWidth(30);
                imageView.setFitHeight(30);
                imageView.setPreserveRatio(true);
                button.setGraphic(imageView);
                int finalRow = row;
                int finalCol = col;
                button.setOnAction( _ -> handleButtonPress(button, finalRow, finalCol));
                buttons[row][col] = button;
                gameBoard.add(button, col, row);
            }
        }
    }

    private void handleButtonPress(Button button, int row, int col) {
        System.out.println("Button pressed at: " + row + ", " + col);
        if (myTurn) {
            button.setStyle("-fx-border-color: #00dc00; -fx-border-width: 2px;");
            if (position[0] == 0) {
                position[1] = row;
                position[2] = col;
                position[0] = 1;
                lastButton = button;
                sendButtonInfoToServer("FIRST", row, col);
            } else {
                sendButtonInfoToServer("SECOND", row, col);
                LinkingResult linkingResult = game.judge(position[1], position[2], row, col);
                boolean change = linkingResult.success;
                if (change) {
                    //handle the grid deletion logic
                    game.board[position[1]][position[2]] = 0;
                    game.board[row][col] = 0;
                    //TODO: the lines
                    PauseTransition delay = new PauseTransition(Duration.seconds(0.3));
                    delay.setOnFinished(e -> {
                        deleteGrid(button, row, col);
                        deleteGrid(lastButton, position[1], position[2]);
                        score++;
                        scoreLabel.setText(Integer.toString(score));
                    });
                    delay.play();
                }
                else {
                    System.out.println("failed!");
                    PauseTransition delay = new PauseTransition(Duration.seconds(0.1));
                    delay.setOnFinished(e -> {
                        addPopup((StackPane) gameBoard.getParent());
                        lastButton.setStyle("");
                        button.setStyle("");
                    });
                    delay.play();
                }
                position[0] = 0;
                myTurn = false;
                rivalPlaying();
            }
        }
    }

    private void sendButtonInfoToServer(String code, int row, int col) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                out.println(code + " " + row + " " + col);
                return null;
            }
        };

        new Thread(task).start();
    }

    protected void rivalPlaying() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws IOException {
                Button formerButton = null;
                int formerRow = 0, formerCol = 0;
                while (true) {
                    String response = in.readLine();
                    if (response.equals("the rival chose first chess")) {
                        int row = Integer.parseInt(in.readLine());
                        int col = Integer.parseInt(in.readLine());
                        Button button = buttons[row][col];
                        Platform.runLater(() -> button.setStyle("-fx-border-color: #f00000; -fx-border-width: 2px;"));
                        formerButton = button;
                        formerRow = row;
                        formerCol = col;
                    }
                    else if (response.equals("the rival linked successfully")) {
                        int row = Integer.parseInt(in.readLine());
                        int col = Integer.parseInt(in.readLine());
                        game.board[formerRow][formerCol] = 0;
                        game.board[row][col] = 0;
                        Button button = buttons[row][col];
                        Button finalFormerButton = formerButton;
                        int finalFormerRow = formerRow;
                        int finalFormerCol = formerCol;

                        Platform.runLater(() -> {
                            button.setStyle("-fx-border-color: #f00000; -fx-border-width: 2px;");

                            PauseTransition delay = new PauseTransition(Duration.seconds(0.3));
                            delay.setOnFinished(e -> {
                                deleteGrid(button, row, col);
                                deleteGrid(finalFormerButton, finalFormerRow, finalFormerCol);
                                //TODO: change to rival's score
                                //scoreLabel.setText(Integer.toString(score));
                            });
                            delay.play();
                        });
                        myTurn = true;
                        break;
                    }
                    else if (response.equals("the rival failed")) {
                        int row = Integer.parseInt(in.readLine());
                        int col = Integer.parseInt(in.readLine());
                        Button button = buttons[row][col];
                        Button finalFormerButton = formerButton;
                        Platform.runLater(() -> {
                            button.setStyle("-fx-border-color: #f00000; -fx-border-width: 2px;");

                            PauseTransition delay = new PauseTransition(Duration.seconds(0.1));
                            delay.setOnFinished(e -> {
                                finalFormerButton.setStyle("");
                                button.setStyle("");
                            });
                            delay.play();
                        });
                        myTurn = true;
                        break;
                    }
                }
                return null;
            }
        };

        new Thread(task).start();
    }

    @FXML
    private void handleReset() {
        //TODO: update that serverside takes over
        System.out.println("Reset");
        score = 0;
        scoreLabel.setText("0");
        game.board = Game.setUpBoard(game.rowSize, game.colSize);
        createGameBoard();
    }

    public void deleteGrid(Button button, int row, int col) {
        ((GridPane) button.getParent()).getChildren().remove(button);
        Button newButton = new Button();
        newButton.setMinSize(40, 40);
        newButton.setPrefSize(40, 40);
        ImageView imageView = new ImageView(imageCarambola);
        imageView.setFitWidth(30);
        imageView.setFitHeight(30);
        imageView.setPreserveRatio(true);
        newButton.setGraphic(imageView);
        newButton.setOnAction( _ -> handleButtonPress(button, row, col));
        gameBoard.add(newButton, col, row);
    }

    public void addPopup(StackPane gamePane) {
        VBox popupBox = new VBox(10);
        popupBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #ff8c00; -fx-border-width: 2px; -fx-padding: 10;");
        popupBox.setAlignment(Pos.CENTER);
        popupBox.setPrefSize(80, 50);

        Text popupText = new Text("Failed!");
        Button closeButton = new Button("close");
        closeButton.setOnAction(event -> gamePane.getChildren().remove(popupBox));
        popupBox.getChildren().addAll(popupText, closeButton);

        gamePane.getChildren().add(popupBox);
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

    public static Image imageApple = new Image(Objects.requireNonNull(Game.class.getResource("/org/linkingGame/apple.png")).toExternalForm());
    public static Image imageMango = new Image(Objects.requireNonNull(Game.class.getResource("/org/linkingGame/mango.png")).toExternalForm());
    public static Image imageBlueberry = new Image(Objects.requireNonNull(Game.class.getResource("/org/linkingGame/blueberry.png")).toExternalForm());
    public static Image imageCherry = new Image(Objects.requireNonNull(Game.class.getResource("/org/linkingGame/cherry.png")).toExternalForm());
    public static Image imageGrape = new Image(Objects.requireNonNull(Game.class.getResource("/org/linkingGame/grape.png")).toExternalForm());
    public static Image imageCarambola = new Image(Objects.requireNonNull(Game.class.getResource("/org/linkingGame/carambola.png")).toExternalForm());
    public static Image imageKiwi = new Image(Objects.requireNonNull(Game.class.getResource("/org/linkingGame/kiwi.png")).toExternalForm());
    public static Image imageOrange = new Image(Objects.requireNonNull(Game.class.getResource("/org/linkingGame/orange.png")).toExternalForm());
    public static Image imagePeach = new Image(Objects.requireNonNull(Game.class.getResource("/org/linkingGame/peach.png")).toExternalForm());
    public static Image imagePear = new Image(Objects.requireNonNull(Game.class.getResource("/org/linkingGame/pear.png")).toExternalForm());
    public static Image imagePineapple = new Image(Objects.requireNonNull(Game.class.getResource("/org/linkingGame/pineapple.png")).toExternalForm());
    public static Image imageWatermelon = new Image(Objects.requireNonNull(Game.class.getResource("/org/linkingGame/watermelon.png")).toExternalForm());

}
