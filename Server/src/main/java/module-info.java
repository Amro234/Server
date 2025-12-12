module com.mycompany.server {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.mycompany.server to javafx.fxml;
    exports com.mycompany.server;
}
