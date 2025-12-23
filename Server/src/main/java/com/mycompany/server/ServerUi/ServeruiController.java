package com.mycompany.server.ServerUi;

import com.mycompany.server.db.DatabaseManager;
import com.mycompany.server.manager.OnlineUsersManager;
import com.mycompany.server.model.OnlineUser;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

public class ServeruiController implements Initializable {

    @FXML
    private Canvas donutCanvas;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        drawDonutChart();
    }

    private void drawDonutChart() {
        int totalUsers = DatabaseManager.getInstance().getTotalUsers();

        int onlineUsers = OnlineUsersManager.getInstance().getOnlineCount();

        int activeUsers = 0;
        for (OnlineUser user : OnlineUsersManager.getInstance().getAllOnlineUsersJSON()
                .toList()
                .stream()
                .map(o -> (java.util.Map<?, ?>) o)
                .map(map -> new OnlineUser(
                        (int) map.get("userId"),
                        (String) map.get("username"),
                        (String) map.get("email"),
                        (int) map.get("score"),
                        null))
                .toList()) {
            activeUsers++;
        }

        int offlineUsers = totalUsers - onlineUsers;
        if (offlineUsers < 0) offlineUsers = 0;

        System.out.println(
                "Active: " + activeUsers +
                ", Online: " + onlineUsers +
                ", Offline: " + offlineUsers +
                ", Total: " + totalUsers
        );

        GraphicsContext gc = donutCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, donutCanvas.getWidth(), donutCanvas.getHeight());

        double centerX = donutCanvas.getWidth() / 2;
        double centerY = donutCanvas.getHeight() / 2;
        double outerRadius = 110;
        double innerRadius = 75;

        double total = activeUsers + onlineUsers + offlineUsers;
        if (total == 0) total = 1;

        double startAngle = 90;

        double activeAngle = (activeUsers / total) * 360;
        gc.setFill(Color.web("#10B981"));
        drawDonutSlice(gc, centerX, centerY, outerRadius, startAngle, activeAngle);
        startAngle += activeAngle;

        double onlineAngle = (onlineUsers / total) * 360;
        gc.setFill(Color.web("#3B82F6"));
        drawDonutSlice(gc, centerX, centerY, outerRadius, startAngle, onlineAngle);
        startAngle += onlineAngle;

        double offlineAngle = (offlineUsers / total) * 360;
        gc.setFill(Color.web("#E5E7EB"));
        drawDonutSlice(gc, centerX, centerY, outerRadius, startAngle, offlineAngle);

        gc.setFill(Color.WHITE);
        gc.fillOval(centerX - innerRadius, centerY - innerRadius,
                innerRadius * 2, innerRadius * 2);
    }

    private void drawDonutSlice(GraphicsContext gc, double centerX, double centerY,
                                double radius, double startAngle, double arcAngle) {

        double x = centerX - radius;
        double y = centerY - radius;
        double diameter = radius * 2;

        gc.fillArc(x, y, diameter, diameter, startAngle, arcAngle, ArcType.ROUND);
    }
}
