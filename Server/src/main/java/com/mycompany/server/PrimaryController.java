package com.mycompany.server;

import com.mycompany.server.network.SocketServer;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class PrimaryController {

    @FXML
    private Label statusLabel;

    @FXML
    private Button serverButton;

    private SocketServer server;

    @FXML
    public void initialize() {
        server = new SocketServer();
    }

    @FXML
    private void onServerToggle() {
        if (!server.isRunning()) {
            server.start();
            statusLabel.setText("STATUS: RUNNING");
            statusLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 18; -fx-font-weight: bold;");
            serverButton.setText("STOP SERVER");
            serverButton.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-padding: 12;");
        } else {
            server.stop();
            statusLabel.setText("STATUS: STOPPED");
            statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 18; -fx-font-weight: bold;");
            serverButton.setText("START SERVER");
            serverButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-padding: 12;");
        }
    }
}
