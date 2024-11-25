package org.linkingGame;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;

public class GameController {
    @FXML
    public VBox root;

    @FXML
    public Label roundReminderLabel;
    @FXML
    private Label myScoreLabel;
    @FXML
    public Label rivalScoreLabel;

    @FXML
    public StackPane gamePane;

    @FXML
    private GridPane gameBoard;

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
    int myScore, rivalScore = 0;

    public void setClientSocket(Socket socket) throws IOException {
        this.clientSocket = socket;
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    @FXML
    public void initialize() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {

                return null;
            }
        };

        new Thread(task).start();
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
            button.setStyle("-fx-border-color: #00ef00; -fx-border-width: 2px;");
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

                    ArrayList<Button> lineButtons = new ArrayList<>();
                    for (Tuple tuple : linkingResult.tuples) {
                        lineButtons.add(buttons[tuple.row][tuple.col]);
                    }
                    for (Button lineButton : lineButtons) {
                        lineButton.setStyle("-fx-border-color: #4fafff; -fx-border-width: 2px;");
                    }

                    PauseTransition delay = new PauseTransition(Duration.seconds(0.3));
                    delay.setOnFinished(e -> {
                        deleteGrid(button, row, col);
                        deleteGrid(lastButton, position[1], position[2]);
                        for (Button lineButton : lineButtons) {
                            lineButton.setStyle("");
                        }
                        myScore++;
                        myScoreLabel.setText(Integer.toString(myScore));
                    });
                    delay.play();
                }
                else {
                    System.out.println("failed!");
                    PauseTransition delay = new PauseTransition(Duration.seconds(0.1));
                    delay.setOnFinished(e -> {
                        addPopup((StackPane) gameBoard.getParent(), "Failed!");
                        lastButton.setStyle("");
                        button.setStyle("");
                    });
                    delay.play();
                }
                position[0] = 0;
                roundReminderLabel.setText("Rival's Round");
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
                    if (response.equals("game ended")) {
                        String responseOfWinner = in.readLine();
                        if (responseOfWinner.equals("you won")) {
                            int myFinalScore = Integer.parseInt(in.readLine());
                            int rivalFinalScore = Integer.parseInt(in.readLine());
                            Platform.runLater(() -> endTheGame(true, false, myFinalScore, rivalFinalScore));
                        } else if (responseOfWinner.equals("you lost")) {
                            int myFinalScore = Integer.parseInt(in.readLine());
                            int rivalFinalScore = Integer.parseInt(in.readLine());
                            Platform.runLater(() -> endTheGame(false, false, myFinalScore, rivalFinalScore));
                        } else if (responseOfWinner.equals("tie")) {
                            int finalScore = Integer.parseInt(in.readLine());
                            Platform.runLater(() -> endTheGame(false, true, finalScore, finalScore));
                        }
                        return null;
                    }
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

                        int lineSize = Integer.parseInt(in.readLine());
                        ArrayList<Button> lineButtons = new ArrayList<>();
                        for (int i = 0; i < lineSize; i++) {
                            int mediaRow = Integer.parseInt(in.readLine());
                            int mediaCol = Integer.parseInt(in.readLine());
                            lineButtons.add(buttons[mediaRow][mediaCol]);
                        }

                        int finalFormerRow = formerRow;
                        int finalFormerCol = formerCol;
                        Platform.runLater(() -> {
                            button.setStyle("-fx-border-color: #f00000; -fx-border-width: 2px;");

                            for (Button lineButton : lineButtons) {
                                lineButton.setStyle("-fx-border-color: #f09000; -fx-border-width: 2px;");
                            }

                            PauseTransition delay = new PauseTransition(Duration.seconds(0.3));
                            delay.setOnFinished(e -> {
                                deleteGrid(button, row, col);
                                deleteGrid(finalFormerButton, finalFormerRow, finalFormerCol);
                                for (Button lineButton : lineButtons) {
                                    lineButton.setStyle("");
                                }
                                //TODO: change rival's score
                                rivalScore++;
                                rivalScoreLabel.setText(Integer.toString(rivalScore));
                            });
                            delay.play();
                        });
                        //receive message from server whether the game ends
                        receiveMessageInMyTurn();
                        //display it's my turn
                        Platform.runLater(() -> roundReminderLabel.setText("Your Round"));
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
                        //receive message from server whether the game ends
                        receiveMessageInMyTurn();
                        //display it's my turn
                        Platform.runLater(() -> roundReminderLabel.setText("Your Round"));
                        myTurn = true;
                        break;
                    }
                }
                return null;
            }
        };

        new Thread(task).start();
    }

    private void receiveMessageInMyTurn() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws IOException {
                String response = in.readLine();
                if (response.equals("game ended")) {
                    String responseOfWinner = in.readLine();
                    if (responseOfWinner.equals("you won")) {
                        int myFinalScore = Integer.parseInt(in.readLine());
                        int rivalFinalScore = Integer.parseInt(in.readLine());
                        Platform.runLater(() -> endTheGame(true, false, myFinalScore, rivalFinalScore));
                    } else if (responseOfWinner.equals("you lost")) {
                        int myFinalScore = Integer.parseInt(in.readLine());
                        int rivalFinalScore = Integer.parseInt(in.readLine());
                        Platform.runLater(() -> endTheGame(false, false, myFinalScore, rivalFinalScore));
                    } else if (responseOfWinner.equals("tie")) {
                        int finalScore = Integer.parseInt(in.readLine());
                        Platform.runLater(() -> endTheGame(false, true, finalScore, finalScore));
                    }
                    return null;
                } else if (response.equals("game continues")) {
                    in.readLine();
                }
                return null;
            }
        };

        new Thread(task).start();
    }

    private void endTheGame(boolean won, boolean tie, int myFinalScore, int rivalFinalScore) {
        String info = won ? "Your Won!" : "Your Lost!";
        info = tie ? "Tie Game!" : info;
        info = info + " Your score: " + myFinalScore + ", Rival Score: " + rivalFinalScore;

        //add a popup box
        VBox popupBox = new VBox(10);
        popupBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #ff8c00; -fx-border-width: 2px; -fx-padding: 10;");
        popupBox.setAlignment(Pos.CENTER);
        popupBox.setPrefSize(20, 14);
        Text popupText = new Text(info);
        Button closeButton = new Button("OK");
        closeButton.setOnAction(event -> {
//            gamePane.getChildren().remove(popupBox);

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("welcome.fxml"));
            Scene welcomeScene;
            try {
                welcomeScene = new Scene(fxmlLoader.load());
            } catch (IOException e) {
                //TODO: exception?
                throw new RuntimeException(e);
            }

            WelcomeController welcomeController = fxmlLoader.getController();
            welcomeController.setClientSocket(clientSocket);
            welcomeController.hideNode(welcomeController.registerButton);
            welcomeController.hideNode(welcomeController.loginButton);
            welcomeController.showNode(welcomeController.matchOrPickBox);

            Stage stage = (Stage) root.getScene().getWindow();
            stage.setTitle("Welcome");
            stage.setScene(welcomeScene);
        });
        popupBox.getChildren().addAll(popupText, closeButton);
        gamePane.getChildren().add(popupBox);
    }



    @FXML
    private void handleReset() {
        //TODO: update that serverside takes over
        System.out.println("Reset");
        myScore = 0;
        myScoreLabel.setText("0");
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
        buttons[row][col] = newButton;
    }

    public void addPopup(StackPane gamePane, String Info) {
        VBox popupBox = new VBox(10);
        popupBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #ff8c00; -fx-border-width: 2px; -fx-padding: 10;");
        popupBox.setAlignment(Pos.CENTER);
        popupBox.setPrefSize(20, 14);

        Text popupText = new Text(Info);
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
