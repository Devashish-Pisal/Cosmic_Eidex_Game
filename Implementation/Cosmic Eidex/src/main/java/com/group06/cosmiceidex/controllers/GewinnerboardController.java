package com.group06.cosmiceidex.controllers;

import com.group06.cosmiceidex.client.Client;
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
import java.util.HashMap;
import java.util.ResourceBundle;

/**
 * Dient dazu, die Funktionen der Szene Gewinnerboard zu implementieren.
 */

public class GewinnerboardController implements Initializable {

    private Client client;
    private static GewinnerboardController gewinnerboardControllerInstance;
    private HashMap<Integer, PlayerInterface> result;

    public Label gewinnerNamePlaceholder;
    public Label zweiterNamePlaceholder;
    public Label dritterNamePlaceholder;

    public Label gewinnerStatsPlaceholder;
    public Label zweiterStatsPlaceholder;
    public Label dritterStatsPlaceholder;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        client = Client.getClientInstance();
        gewinnerboardControllerInstance = this;
        result = client.getLastMatchResult();
        client.setLastMatchResult(null);

        if(result != null){
            gewinnerNamePlaceholder.setText(result.get(1).getUsername());
            zweiterNamePlaceholder.setText(result.get(2).getUsername());
            dritterNamePlaceholder.setText(result.get(3).getUsername());

            gewinnerStatsPlaceholder.setText("Gewinnpunkte : " + result.get(1).getWonPartyPoints());
            zweiterStatsPlaceholder.setText("Gewinnpunkte : " + result.get(2).getWonPartyPoints());
            dritterStatsPlaceholder.setText("Gewinnpunkte : " + result.get(3).getWonPartyPoints());
        }
    }

    /**
     * Funktion des "Neues Spiel"-Buttons.
     * Lädt die Warteraum-Szene.
     * @param event
     * @throws IOException
     */
    @FXML
    private void onNeuesSpiel(ActionEvent event) throws IOException {
        // FXML laden
        Parent root = FXMLLoader.load(getClass().getResource("/com/group06/cosmiceidex/FXMLFiles/Warteraum.fxml"));
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

    /**
     * Funktion des "Zurück zur Lobby"-Buttons.
     * Lädt die Lobby-Szene.
     * @param event
     * @throws IOException
     */
    @FXML
    private void onLobby(ActionEvent event) throws IOException {
        // FXML laden
        Parent root = FXMLLoader.load(getClass().getResource("/com/group06/cosmiceidex/FXMLFiles/Lobby.fxml"));
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


    public GewinnerboardController getGewinnerboardControllerInstance(){
        return gewinnerboardControllerInstance;
    }

}
