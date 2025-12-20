/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.mycompany.server.ServerUi;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

/**
 * FXML Controller class
 *
 * @author DELLgit
 */
public class ServeruiController implements Initializable {
    
    @FXML
    private Canvas donutCanvas;
    
   
    private final int activeUsers = 428;
    private final int onlineUsers = 356;
    private final int offlineUsers = 240;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        drawDonutChart();
    }
    
    
    private void drawDonutChart() {
        GraphicsContext gc = donutCanvas.getGraphicsContext2D();
        
      
        gc.clearRect(0, 0, donutCanvas.getWidth(), donutCanvas.getHeight());
        
      
        double centerX = donutCanvas.getWidth() / 2;
        double centerY = donutCanvas.getHeight() / 2;
        double outerRadius = 110; 
        double innerRadius = 75;  
        
       
        double total = activeUsers + onlineUsers + offlineUsers;
        double startAngle = 90; 
        
        
        double activeAngle = (activeUsers / total) * 360;
        gc.setFill(Color.web("#10B981"));
        drawDonutSlice(gc, centerX, centerY, outerRadius, innerRadius, 
                      startAngle, activeAngle);
        startAngle += activeAngle;
        
        
        double onlineAngle = (onlineUsers / total) * 360;
        gc.setFill(Color.web("#3B82F6"));
        drawDonutSlice(gc, centerX, centerY, outerRadius, innerRadius, 
                      startAngle, onlineAngle);
        startAngle += onlineAngle;
        
        
        double offlineAngle = (offlineUsers / total) * 360;
        gc.setFill(Color.web("#E5E7EB"));
        drawDonutSlice(gc, centerX, centerY, outerRadius, innerRadius, 
                      startAngle, offlineAngle);
        
       
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
}