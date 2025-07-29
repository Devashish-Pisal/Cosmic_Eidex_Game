package com.group06.cosmiceidex.controllers;

import com.group06.cosmiceidex.client.Client;
import com.group06.cosmiceidex.common.Message;
import com.group06.cosmiceidex.common.User; // Import für User-Objekt
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
import javafx.stage.Stage;
import javafx.scene.input.*;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Dient dazu, die Funktionen der Szene Login zu implementieren.
 */

public class LoginController implements Initializable {

    private static LoginController loginControllerInstance;
    private Client client;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Parent root;

    @FXML
    private Button loginButton;

    /**
     * Initialisiert die Login-Szene.
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.client = Client.getClientInstance();
        loginControllerInstance = this;

        // Wenn zu einem beliebigen Zeitpunkt "Enter" gedrückt wird, LogIn Button feuern
        root.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                loginButton.fire();
            }
        });
    }

    /**
     * Lädt die LoginController-Instanz.
     * @return LoginController-Instanz
     */
    public static LoginController getInstance() {
        return loginControllerInstance;
    }

    /**
     * Funktion des "Einloggen"-Buttons. Überprüft die eingegebenen Daten.
     * Wenn valide: Lädt die Lobby-Szene.
     * Wenn nicht valide: Fehlermeldung und Felder werden geleert.
     * @param event
     * @throws IOException
     */
    @FXML
    private void onLogIn(ActionEvent event){
        String username = usernameField.getText();
        String password = passwordField.getText();

        if(username == null || username.trim().isEmpty()){
            showError("Leere Benutzername", "Feld Benutzername darf nicht leer sein!");
            return;
        }
        if(password == null || password.trim().isEmpty()){
            showError("Leere Passwort", "Feld Passwort darf nicht leer sein!");
            return;
        }

            if(!client.connectionWithServerEstablished){
                try{
                    client.connectToServer(client.getServerIP(), client.getServerPort(), "SYSTEM");
                }catch (Exception e){
                    showError("Verbindung Fehler","Fehler: Entweder Server ist offline oder es läuft auf gegebener IP und Port nicht!");
                    return;
                }
            }
            User userCredentials = new User(username, password);
            Message msg = new Message(Message.MessageType.LOGIN_REQUEST, "SYSTEM", userCredentials);
            client.sendMessage(msg);
    }

    /**
     * Funktion des "Registrieren"-Buttons.
     * Lädt die Registrieren-Szene. Eingaben werden verworfen.
     * @param event
     * @throws IOException
     */
    @FXML
    private void onRegistrieren(ActionEvent event) throws IOException {
        try {
            // FXML laden
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/group06/cosmiceidex/FXMLFiles/Registrieren.fxml")));
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
            showError("FXML Ladefehler", "Die FXML-Datei für die Registrierung konnte nicht gefunden werden. Bitte Pfad prüfen.");
            e.printStackTrace();
        }
    }

    /**
     * Funktion des "Eigene IP-Adresse verwenden"-Buttons.
     * Lädt die IPKonfiguration-Szene. Eingaben werden verworfen.
     * @param event
     * @throws IOException
     */
    @FXML
    private void onCustomIP(ActionEvent event) throws IOException {
        try {
            // FXML laden
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/group06/cosmiceidex/FXMLFiles/IPKonfiguration.fxml")));
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
            showError("FXML Ladefehler", "Die FXML-Datei für die Registrierung konnte nicht gefunden werden. Bitte Pfad prüfen.");
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

    // Nur für testing verwenden
    public static void setMockLoginControllerInstance(LoginController mockController){
        loginControllerInstance = mockController;
    }
}