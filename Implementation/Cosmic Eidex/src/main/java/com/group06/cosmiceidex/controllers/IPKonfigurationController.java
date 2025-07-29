package com.group06.cosmiceidex.controllers;

import com.group06.cosmiceidex.client.Client;
import com.group06.cosmiceidex.controllerlogic.IPKonfigurationControllerLogic;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class IPKonfigurationController implements Initializable {

    private static IPKonfigurationController ipKonfigurationController;
    private Client client;

    @FXML
    private TextField ipField;

    @FXML
    private TextField portField;

    /**
     * Initialisiert IPKonfigurator-Szene
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.client = Client.getClientInstance();
        ipKonfigurationController = this;
    }

    /**
     * Ruft IPKonfiguratorController-Instanz auf
     * @return IPKonfiguratorController-Instanz
     */
    public static IPKonfigurationController getInstance() {
        return ipKonfigurationController;
    }

    /**
     * Funktion des "Zurück"-Buttons.
     * Lädt die Login-Szene. Eingaben werden verworfen.
     * @param event
     * @throws IOException
     */
    @FXML
    private void onZurück(ActionEvent event) throws IOException {
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
     * Funktion des "Anwenden"-Buttons.
     * Lädt die Login-Szene. Eingaben werden übernommen.
     * @param event
     * @throws IOException
     */
    @FXML
    private void onAnwenden(ActionEvent event) throws IOException {
            if(!ipField.getText().isEmpty() && !portField.getText().isEmpty()) {
                String ip = ipField.getText().trim();
                int port = Integer.parseInt(portField.getText().trim());
                if(!IPKonfigurationControllerLogic.isValidIP(ip)){
                    showError("IP Fehler", "Gegebene IP ist keine valide IP!");
                    return;
                } else if(!IPKonfigurationControllerLogic.isValidPort(port)){
                    showError("PORT Fehler", "Gegebene PORT ist kein valides PORT!");
                    return;
                }else{
                    client.setServerIP(ip);
                    client.setServerPort(port);
                    try{
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
            }else{
                showError("IP/PORT Konfigurationsfehler", "IP Feld oder Port Feld darf nicht leer sein!");
            }
    }

    /**
     * Anzeigen einer Fehlermeldung in GUI
     * @param title Titel der Fehlermeldung.
     * @param message Nachricht der Fehlermeldung.
     */
    public void showError(String title, String message) {
        ipField.clear();
        portField.clear();
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
}
