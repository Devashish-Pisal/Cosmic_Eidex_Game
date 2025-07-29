package com.group06.cosmiceidex.controllers;

import com.group06.cosmiceidex.client.Client;
import com.group06.cosmiceidex.common.Message;
import com.group06.cosmiceidex.common.User;
import com.group06.cosmiceidex.controllerlogic.RegistrierenControllerLogic;
import com.group06.cosmiceidex.exceptions.AuthException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * Dient dazu, die Funktionen der Szene Registrieren zu implementieren.
 */

public class RegistrierenController implements Initializable {

    private static RegistrierenController registrierenControllerInstance;
    private Client client;

    @FXML private TextField usernameField;

    @FXML private PasswordField passwordField;

    @FXML private PasswordField repeatPasswordField;

    @FXML private Button registrierenButton;

    @FXML private Node root;

    /**
     * Initialisiert Registrieren-Szene
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.client = Client.getClientInstance();
        registrierenControllerInstance = this;

        // Wenn zu einem beliebigen Zeitpunkt "Enter" gedrückt wird, Registrieren Button feuern
        root.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                registrierenButton.fire();
            }
        });
    }

    /**
     * Ruft RegistrierenController-Instanz auf
     * @return RegistrierenController-Instanz
     */
    public static RegistrierenController getInstance() {
        return registrierenControllerInstance;
    }

    /**
     * Funktion des "Zurück"-Buttons.
     * Lädt die Login-Szene. Eingaben werden verworfen.
     * @param event
     * @throws IOException
     */
    @FXML
    private void onZurück(ActionEvent event) throws IOException { // Zurück zu Login, Eingaben werden nicht übernommen
        try {
            // FXML laden
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/group06/cosmiceidex/FXMLFiles/Login.fxml")));
            // Aktuelle Stage laden
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            // Aktuelle Fenstergröße zwischenspeichern
            double width = stage.getWidth();
            double height = stage.getHeight();
            // Fenstergröße mitgeben und Szene wechseln
            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            stage.show();
        } catch (NullPointerException e) {
            showError("FXML Ladefehler", "Die FXML-Datei für den Login konnte nicht gefunden werden. Bitte Pfad prüfen.");
            e.printStackTrace();
        }
    }

    /**
     * Funktion des "Registrieren"-Buttons. Eingegebene Daten werden überprüft.
     * Wenn valide: Lädt die Login-Szene. Eingaben werden gespeichert.
     * @param event
     */
    @FXML
    private void onRegistrieren(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String repeatPassword = repeatPasswordField.getText();

        try {
            String[] areCredentialsValid = RegistrierenControllerLogic.validateCredentials(username, password, repeatPassword);
            if (areCredentialsValid == null) {
                User userCredentials = new User(username, password);
                Message msg = new Message(Message.MessageType.REGISTER_REQUEST, "SYSTEM", "SERVER", userCredentials);
                if (!client.connectionWithServerEstablished) {
                    try {
                        client.connectToServer(client.getServerIP(), client.getServerPort(), "SYSTEM");
                    }catch (Exception e){
                        showError("Verbindung Fehler","Fehler: Entweder Server ist offline oder es läuft auf gegebener IP und Port nicht!");
                        return;
                    }
                }
                client.sendMessage(msg);
            }
            else{
                showError(areCredentialsValid[0], areCredentialsValid[1]);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Anzeigen einer Fehlermeldung in GUI
     * @param title Titel der Fehlermeldung.
     * @param message Nachricht der Fehlermeldung.
     */
    public void showError(String title, String message) {
        usernameField.clear();
        passwordField.clear();
        repeatPasswordField.clear();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Anzeigen der erfolgreichen Registrierung.
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
     * Laden der Login-Szene.
     * @param stage
     */
    public void switchToLoginGUI(Stage stage){
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/group06/cosmiceidex/FXMLFiles/Login.fxml")));
            double width = stage.getWidth();
            double height = stage.getHeight();
            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            stage.show();
        }catch(IOException e){
            System.out.println("[ERROR] [" + client.getUsername() + "] " + "Failed to switch to Login GUI" );
        }
    }

    public RegistrierenController getRegistrierenControllerInstance(){
        return registrierenControllerInstance;
    }


    // Nur für testing verwenden
    public static void setMockRegistrierenControllerInstance(RegistrierenController mockController){
        registrierenControllerInstance = mockController;
    }
}