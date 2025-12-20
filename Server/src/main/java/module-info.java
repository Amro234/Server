module com.mycompany.server {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;

    opens com.mycompany.server to javafx.fxml;
    exports com.mycompany.server;
}
