module com.mycompany.server {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires derbyclient;
    requires org.json;

    opens com.mycompany.server to javafx.fxml;

    exports com.mycompany.server;
}
