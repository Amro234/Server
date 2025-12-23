package com.mycompany.server.ServerUi;

import com.mycompany.server.db.DatabaseManager;
import com.mycompany.server.manager.OnlineUsersManager;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

public class ServeruiController implements Initializable {
    
    @FXML
    private Canvas donutCanvas;
    
    private int activeUsers = 0;
    private int onlineUsers = 0;
    private int offlineUsers = 0;
    
    @FXML
    private Label activeLabel;
    @FXML
    private Label onlineLabel;
    @FXML
    private Label offlineLabel;
    @FXML
    private Label totalLabel;
    @FXML
    private Label activeLabelPrecentege;
    @FXML
    private Label onlineLabelPricentege;
    @FXML
    private Label offlineLabelPrecentege;
    
    private DatabaseManager dbManager;
    private OnlineUsersManager onlineManager;
    private Thread updateThread;
    private volatile boolean running = true;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dbManager = DatabaseManager.getInstance();
        onlineManager = OnlineUsersManager.getInstance();
        
        updateData();
        startUpdateThread();
    }
    
    private void updateData() {
        try {
            int totalUsers = dbManager.getTotalUsers();
            int totalOnlineUsers = onlineManager.getOnlineCount();
            
            activeUsers = 0;
            onlineUsers = 0;
            
            org.json.JSONArray usersArray = onlineManager.getAllOnlineUsersJSON();
            for (int i = 0; i < usersArray.length(); i++) {
                org.json.JSONObject user = usersArray.getJSONObject(i);
                if (user.getBoolean("isInGame")) {
                    activeUsers++;
                } else {
                    onlineUsers++;
                }
            }
            
            offlineUsers = totalUsers - totalOnlineUsers;
            
            System.out.println("[UI] Total: " + totalUsers + " | Online: " + totalOnlineUsers + 
                             " | Active: " + activeUsers + " | Available: " + onlineUsers + 
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
        activeLabel.setText(String.valueOf(activeUsers));
        onlineLabel.setText(String.valueOf(onlineUsers));
        offlineLabel.setText(String.valueOf(offlineUsers));
        totalLabel.setText(String.valueOf(total));
        
        if (total > 0) {
            double activePercent = (activeUsers * 100.0) / total;
            double onlinePercent = (onlineUsers * 100.0) / total;
            double offlinePercent = (offlineUsers * 100.0) / total;
            
            activeLabelPrecentege.setText(String.format("%.1f%%", activePercent));
            onlineLabelPricentege.setText(String.format("%.1f%%", onlinePercent));
            offlineLabelPrecentege.setText(String.format("%.1f%%", offlinePercent));
        } else {
            activeLabelPrecentege.setText("0%");
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
    
    private void drawDonutChart() {
        GraphicsContext gc = donutCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, donutCanvas.getWidth(), donutCanvas.getHeight());
        
        double centerX = donutCanvas.getWidth() / 2;
        double centerY = donutCanvas.getHeight() / 2;
        double outerRadius = 110; 
        double innerRadius = 75;  
        
        double total = activeUsers + onlineUsers + offlineUsers;
        
        if (total == 0) {
            gc.setFill(Color.web("#E5E7EB"));
            drawDonutSlice(gc, centerX, centerY, outerRadius, innerRadius, 0, 360);
            gc.setFill(Color.WHITE);
            gc.fillOval(centerX - innerRadius, centerY - innerRadius,
                       innerRadius * 2, innerRadius * 2);
            return;
        }
        
        double startAngle = 90; 
        
        if (activeUsers > 0) {
            double activeAngle = (activeUsers / total) * 360;
            gc.setFill(Color.web("#10B981"));
            drawDonutSlice(gc, centerX, centerY, outerRadius, innerRadius, 
                          startAngle, activeAngle);
            startAngle += activeAngle;
        }
        
        if (onlineUsers > 0) {
            double onlineAngle = (onlineUsers / total) * 360;
            gc.setFill(Color.web("#3B82F6"));
            drawDonutSlice(gc, centerX, centerY, outerRadius, innerRadius, 
                          startAngle, onlineAngle);
            startAngle += onlineAngle;
        }
        
        if (offlineUsers > 0) {
            double offlineAngle = (offlineUsers / total) * 360;
            gc.setFill(Color.web("#E5E7EB"));
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
    
    @FXML
    private void ActiveLabel(MouseEvent event) {
        updateData();
    }
    
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
    private void ActiveLabelPrecentege(MouseEvent event) {
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