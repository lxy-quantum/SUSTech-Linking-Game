module org.example.assign2 {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.assign2 to javafx.fxml;
    exports org.example.assign2;
}