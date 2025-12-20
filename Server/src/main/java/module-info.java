module com.mycompany.server {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;
    requires java.sql;
    requires derbyclient;

    opens com.mycompany.server to javafx.fxml;
    opens com.mycompany.server.ServerUi to javafx.fxml;

    exports com.mycompany.server;
}
