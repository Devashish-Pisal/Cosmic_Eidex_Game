package com.group06.cosmiceidex.controllers;

import com.group06.cosmiceidex.client.Client;
import com.group06.cosmiceidex.common.Message;
import com.group06.cosmiceidex.common.RoomCredential;
import com.group06.cosmiceidex.controllerlogic.SaveControllerVariables;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Dient dazu, die Funktionen der Szene SpielraumLogin zu implementieren.
 */

public class SpielraumLoginController implements Initializable {

    private Client client;
    private static SpielraumLoginController spielraumLoginControllerInstance;

    @FXML private PasswordField passwordField;
    
    @FXML private Node root;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        client = Client.getClientInstance();
        spielraumLoginControllerInstance = this;
            
        // Wenn zu einem beliebigen Zeitpunkt "Enter" gedrückt wird, LogIn Button feuern
        root.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                try{
                    onBeitreten(null);
                } catch(IOException e){
                    showError("Passwort Fehler", "Passwort darf nicht leer sein!");
                }
            }
        });
    }


    /**
     * Funktion des "Zurück"-Buttons.
     * Lädt die Lobby-Szene.
     * @param event
     * @throws IOException
     */
    @FXML
    private void onZurück(ActionEvent event) throws IOException {
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

    /**
     * Funktion des "Beitreten"-Buttons.
     * Lädt die Warteraum-Szene, sofern das Passwort korrekt ist.
     * @param event
     * @throws IOException
     */
    @FXML
    private void onBeitreten(ActionEvent event) throws IOException {
        if(passwordField.getText().trim().isEmpty()){
            showError("Passwort Fehler", "Passwort darf nicht leer sein!");
            return;
        }else{
            RoomCredential credentials = new RoomCredential(SaveControllerVariables.getSelectedSpielraum().trim(), passwordField.getText().trim());
            Message request = new Message(Message.MessageType.JOIN_ROOM_REQUEST, client.getUsername(), "SERVER", credentials);
            client.sendMessage(request);
        }
    }


    /**
     * Anzeigen einer Fehlermeldung in GUI
     * @param title Titel der Fehlermeldung.
     * @param message Nachricht der Fehlermeldung.
     */
    public void showError(String title, String message) {
        passwordField.clear();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    /**
     * Laden der Warteraum Szene
     * @param stage
     */
    public void switchToWarteraumGUI(Stage stage){
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/group06/cosmiceidex/FXMLFiles/Warteraum.fxml")));
            double width = stage.getWidth();
            double height = stage.getHeight();
            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            stage.show();
        }catch(IOException e){
            System.out.println("[ERROR] [" + client.getUsername() + "] " + "Failed to switch to Warteraum GUI" );
        }
    }

    public static SpielraumLoginController getSpielraumLoginControllerInstance() {
        return spielraumLoginControllerInstance;
    }
}
