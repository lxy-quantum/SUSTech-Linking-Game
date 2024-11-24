module org.example.linkingGame {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.naming;


    opens org.linkingGame to javafx.fxml;
    exports org.linkingGame;
    exports org.example;
    opens org.example to javafx.fxml;
}