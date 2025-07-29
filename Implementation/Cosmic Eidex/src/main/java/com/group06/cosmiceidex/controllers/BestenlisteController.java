package com.group06.cosmiceidex.controllers;

import com.group06.cosmiceidex.client.Client;
import com.group06.cosmiceidex.common.Message;
import com.group06.cosmiceidex.controllerlogic.SaveControllerVariables;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.group06.cosmiceidex.common.LeaderboardEntry;
import java.util.List;
import com.group06.cosmiceidex.controllerlogic.BestenlisteControllerLogic;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Dient dazu, die Funktionen der Szene Bestenliste zu implementieren.
 */

public class BestenlisteController {

    private Client client;
    private static BestenlisteController bestenlisteControllerInstance;

    DateTimeFormatter zeitFormat = DateTimeFormatter.ofPattern("HH:mm:ss");


    @FXML
    public ScrollPane bestenlisteScrollPane;

    @FXML
    public VBox bestenlisteBox;

    @FXML
    Label zuletztAktualisiertLabel;

    /**
     * Lädt die Bestenliste Szene
     */
    public void initialize() {
        this.client = Client.getClientInstance();
        bestenlisteControllerInstance = this;
        Message request = new Message(Message.MessageType.COMPLETE_LEADERBOARD_REQUEST, client.getUsername(), "SERVER", null);
        client.sendMessage(request);
        zuletztAktualisiertLabel.setText("Zuletzt aktualisiert: " + LocalTime.now().format(zeitFormat));
    }

    /**
     * Funktion des "Zurück"-Buttons.
     * Lädt die vorherige Szene (Spielraum, Lobby sonst)
     *
     * @param event
     * @throws IOException
     */
    @FXML
    public void onZurück(ActionEvent event) throws IOException {
        Parent root;
        // FXML laden
        if (SaveControllerVariables.getPrevScene() == "Spielraum"){
            root = FXMLLoader.load(getClass().getResource("/com/group06/cosmiceidex/FXMLFiles/Spielraum.fxml"));
        }
        else {
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

    /**
     * Funktion des "Aktualisieren"-Buttons. Lädt die aktuelle Bestenliste.
     *
     * @param event
     * @throws IOException
     */
    @FXML
    public void onAktualisieren(ActionEvent event) throws IOException {
        bestenlisteBox.getChildren().clear();
        Message request = new Message(Message.MessageType.COMPLETE_LEADERBOARD_REQUEST, client.getUsername(), "SERVER", null);
        client.sendMessage(request);
        zuletztAktualisiertLabel.setText("Zuletzt aktualisiert: " + LocalTime.now().format(zeitFormat));
    }

    /**
     * Zeigt Bestenliste im GUI an
     * @param leaderboard
     */
    public void bestenlisteZeigen(List<LeaderboardEntry> leaderboard) {
        // VBox zuerst leeren, falls sie bereits Einträge enthält
        bestenlisteBox.getChildren().clear();

        // Durch alle geladenen und sortierten Einträge iterieren
        for (int i = 0; i < leaderboard.size(); i++) {
            LeaderboardEntry entry = leaderboard.get(i);
            String text = BestenlisteControllerLogic.createEntry(entry, i);

            Label benutzer = new Label(text);
            benutzer.setStyle("-fx-padding: 5 10 5 10;");

            bestenlisteBox.getChildren().add(benutzer);
        }
        bestenlisteScrollPane.layout();
        bestenlisteScrollPane.setVvalue(1.0);
    }

    /**
     * Gibt Singleton Instanz zurück
     * @return
     */
    public static BestenlisteController getInstance() {
        return bestenlisteControllerInstance;
    }

    // Nur für Testing
    public static void setBestenlisteControllerInstance(BestenlisteController controller){
        bestenlisteControllerInstance = controller;
    }
}
