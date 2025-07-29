package com.group06.cosmiceidex.controllers;

import com.group06.cosmiceidex.client.Client;
import com.group06.cosmiceidex.common.Message;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Dient dazu, die Funktionen der Szene Warteraum zu implementieren.
 */

public class WarteraumController implements Initializable {

    public Label player1Placeholder;
    public Label player2Placeholder;
    public Label player3Placeholder;
    private Client client;
    private static WarteraumController warteraumControllerInstance;
    private List<String> currentParticipants;


    @FXML
    private TextField chatInputField;

    @FXML
    private VBox chatBox;

    @FXML
    private ScrollPane chatScrollPane;

    @FXML
    private Button spielStartenButton;

    /**
     * Initialisiert Warteraum-Szene
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        client = Client.getClientInstance();
        warteraumControllerInstance = this;
        currentParticipants = new ArrayList<>();
        if(!client.getIsGameMaster()){
            spielStartenButton.setDisable(true);
        }
    }

    /**
     * Funktion des "Zurück"-Buttons.
     * Lädt die Lobby-Szene.
     * @param event
     * @throws IOException
     */
    @FXML
    // Zurück zu Login, Eingaben werden nicht übernommen
    public void onZurück(ActionEvent event) throws IOException {
        if(client.getIsGameMaster()){
            Message request = new Message(Message.MessageType.CLOSE_ROOM_REQUEST, client.getUsername(), "SERVER", client.getCurrentRoom());
            client.sendMessage(request);
        }else {
            Message request = new Message(Message.MessageType.LEAVE_ROOM_REQUEST, client.getUsername(), "SERVER", client.getCurrentRoom());
            client.sendMessage(request);
        }
    }

    /**
     * Funktion des "Spiel starten"-Buttons. Lädt Spielraum.
     * @param event
     * @throws IOException
     */
    @FXML
    // Zurück zu Login, Eingaben werden nicht übernommen
    private void onSpielStarten(ActionEvent event) throws IOException {
        if (!client.getIsGameMaster()) {
            showError("Spiel Starten Fehler", "Sie sind nicht GameMaster, um das Spiel zu starten.");
            return;
        }

        // Prüfe, ob die vom Server bestätigte Spieleranzahl 3 ist.
        if (currentParticipants.size() != 3) {
            showError("Spieler Anzahl Fehler", "Es müssen genau 3 Spieler (inkl. Bots) sein.");
            return;
        }


        //Sende Raum Name als Payload.
        Message request = new Message(Message.MessageType.START_GAME_REQUEST, client.getUsername(), "SERVER", client.getCurrentRoom());
        client.sendMessage(request);
    }

    /**
     * Funktion des "Absenden"-Buttons. Überprüft, ob Nachricht max. 25 Buchstaben hat. Zeigt Fehler, falls.
     * Überprüft auf leere Nachrichten. Fügt Nachrichten zu Chatbox hinzu, wenn alles in Ordnung.
     */
    @FXML
    private void onSendMessage() {
        String message = chatInputField.getText().trim();
        if(message.length() > 25){
            showError("Zu lange Nachricht", "Die Länge der Nachricht darf nicht 25 Buchstaben/Zeichen überschreiten.");
            return;
        }
        // Kontrollieren, dass Nachricht nicht leer ist
        if (!message.isEmpty()) {
            Message msg = new Message(Message.MessageType.WAITING_ROOM_CHAT_REQUEST, client.getUsername(), client.getCurrentRoom(), message);
            client.sendMessage(msg);

            // Texteingabe wieder frei machen
            chatInputField.clear();
        }
    }

    /**
     * Funktion des Buttons zum Hinzufügen eines leichten Bots.
     */
    @FXML
    private void onAddEasyBot(){
        if (!client.getIsGameMaster()) {
            showError("Keine Berechtigung", "Nur der Host kann Bots hinzufügen.");
            return;
        }
        // "Easy" Payload an Server schicken
        Message request = new Message(Message.MessageType.ADD_BOT, client.getUsername(), client.getCurrentRoom(), "Easy");
        client.sendMessage(request);
    }

    /**
     * Funktion des Buttons zum Hinzufügen eines schweren Bots.
     */
    @FXML
    private void onAddDifficultBot(){
        if (!client.getIsGameMaster()) {
            showError("Keine Berechtigung", "Nur der Host kann Bots hinzufügen.");
            return;
        }
        // "Hard" Payload an Server schicken
        Message request = new Message(Message.MessageType.ADD_BOT, client.getUsername(), client.getCurrentRoom(), "Hard");
        client.sendMessage(request);
    }

    /**
     * Funktion des Buttons zum Entfernen aller Bots.
     */
    @FXML
    private void onRemoveBots(){
        if (!client.getIsGameMaster()) {
            showError("Keine Berechtigung", "Nur der Host kann Bots entfernen.");
            return;
        }
        Message request = new Message(Message.MessageType.REMOVE_BOT, client.getUsername(), client.getCurrentRoom(), null);
        client.sendMessage(request);
    }

    /**
     * Fügt Nachricht in GUI in Chatbox ein.
     * @param username Username von Nutzer, der die Nachricht geschrieben hat.
     * @param message Abzubildende Nachricht.
     */
    public void addMessageToChatBox(String username, String message) {
        Text messageText = new Text(username + " : " + message);
        messageText.wrappingWidthProperty().bind(chatScrollPane.widthProperty().subtract(30));
        chatBox.getChildren().add(messageText);

        // Automatisch nach unten scrollen
        chatScrollPane.layout();
        chatScrollPane.setVvalue(1.0);
    }

    /**
     * Anzeigen einer Fehlermeldung in GUI
     * @param title Titel der Fehlermeldung.
     * @param message Nachricht der Fehlermeldung.
     */
    public void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Laden der Login-Szene.
     * @param stage
     */
    public void switchToLobbyGUI(Stage stage){
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/group06/cosmiceidex/FXMLFiles/Lobby.fxml")));
            double width = stage.getWidth();
            double height = stage.getHeight();
            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            stage.show();
        }catch(IOException e){
            System.out.println("[ERROR] [" + client.getUsername() + "] " + "Failed to switch to Lobby GUI" );
        }
    }


    /**
     * Zum Spielraum wechseln
     * @param stage
     */
    public void switchToSpielraumGUI(Stage stage){
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/group06/cosmiceidex/FXMLFiles/Spielraum.fxml")));
            double width = stage.getWidth();
            double height = stage.getHeight();
            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            stage.show();
        }catch(IOException e){
            System.out.println("[ERROR] [" + client.getUsername() + "] " + "Failed to switch to Spielraum GUI" );
            e.printStackTrace();
        }
    }


    /**
     * Anzeigen der vom Server übergebenen Daten
     * @param participants übergebene Daten
     * @author TB
     */
    public void displayRoomParticipants(List<String> participants){
        this.currentParticipants = participants;

        // UI labels reseten und mit vom Server geschickten Sachen füllen
        player1Placeholder.setText("Platz verfügbar");
        player2Placeholder.setText("Platz verfügbar");
        player3Placeholder.setText("Platz verfügbar");
        if (!participants.isEmpty()) {
            player1Placeholder.setText(participants.get(0));
        }
        if (participants.size() > 1) {
            player2Placeholder.setText(participants.get(1));
        }
        if (participants.size() > 2) {
            player3Placeholder.setText(participants.get(2));
        }
    }
    public static WarteraumController getWarteraumControllerInstance() {
        return warteraumControllerInstance;
    }
}
