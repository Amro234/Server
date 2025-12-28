package com.mycompany.server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("serverui"), 1280, 720);
        stage.setScene(scene);

        // Set the application icon
        try {
            stage.getIcons()
                    .add(new javafx.scene.image.Image(App.class.getResourceAsStream("assets/images/serverIcon.png")));
        } catch (Exception e) {
            System.err.println("Could not load icon: " + e.getMessage());
        }

        stage.setTitle("Tic Tac Toe Server");
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}