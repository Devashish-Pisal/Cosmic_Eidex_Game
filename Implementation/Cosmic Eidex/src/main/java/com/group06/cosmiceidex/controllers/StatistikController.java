package com.group06.cosmiceidex.controllers;

import com.group06.cosmiceidex.client.Client;
import com.group06.cosmiceidex.controllerlogic.SaveControllerVariables;
import com.group06.cosmiceidex.game.GameSession;
import com.group06.cosmiceidex.game.Player;
import com.group06.cosmiceidex.game.PlayerInterface;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Dient dazu, die Funktionen der Szene Statistik zu implementieren.
 */

public class StatistikController implements Initializable {

    public Label nutzernamePlaceholder;
    public Label wonGamesPlaceholder;
    public Label wonPartienPlaceholder;
    public Label wonStichePlacholder;

    private static StatistikController statistikControllerInstance;
    private Client client;
    private GameSession session;

    /**
     * Initialisiert Statistik GUI
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        client = Client.getClientInstance();
        statistikControllerInstance = this;
        session = client.getSession();
        setLabels();
    }

    /**
     * Setzten alle Labels mit Spieler Information
     * @author Devashish Pisal
     */
    public void setLabels(){
        PlayerInterface player = session.getPlayersWithUsernames().get(client.getUsername());
        nutzernamePlaceholder.setText("Spieler: " + player.getUsername());
        wonGamesPlaceholder.setText("Gewinnpunkte: " + player.getWonPartyPoints());
        wonPartienPlaceholder.setText("Anzahl der gewonnenen Stiche: " + player.getNumberOfWonTricksDuringEntireGame());
        wonStichePlacholder.setText("Bis jetzt gewonnene Stichpunkte: " + player.getTotalWonTrickPointDuringEntireGame());
    }

    /**
     * Funktion des "Zurück"-Buttons.
     * Lädt die vorherige Szene.
     * @param event
     * @throws IOException
     */
    @FXML
    private void onZurück(ActionEvent event) throws IOException {
        Parent root;
        // FXML laden
        if (SaveControllerVariables.getPrevScene() == "Spielraum"){
            root = FXMLLoader.load(getClass().getResource("/com/group06/cosmiceidex/FXMLFiles/Spielraum.fxml"));
        }
        else{
            root = FXMLLoader.load(getClass().getResource("/com/group06/cosmiceidex/FXMLFiles/Lobby.fxml"));
        }
        // Aktuelle Stage laden
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // Aktuelle Fenstergröße zwischenspeichern
        double width = stage.getWidth();
        double height = stage.getHeight();

        // Fenstergröße mitgeben und Szene wechseln
        Scene scene = new Scene(root, width, height);
        stage.setScene(scene);
        stage.show();
    }
}
