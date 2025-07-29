package com.group06.cosmiceidex.controllers;

import com.group06.cosmiceidex.client.Client;
import com.group06.cosmiceidex.common.Message;
import com.group06.cosmiceidex.common.RoomCredential;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import com.group06.cosmiceidex.controllerlogic.SpielraumErstellenControllerLogic;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Dient dazu, die Funktionen der Szene SpielraumErstellen zu implementieren.
 */

public class SpielraumErstellenController implements Initializable {

    private Client client;
    private static SpielraumErstellenController spielraumErstellenControllerInstance;

    @FXML public TextField spielraumNameField;

    @FXML public PasswordField passwordField;

    @FXML public PasswordField repeatPasswordField;

    @FXML private Node root;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        client = Client.getClientInstance();
        spielraumErstellenControllerInstance = this;


        // Wenn zu einem beliebigen Zeitpunkt "Enter" gedrückt wird, LogIn Button feuern
        root.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                try{
                    onWeiter(null);
                } catch(IOException e){
                    showError("Passwort Fehler", "Passwort darf nicht leer sein!");
                }
            }
        });
    }

    /**
     * Funktion des "Abbrechen"-Buttons.
     * Lädt die Lobby-Szene. Eingaben werden verworfen.
     * @param event
     * @throws IOException
     */
    @FXML
    private void onAbbrechen(ActionEvent event) throws IOException {
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
     * Funktion des "Weiter"-Buttons.
     * Lädt die Warteraum-Szene. Eingaben werden gespeichert.
     * @param event
     * @throws IOException
     */
    @FXML
    private void onWeiter(ActionEvent event) throws IOException {
        String[] isGameroomValid = SpielraumErstellenControllerLogic.validateGameroom(spielraumNameField.getText(), passwordField.getText(), repeatPasswordField.getText());
        if(isGameroomValid == null){
            RoomCredential roomCredentials = new RoomCredential(spielraumNameField.getText(), passwordField.getText());
            Message request = new Message(Message.MessageType.CREATE_ROOM_REQUEST, client.getUsername(), "SERVER", roomCredentials);
            client.sendMessage(request);
        }
        else{
            showError(isGameroomValid[0], isGameroomValid[1]);
        }
    }


    /**
     * Anzeigen einer Fehlermeldung in GUI
     * @param title Titel der Fehlermeldung.
     * @param message Nachricht der Fehlermeldung.
     */
    public void showError(String title, String message) {
        spielraumNameField.clear();
        passwordField.clear();
        repeatPasswordField.clear();
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

    public static SpielraumErstellenController getSpielraumErstellenControllerInstance() {
        return spielraumErstellenControllerInstance;
    }
}
