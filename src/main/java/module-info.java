module org.example.assign2 {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.assign2 to javafx.fxml;
    exports org.assign2;
    exports org.example;
    opens org.example to javafx.fxml;
}