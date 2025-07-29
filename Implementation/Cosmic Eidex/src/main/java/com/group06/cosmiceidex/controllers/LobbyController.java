package com.group06.cosmiceidex.controllers;

import com.group06.cosmiceidex.client.Client;
import com.group06.cosmiceidex.common.Message;
import com.group06.cosmiceidex.common.LeaderboardEntry;
import com.group06.cosmiceidex.controllerlogic.SaveControllerVariables;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Dient dazu, die Funktionen der Szene Lobby zu implementieren.
 */

public class LobbyController implements Initializable {

    private Client client;
    private static LobbyController LobbyControllerInstance;

    // Formatierung für die Zeit
    DateTimeFormatter zeitFormat = DateTimeFormatter.ofPattern("HH:mm:ss");

    // Variablen aus fxml laden
    @FXML private TextField chatInputField;
    @FXML public VBox chatBox;
    @FXML public ScrollPane chatScrollPane;
    @FXML private Button absendenButton;

    @FXML private Label benutzernameLabel;

    @FXML private ScrollPane spielraumScrollPane;
    @FXML public VBox spielraumBox;
    @FXML private Button beitretenButton;

    @FXML private ScrollPane bestenlisteScrollPane;
    @FXML private VBox bestenlisteBox;

    @FXML private Label zuletztAktualisiertLabel;

