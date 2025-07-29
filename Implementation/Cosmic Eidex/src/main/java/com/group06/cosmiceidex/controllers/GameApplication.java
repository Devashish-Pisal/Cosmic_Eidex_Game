package com.group06.cosmiceidex.controllers;

import com.group06.cosmiceidex.client.Client;
import com.group06.cosmiceidex.server.DatabaseService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Dient dazu die Applikation des Spiels zu starten.
 */

public class GameApplication extends Application {

    /**
     * Startet die Applikation
      * @param stage Das Fenster, das geöffnet wird
     * @throws IOException
     */
    @Override
    public void start(Stage stage) throws IOException {
        Client client = Client.getClientInstance();
        client.setPrimaryStage(stage);
        FXMLLoader fxmlLoader = new FXMLLoader(GameApplication.class.getResource("/com/group06/cosmiceidex/FXMLFiles/Login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
        // Das nachträgliche Setzen der Size ist nötig, um den Bug zu fixen, dass beim Wechseln der Szene das Fenster in der gleichen Größe bleibt
        // das Nutzen während der Initialisierung sorgte dafür, dass das Fenster mit dem Wechseln der Szenen größer wurde
        stage.setWidth(1280);
        stage.setHeight(720);
        stage.show();
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args){
        launch();
    }
}