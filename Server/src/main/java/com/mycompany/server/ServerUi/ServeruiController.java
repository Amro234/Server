package com.mycompany.server.ServerUi;

import com.mycompany.server.db.DatabaseManager;
import com.mycompany.server.manager.OnlineUsersManager;
import com.mycompany.server.network.SocketServer;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;

public class ServeruiController implements Initializable {

    @FXML
    private Canvas donutCanvas;

    // User statistics labels
    @FXML
    private Label onlineLabel;
    @FXML
    private Label offlineLabel;
    @FXML
    private Label totalLabel;
    @FXML
    private Label onlineLabelPricentege;
    @FXML
    private Label offlineLabelPrecentege;

    // Server status components
    @FXML
    private Label statusLabel;
    @FXML
    private Circle statusCircle;
    @FXML
    private Button startServerBtn;
    @FXML
    private Button stopServerBtn;

    // User counts
    private int onlineUsers = 0;
    private int offlineUsers = 0;

    // Managers and threads
    private DatabaseManager dbManager;
    private OnlineUsersManager onlineManager;
    private SocketServer server;
    private Thread updateThread;
    private volatile boolean running = true;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dbManager = DatabaseManager.getInstance();
        onlineManager = OnlineUsersManager.getInstance();
        server = new SocketServer();

        updateData();
        startUpdateThread();
    }

    // ============ Server Control Methods ============

    @FXML
    private void handleStartServer(ActionEvent event) {
        if (!server.isRunning()) {
            server.start();
            updateServerStatus(true);
        }
    }

    @FXML
    private void handleStopServer(ActionEvent event) {
        if (server.isRunning()) {
            server.stop();
            updateServerStatus(false);
        }
    }

    private void updateServerStatus(boolean isOnline) {
        Platform.runLater(() -> {
            if (isOnline) {
                statusLabel.setText("Server Online");
                statusLabel.setTextFill(Color.web("#12d335"));
                statusCircle.setFill(Color.web("#1fff63"));
                statusCircle.setStroke(Color.web("#1fff63"));
            } else {
                statusLabel.setText("Server Offline");
                statusLabel.setTextFill(Color.web("#ff0000"));
                statusCircle.setFill(Color.web("#ff0000"));
                statusCircle.setStroke(Color.web("#ff0000"));
            }
        });
    }

    // ============ Data Update Methods ============

    private void updateData() {
        try {
            int totalUsers = dbManager.getTotalUsers();
            int totalOnlineUsers = onlineManager.getOnlineCount();

            onlineUsers = totalOnlineUsers;
            offlineUsers = totalUsers - totalOnlineUsers;

            System.out.println("[UI] Total: " + totalUsers + " | Online: " + totalOnlineUsers +
                    " | Offline: " + offlineUsers);

            Platform.runLater(() -> {
                updateLabels(totalUsers);
                drawDonutChart();
            });
        } catch (Exception e) {
            System.err.println("[UI] Error updating data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateLabels(int total) {
        onlineLabel.setText(String.valueOf(onlineUsers));
        offlineLabel.setText(String.valueOf(offlineUsers));
        totalLabel.setText(String.valueOf(total));

        if (total > 0) {
            double onlinePercent = (onlineUsers * 100.0) / total;
            double offlinePercent = (offlineUsers * 100.0) / total;

            onlineLabelPricentege.setText(String.format("%.1f%%", onlinePercent));
            offlineLabelPrecentege.setText(String.format("%.1f%%", offlinePercent));
        } else {
            onlineLabelPricentege.setText("0%");
            offlineLabelPrecentege.setText("0%");
        }
    }

    private void startUpdateThread() {
        updateThread = new Thread(() -> {
            while (running) {
                try {
                    updateData();
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        updateThread.setDaemon(true);
        updateThread.start();
    }

    public void stopUpdateThread() {
        running = false;
        if (updateThread != null) {
            updateThread.interrupt();
        }
    }

    // ============ Chart Drawing Methods ============

    private void drawDonutChart() {
        GraphicsContext gc = donutCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, donutCanvas.getWidth(), donutCanvas.getHeight());

        double centerX = donutCanvas.getWidth() / 2;
        double centerY = donutCanvas.getHeight() / 2;
        double outerRadius = 110;
        double innerRadius = 75;

        double total = onlineUsers + offlineUsers;

        if (total == 0) {
            gc.setFill(Color.web("#E5E7EB"));
            drawDonutSlice(gc, centerX, centerY, outerRadius, innerRadius, 0, 360);
            gc.setFill(Color.WHITE);
            gc.fillOval(centerX - innerRadius, centerY - innerRadius,
                    innerRadius * 2, innerRadius * 2);
            return;
        }

        double startAngle = 90;

        if (onlineUsers > 0) {
            double onlineAngle = (onlineUsers / total) * 360;
            gc.setFill(Color.web("#10B981")); // Green color for online
            drawDonutSlice(gc, centerX, centerY, outerRadius, innerRadius,
                    startAngle, onlineAngle);
            startAngle += onlineAngle;
        }

        if (offlineUsers > 0) {
            double offlineAngle = (offlineUsers / total) * 360;
            gc.setFill(Color.web("#E5E7EB")); // Gray color for offline
            drawDonutSlice(gc, centerX, centerY, outerRadius, innerRadius,
                    startAngle, offlineAngle);
        }

        gc.setFill(Color.WHITE);
        gc.fillOval(centerX - innerRadius, centerY - innerRadius,
                innerRadius * 2, innerRadius * 2);
    }

    private void drawDonutSlice(GraphicsContext gc, double centerX, double centerY,
            double outerRadius, double innerRadius,
            double startAngle, double arcAngle) {

        double x = centerX - outerRadius;
        double y = centerY - outerRadius;
        double diameter = outerRadius * 2;

        gc.fillArc(x, y, diameter, diameter, startAngle, arcAngle, ArcType.ROUND);
    }

    // ============ Mouse Event Handlers ============

    @FXML
    private void OnlineLabel(MouseEvent event) {
        updateData();
    }

    @FXML
    private void OfflineLabel(MouseEvent event) {
        updateData();
    }

    @FXML
    private void TotalLabel(MouseEvent event) {
        updateData();
    }

    @FXML
    private void OnlineLabelPricentege(MouseEvent event) {
        updateData();
    }

    @FXML
    private void OfflineLabelPrecentege(MouseEvent event) {
        updateData();
    }
}