    /**
     * Initialisiert die Lobby-Szene
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        SaveControllerVariables.setSelectedSpielraum(null);
        client = Client.getClientInstance();
        LobbyControllerInstance = this;
        if((client != null) && (client.getUsername() != null) && (!client.getUsername().equals("SYSTEM"))){
            benutzernameLabel.setText("Benutzername: " + client.getUsername());
        }
        bestenlisteLaden();
        Message request = new Message(Message.MessageType.ACTIVE_ROOMS_REQUEST, client.getUsername(), "SERVER", null);
        client.sendMessage(request);
        zuletztAktualisiertLabel.setText("Zuletzt aktualisiert: " + LocalTime.now().format(zeitFormat));

        // Wenn zu einem beliebigen Zeitpunkt "Enter" gedrückt wird und die Texteingabe fokussiert ist, Senden Button feuern
        chatInputField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                absendenButton.fire();
            }
        });
    }

    /**
     * Lädt die LobbyController-Instanz.
     * @return LobbyController-Instanz
     */
    public static LobbyController getInstance() {
        return LobbyControllerInstance;
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
            // Nachricht zu Chatbox hinzufügen
            Message msg = new Message(Message.MessageType.LOBBY_CHAT_REQUEST, client.getUsername(), message);
            client.sendMessage(msg);

            // Texteingabe wieder frei machen
            chatInputField.clear();
        }
    }

    /**
     * Funktion des "Ausloggen"-Buttons.
     * Lädt die Login-Szene. Der Benutzer wird ausgeloggt.
     * @param event
     * @throws IOException
     */
    @FXML
    private void onLogOut(ActionEvent event) throws IOException {
        // Benutzer ausloggen
        Message logOutMessage = new Message(Message.MessageType.LOGOUT_REQUEST, client.getUsername(), "SERVER", null);
        client.sendMessage(logOutMessage);
    }

    /**
     * Funktion des "Gesamte Bestenliste anzeigen"-Buttons.
     * Lädt die Bestenliste-Szene.
     * @param event
     * @throws IOException
     */
    @FXML
    private void onBestenliste(ActionEvent event) throws IOException {
        // prevScene auf Lobby
        SaveControllerVariables.setPrevScene("Lobby");

        // FXML laden
        Parent root = FXMLLoader.load(getClass().getResource("/com/group06/cosmiceidex/FXMLFiles/Bestenliste.fxml"));
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
     * Funktion des "Spielraum erstellen"-Buttons.
     * Lädt die SpielraumErstellen-Szene.
     * @param event
     * @throws IOException
     */
    @FXML
    private void onSpielraumErstellen(ActionEvent event) throws IOException {
        // FXML laden
        Parent root = FXMLLoader.load(getClass().getResource("/com/group06/cosmiceidex/FXMLFiles/SpielraumErstellen.fxml"));
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
     * Funktion des "Spielraum beitreten"-Buttons.
     * Lädt die Spielraumlogin-Szene.
     * Kann nur genutzt werden, wenn ein Spielraum ausgewählt ist. Sonst wird ein Fehler angezeigt.
     * @param event
     * @throws IOException
     */
    @FXML
    private void onSpielraumBeitreten(ActionEvent event) throws IOException {
        // Raumlogin soll nur geladen werden, wenn auch ein Raum ausgewählt wurde
        if (SaveControllerVariables.getSelectedSpielraum() != null){
            // FXML laden
            Parent root = FXMLLoader.load(getClass().getResource("/com/group06/cosmiceidex/FXMLFiles/SpielraumLogin.fxml"));
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
        else{
            // Fehler werfen, falls kein Raum ausgewählt
            showError("Kein Spielraum ausgewählt", "Es muss ein Spielraum ausgewählt sein!");
        }
    }

    /**
     * Funktion des "Nutzer-Einstellungen"-Buttons.
     * Lädt die Profileinstellungen-Szene.
     * @param event
     * @throws IOException
     */
    @FXML
    private void onProfileinstellungen(ActionEvent event) throws IOException {
        // FXML laden
        Parent root = FXMLLoader.load(getClass().getResource("/com/group06/cosmiceidex/FXMLFiles/Profileinstellungen.fxml"));
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
     * Funktion des "Spielregeln"-Buttons.
     * Lädt die Spielregeln-Szene.
     * @param event
     * @throws IOException
     */
    @FXML
    private void onSpielregeln(ActionEvent event) throws IOException {
        // FXML laden
        Parent root = FXMLLoader.load(getClass().getResource("/com/group06/cosmiceidex/FXMLFiles/Spielregeln.fxml"));
        // Aktuelle Stage laden
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        // Aktuellen Raum in prevRoom speichern
        SaveControllerVariables.setPrevScene("Lobby");

        // Aktuelle Fenstergröße zwischenspeichern
        double width = stage.getWidth();
        double height = stage.getHeight();

        // Fenstergröße mitgeben und Szene wechseln
        Scene scene = new Scene(root, width, height);
        stage.setScene(scene);
        stage.show();
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
     * Fügt Nachricht in GUI in Spielraumbox ein.
     * @param spielRaumName Name des Spielraums.
     */
    public void addSpielraumToSpielraumBox(String spielRaumName, String player1, String player2, String player3) {
        if(player1 == null){
            return;
        }
        if(player2 == null){
            player2 = "Platz verfügbar";
        }
        if(player3 == null){
            player3 = "Platz verfügbar";
        }
        // Spielraum erstellen
        Label spielRaum = new Label(spielRaumName + " -- AKTUELLE MITSPIELER :  " + player1 + " | " + player2 + " | " + player3 + " |");
        spielRaum.setId(spielRaumName);
        spielRaum.setMouseTransparent(false);
        spielRaum.setStyle("-fx-padding: 5 10 5 10;");
        // Funktion für Anklicken initialisieren
        // public variable SelectedSpielraum auf den aktuell ausgewählten Spielraum setzen
        spielRaum.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            SaveControllerVariables.setSelectedSpielraum(spielRaum.getId());

            // Alle Elemente transparent machen
            for (Node node : spielraumBox.getChildren()) {
                if (node instanceof Label) {
                    node.setStyle("-fx-padding: 5 10 5 10;");
                }
            }
            // Ausgewählten Raum hervorheben
            spielRaum.setStyle("-fx-background-color: lightblue; -fx-padding: 5 10 5 10;");
        });
        spielRaum.addEventHandler(MouseEvent.MOUSE_CLICKED, event ->{
            if(event.getClickCount() == 2){
                beitretenButton.fire();
            }
        });

        spielraumBox.getChildren().add(spielRaum);

        // Automatisch nach unten scrollen
        spielraumScrollPane.layout();
        spielraumScrollPane.setVvalue(1.0);
    }

    /**
     * Laden der Login-Szene.
     * @param stage
     */
    public void switchToLoginGUI(Stage stage){
        try {
            // FXML laden
            Parent root = FXMLLoader.load(getClass().getResource("/com/group06/cosmiceidex/FXMLFiles/Login.fxml"));
            // Aktuelle Fenstergröße zwischenspeichern
            double width = stage.getWidth();
            double height = stage.getHeight();
            // Fenstergröße mitgeben und Szene wechseln
            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            stage.show();
        }catch(IOException e){
            System.out.println("[ERROR] [" + client.getUsername() + "] " + "Failed to switch to Login GUI" );
        }
    }

    /**
     * Funktion des "Aktualisieren"-Buttons. Lädt die aktuelle Bestenliste.
     * @param event
     * @throws IOException
     */
    @FXML
    private void onAktualisieren(ActionEvent event) throws IOException{
        bestenlisteBox.getChildren().clear();
        bestenlisteLaden();
        zuletztAktualisiertLabel.setText("Zuletzt aktualisiert: " + LocalTime.now().format(zeitFormat));
    }

    /**
     * Lädt die aktuelle Bestenliste vom Server und lädt sie in das GUI.
     */
    private void bestenlisteLaden(){
        // VBox zuerst leeren, falls sie bereits Einträge enthält
        bestenlisteBox.getChildren().clear();

        // Bestenliste Anfrage
        Message request = new Message(Message.MessageType.LEADERBOARD_DATA_REQUEST, client.getUsername(), "SERVER", null);
        client.sendMessage(request);

    }


    public void representBestenliste(List<LeaderboardEntry> leaderboard){
        // Durch alle geladenen und sortierten Einträge iterieren
        for (int i = 0; i < leaderboard.size(); i++) {
            LeaderboardEntry entry = leaderboard.get(i);
            String text = String.format("%d. : %s | Siege: %d | Stiche: %d | Punkte: %d",
                    i + 1,
                    entry.getUserName(),
                    entry.getWins(),
                    entry.getTricks(),
                    entry.getPoints());

            Label benutzer = new Label(text);
            benutzer.setStyle("-fx-padding: 5 10 5 10;");

            bestenlisteBox.getChildren().add(benutzer);
        }
        bestenlisteScrollPane.layout();
        bestenlisteScrollPane.setVvalue(1.0);
    }
}
