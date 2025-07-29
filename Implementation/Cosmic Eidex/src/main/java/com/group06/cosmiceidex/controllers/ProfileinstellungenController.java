package com.group06.cosmiceidex.controllers;

import com.group06.cosmiceidex.client.Client;
import com.group06.cosmiceidex.common.Message;
import com.group06.cosmiceidex.controllerlogic.ProfileinstellungenControllerLogic;
import com.group06.cosmiceidex.exceptions.AuthException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * Dient dazu, die Funktionen der Szene Profileinstellungen zu implementieren.
 */

public class ProfileinstellungenController implements Initializable {

    private Client client;
    private static ProfileinstellungenController ProfileinstellungenControllerInstance;

    @FXML
    public Label usernameLable;

    @FXML
    public PasswordField passwordField;

    @FXML
    public PasswordField repeatPasswordField;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        client = Client.getClientInstance();
        ProfileinstellungenControllerInstance = this;
        usernameLable.setText("Benutzername : " + client.getUsername());
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
     * Funktion des "Speichern"-Buttons.
     * Lädt die Lobby-Szene. Eingaben werden gespeichert.
     * @param event
     * @throws IOException
     */
    @FXML
    private void onSpeichern(ActionEvent event) throws IOException {
        String password = passwordField.getText().trim();
        String repeatedPassword = repeatPasswordField.getText().trim();
        String[] isPasswordValid = ProfileinstellungenControllerLogic.validatePassword(password, repeatedPassword);
        if(isPasswordValid == null) {
            Message request = new Message(Message.MessageType.CHANGE_PASSWORD_REQUEST, client.getUsername(), "SERVER", password);
            client.sendMessage(request);
        }
        else{
            showError(isPasswordValid[0], isPasswordValid[1]);
        }
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
     * Anzeigen der erfolgreichen Passwort Änderung.
     * @param title
     * @param message
     */
    public void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Laden der Lobby-Szene.
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
     * Gibt die aktuelle Instanz des Profileinstellungencontrollers zurück
     * @return
     */
    public static ProfileinstellungenController getProfileinstellungenControllerInstance(){
        return ProfileinstellungenControllerInstance;
    }
}
