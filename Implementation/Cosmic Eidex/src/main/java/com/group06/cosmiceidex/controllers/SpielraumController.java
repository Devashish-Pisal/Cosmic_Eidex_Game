package com.group06.cosmiceidex.controllers;

import com.group06.cosmiceidex.controllerlogic.SpielraumControllerLogic;
import com.group06.cosmiceidex.client.Client;
import com.group06.cosmiceidex.common.Message;
import com.group06.cosmiceidex.controllerlogic.SaveControllerVariables;
import com.group06.cosmiceidex.game.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.tools.Tool;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dient dazu, die Funktionen der Szene Spielraum zu implementieren.
 */

public class SpielraumController implements Initializable {
    private Client client;
    private static SpielraumController SpielraumControllerInstance;
    private static GameSession session;
    private int cardsPlayer2;
    private int cardsPlayer3;
    private Map<ImageView, Tooltip> tooltipMap = new HashMap<>(); //Zum Zwischenspeichern der Tooltips
    // Die gewonnenen Karten sollen immer anzeigbar sein. Daher die direkte Initialisierung
    private Tooltip tooltipPlayer1WonCards = new Tooltip();
    private Tooltip tooltipPlayer2WonCards = new Tooltip();
    private Tooltip tooltipPlayer3WonCards = new Tooltip();

    @FXML private TextField chatInputField;
    @FXML private VBox chatBox;
    @FXML private ScrollPane chatScrollPane;
    @FXML private ScrollPane logScrollPane;
    @FXML private VBox logBox;
    @FXML private Button absendenButton;

    @FXML
    private ImageView player1card1;
    @FXML
    private ImageView player1card2;
    @FXML
    private ImageView player1card3;
    @FXML
    private ImageView player1card4;
    @FXML
    private ImageView player1card5;
    @FXML
    private ImageView player1card6;
    @FXML
    private ImageView player1card7;
    @FXML
    private ImageView player1card8;
    @FXML
    private ImageView player1card9;
    @FXML
    private ImageView player1card10;
    @FXML
    private ImageView player1card11;
    @FXML
    private ImageView player1card12;

    @FXML
    private ImageView player2card1;
    @FXML
    private ImageView player2card2;
    @FXML
    private ImageView player2card3;
    @FXML
    private ImageView player2card4;
    @FXML
    private ImageView player2card5;
    @FXML
    private ImageView player2card6;
    @FXML
    private ImageView player2card7;
    @FXML
    private ImageView player2card8;
    @FXML
    private ImageView player2card9;
    @FXML
    private ImageView player2card10;
    @FXML
    private ImageView player2card11;
    @FXML
    private ImageView player2card12;

    @FXML
    private ImageView player3card1;
    @FXML
    private ImageView player3card2;
    @FXML
    private ImageView player3card3;
    @FXML
    private ImageView player3card4;
    @FXML
    private ImageView player3card5;
    @FXML
    private ImageView player3card6;
    @FXML private ImageView player3card7;
    @FXML private ImageView player3card8;
    @FXML private ImageView player3card9;
    @FXML private ImageView player3card10;
    @FXML private ImageView player3card11;
    @FXML private ImageView player3card12;

    @FXML private ImageView player1pressedCard;
    @FXML private ImageView player2pressedCard;
    @FXML private ImageView player3pressedCard;

    @FXML private ImageView player1playedCard;
    @FXML private ImageView player2playedCard;
    @FXML private ImageView player3playedCard;

    @FXML private ImageView player1wonCard1;
    @FXML private ImageView player1wonCard2;
    @FXML private ImageView player1wonCard3;
    @FXML private ImageView player1wonCard4;
    @FXML private ImageView player1wonCard5;
    @FXML private ImageView player1wonCard6;

    @FXML private ImageView player2wonCard1;
    @FXML private ImageView player2wonCard2;
    @FXML private ImageView player2wonCard3;
    @FXML private ImageView player2wonCard4;
    @FXML private ImageView player2wonCard5;
    @FXML private ImageView player2wonCard6;
    @FXML private ImageView player2wonCard7;
    @FXML private ImageView player2wonCard8;
    @FXML private ImageView player2wonCard9;
    @FXML private ImageView player2wonCard10;
    @FXML private ImageView player2wonCard11;
    @FXML private ImageView player2wonCard12;
    @FXML private ImageView player2wonCard13;
    @FXML private ImageView player2wonCard14;
    @FXML private ImageView player2wonCard15;

    @FXML private ImageView player3wonCard1;
    @FXML private ImageView player3wonCard2;
    @FXML private ImageView player3wonCard3;
    @FXML private ImageView player3wonCard4;
    @FXML private ImageView player3wonCard5;
    @FXML private ImageView player3wonCard6;
    @FXML private ImageView player3wonCard7;
    @FXML private ImageView player3wonCard8;
    @FXML private ImageView player3wonCard9;
    @FXML private ImageView player3wonCard10;
    @FXML private ImageView player3wonCard11;
    @FXML private ImageView player3wonCard12;
    @FXML private ImageView player3wonCard13;
    @FXML private ImageView player3wonCard14;
    @FXML private ImageView player3wonCard15;

    @FXML private Label player1Name;
    @FXML private Label player1NameMitte;
    @FXML private Label player1NamePressedCard;

    @FXML private Label player2Name;
    @FXML private Label player2NameMitte;
    @FXML private Label player2NamePressedCard;

    @FXML private Label player3Name;
    @FXML private Label player3NameMitte;
    @FXML private Label player3NamePressedCard;

    @FXML private Pane player1wonCards;
    @FXML private Pane player2wonCards;
    @FXML private Pane player3wonCards;

    @FXML private Label player1WonPartyPoints;
    @FXML private Label player1SumOfTrickPoints;
    @FXML private Label player2WonPartyPoints;
    @FXML private Label player3WonPartyPoints;

    @FXML private VBox player1box;
    @FXML private VBox player2box;
    @FXML private VBox player3box;

    @FXML private Label gameTypePlaceholder;
    @FXML private Label trumpPlaceholder;

    /**
     * Initialisiert die SpielraumController-Szene
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        client = Client.getClientInstance();
        session = client.getSession();
        SpielraumControllerInstance = this;
        update();

        // Wenn zu einem beliebigen Zeitpunkt "Enter" gedrückt wird und die Texteingabe fokussiert ist, Senden Button feuern
        chatInputField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                absendenButton.fire();
            }
        });
    }

    /**
     * Aktualisiert Client GUI
     * @author Devashish Pisal
     */
    public void update(){
        session = client.getSession();

        updateAllLabels();
        updateAllCards();
        decideTurnAndCardLocks();
        addMessageToLogBox(session.getLogData());
        showResult();
    }

    /**
     * Aktualisiert alle Labels
     * @author Devashish Pisal
     */
    public void updateAllLabels(){
        List<PlayerInterface> playerList = new ArrayList<>();
        playerList.addAll(session.getPlayersWithUsernames().values());

        boolean player2IsSet = false;
        for(PlayerInterface player : playerList){
            if(player.getUsername().equals(client.getUsername())){
                setPlayerName("player1", player.getUsername());
                setGamePoints("player1", player.getWonPartyPoints());
                setSessionPoints("player1", player.getSumOfTrickPoints());
            }else if(!player2IsSet){
                setPlayerName("player2", player.getUsername());
                setGamePoints("player2", player.getWonPartyPoints());
                player2IsSet = true;
            }else{
                setPlayerName("player3", player.getUsername());
                setGamePoints("player3", player.getWonPartyPoints());
            }
        }
        setGameTypePlaceholder(session.getGameType().name());
        setTrumpPlaceholder(session.getGameType());
    }

    /**
     * Aktualisiert alle Karten
     * @author Devashish Pisal
     */
    public void updateAllCards(){
        updateCardsOfPlayer1();
        updateCardsOfPlayer2AndPlayer3();
        updateTrickCards();
        updateAllPressedCards();
        updateWinningStackCards();
    }

    /**
     * Auffüllen Hände der anderen Spieler mit den Rückseiten
     * @author Devashish Pisal
     */
    public void updateCardsOfPlayer2AndPlayer3(){
        List<Card> player2Hand = session.getPlayersWithUsernames().get(player2Name.getText()).getHand();
        List<Card> player3Hand = session.getPlayersWithUsernames().get(player3Name.getText()).getHand();
        if(player2Hand != null) {
            for (int i = 1; i <= 12; i++) {
                if(i > player2Hand.size()) {
                    getImageViewByString("player2card" + i).setImage(null);
                }else{
                    setImage(getImageViewByString("player2card" + i), "Back.png");
                }
            }
        }
        if(player3Hand != null) {
            for (int i = 1; i <= 12; i++) {
                if(i > player3Hand.size()) {
                    getImageViewByString("player3card" + i).setImage(null);
                }else{
                    setImage(getImageViewByString("player3card" + i), "Back.png");
                }
            }
        }
    }

    /**
     * Auffüllen Hand der Spieler mit Vorderseite
     * @author Devashish Pisal
     */
    public void updateCardsOfPlayer1(){
        List<Card> player1Hand = session.getPlayersWithUsernames().get(client.getUsername()).getHand();
        for(int i = 1; i<=12; i++) {
            if(i > player1Hand.size()){
                getImageViewByString("player1card" + i).setImage(null);
                Tooltip.uninstall(getImageViewByString("player1card" + i), tooltipMap.get("player1card" + i));
            }else {
                setImage(getImageViewByString("player1card" + i), player1Hand.get(i - 1).getImage());
                Tooltip tooltip = new Tooltip(player1Hand.get(i - 1).toString());
                tooltipMap.put(getImageViewByString("player1card" + i), tooltip);
                Tooltip.install(getImageViewByString("player1card" + i), tooltip);
            }
        }
    }

    /**
     * Aktualisiert gedruckte Karten
     * @author Devashish Pisal
     */
    public void updateAllPressedCards(){
        player1pressedCard.setImage(null);
        player2pressedCard.setImage(null);
        player3pressedCard.setImage(null);

        ConcurrentHashMap<String, Card> pressedCards = session.getPressedCards();
        for(Map.Entry<String, Card> entry : pressedCards.entrySet()){
            if(entry.getKey().equals(client.getUsername())){
                Card pressedCard = entry.getValue();
                setImage(player1pressedCard, pressedCard.getImage());
            }else if (entry.getKey().equals(player2Name.getText())){
                setImage(player2pressedCard, "Back.png");
            }else if(entry.getKey().equals(player3Name.getText())){
                setImage(player3pressedCard, "Back.png");
            }
        }
    }


    /**
     * Aktualisiert Stich Karten
     * @author Devashish Pisal
     */
    public void updateTrickCards(){
        player1playedCard.setImage(null);
        Tooltip.uninstall(player1playedCard, tooltipMap.get("player1playedCard"));
        player2playedCard.setImage(null);
        Tooltip.uninstall(player2playedCard, tooltipMap.get("player2playedCard"));
        player3playedCard.setImage(null);
        Tooltip.uninstall(player3playedCard, tooltipMap.get("player3playedCard"));

        ConcurrentHashMap<String, Card> currentTrickCards = session.getCurrentTrick();
        for(Map.Entry<String, Card> entry : currentTrickCards.entrySet()){
            if(entry.getKey().equals(client.getUsername())){
                Card playedCard = entry.getValue();
                setImage(player1playedCard, playedCard.getImage());

                Tooltip tooltip = new Tooltip(playedCard.toString());
                tooltipMap.put(player1playedCard, tooltip);
                Tooltip.install(player1playedCard, tooltip);
            }else if(entry.getKey().equals(player2Name.getText())){
                Card playedCard = entry.getValue();
                setImage(player2playedCard, playedCard.getImage());

                Tooltip tooltip = new Tooltip(playedCard.toString());
                tooltipMap.put(player2playedCard, tooltip);
                Tooltip.install(player2playedCard, tooltip);
            }else if(entry.getKey().equals(player3Name.getText())){
                Card playedCard = entry.getValue();
                setImage(player3playedCard, playedCard.getImage());

                Tooltip tooltip = new Tooltip(playedCard.toString());
                tooltipMap.put(player3playedCard, tooltip);
                Tooltip.install(player3playedCard, tooltip);
            }
        }
    }

    /**
     * Aktualisiert gewonnene Stich Karten
     * @author Devashish Pisal
     */
    public void updateWinningStackCards(){
        makeWinningStackEmpty();
        putCardsToWinningStack("player1", session.getWonTricksMapping().get(client.getUsername()));
        putCardsToWinningStack("player2", session.getWonTricksMapping().get(player2Name.getText()));
        putCardsToWinningStack("player3", session.getWonTricksMapping().get(player3Name.getText()));
    }


    /**
     * Entfernt alle gewonnene Stich Karten aus GUI
     * @author Devashish Pisal
     */
    public void makeWinningStackEmpty(){
        for(int i = 1; i<=15; i++){
            ImageView player1WinningStackCard = getImageViewByString("player1wonCard" + i);
            ImageView player2WinningStackCard  = getImageViewByString("player2wonCard" + i);
            ImageView player3WinningStackCard  = getImageViewByString("player3wonCard" + i);

            if(player1WinningStackCard != null){
                player1WinningStackCard.setImage(null);
            }
            player2WinningStackCard.setImage(null);
            player3WinningStackCard.setImage(null);
        }
        tooltipPlayer1WonCards.setText(null);
        tooltipPlayer2WonCards.setText(null);
        tooltipPlayer3WonCards.setText(null);

        tooltipPlayer1WonCards = new Tooltip();
        tooltipPlayer2WonCards = new Tooltip();
        tooltipPlayer3WonCards = new Tooltip();
    }

    /**
     * Anhand Benutzername der Spieler, der am Zug ist, Karten sperren oder freigeben.
     * Und hintergrund der Zug Spieler Gelb färben
     * @author Devashish Pisal
     */
    public void decideTurnAndCardLocks(){
        if(!session.getUsernameOfPlayerOnTurn().equals(client.getUsername())){
            lockAllCards();
        }else {
            unlockAllCards();
        }
        showTurn(session.getUsernameOfPlayerOnTurn());
    }


    /**
     * Spieler Karten sperren
     * @author Devashish Pisal
     */
    public void lockAllCards(){
        for(int i = 1; i<= 12; i++){
            getImageViewByString("player1card" + i).setDisable(true);
        }
    }

    /**
     * Alle Spieler Karten freigeben, um anzuklicken
     * @author Devashish Pisal
     */
    public void unlockAllCards(){
        for(int i = 1; i<= 12; i++){
            getImageViewByString("player1card" + i).setDisable(false);
        }
    }


    /**
     * Funktion des "SpielBeenden"-Buttons → Testphase, wird später durch Spielende ausgelöst.
     * Lädt die Gewinnerboard-Szene.
     * @param event
     * @throws IOException
     */
    @FXML
    private void onSpielBeenden(ActionEvent event) throws IOException {
        session = Client.getClientInstance().getSession();

        List<String> usersExceptQuitter = new ArrayList<>(session.getUsernames());
        usersExceptQuitter.remove(client.getUsername());

        PlayerInterface player1 = session.getPlayersWithUsernames().get(usersExceptQuitter.get(0));
        PlayerInterface player2 = session.getPlayersWithUsernames().get(usersExceptQuitter.get(1));

        HashMap<Integer, PlayerInterface> res = SpielraumControllerLogic.computeResults(player1, player2);
        res.put(3,session.getPlayersWithUsernames().get(client.getUsername())); // quitter is 3rd

        session.setGameState(GameSession.GameState.GAME_OVER);
        session.setGameResult(res);

        Message request = new Message(Message.MessageType.UPDATE_REQUEST, client.getUsername(), client.getCurrentRoom(), session);
        client.sendMessage(request);

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
        SaveControllerVariables.setPrevScene("Spielraum");

        // Aktuelle Fenstergröße zwischenspeichern
        double width = stage.getWidth();
        double height = stage.getHeight();

        // Fenstergröße mitgeben und Szene wechseln
        Scene scene = new Scene(root, width, height);
        stage.setScene(scene);
        stage.show();
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
            Message msg = new Message(Message.MessageType.GAME_ROOM_CHAT_REQUEST, client.getUsername(),client.getCurrentRoom(), message);
            client.sendMessage(msg);

            // Texteingabe wieder frei machen
            chatInputField.clear();
        }
    }

    /**
     * Wechselt die Szene zur Bestenliste.
     */
    @FXML
    private void onBestenliste(ActionEvent event) throws IOException {
        // prevScene auf Spielraum setzen
        SaveControllerVariables.setPrevScene("Spielraum");

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
     * Wechselt die Szene zur Statistik.
     */
    @FXML
    private void onStatistik(ActionEvent event) throws IOException {
        // prevScene auf Spielraum setzen
        SaveControllerVariables.setPrevScene("Spielraum");

        // FXML laden
        Parent root = FXMLLoader.load(getClass().getResource("/com/group06/cosmiceidex/FXMLFiles/Statistik.fxml"));
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
     * Fügt Nachricht in GUI in LogBox ein.
     * @param logData Alle Log Nachrichten.
     */
    public void addMessageToLogBox(List<String> logData) {
        logBox.getChildren().clear();
        for (String message : logData) {
            Text messageText = new Text(message);
            messageText.wrappingWidthProperty().bind(chatScrollPane.widthProperty().subtract(30));
            logBox.getChildren().add(messageText);
        }
        // Automatisch nach unten scrollen
        logScrollPane.layout();
        logScrollPane.setVvalue(1.0);
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
     * Setzt das Bild für eine bestimmte ImageView.
     * @param imageView das ImageView Feld, in das das Bild eingefügt werden soll
     * @param imageSrc die Adresse des Bildes
     */
    private void setImage(ImageView imageView, String imageSrc){
        Image image = new Image(getClass().getResource("/com/group06/cosmiceidex/images/cards/" + imageSrc).toExternalForm());
        imageView.setImage(image);
    }

    /**
     * Setzt den Nutzernamen für die verschiedenen Nutzernamen-Labels
     * @param player Der Spielerslot, der von dem Nutzer belegt wird.
     * @param username Der anzuzeigende Nutzername.
     */
    private void setPlayerName(String player, String username){
        try{
            if (Objects.equals(player, "player1")){
                player1Name.setText(username + "(Du)");
                player1NameMitte.setText(username + "(Du)");
                player1NamePressedCard.setText(username + "(Du)");
            } else if (Objects.equals(player, "player2")) {
                player2Name.setText(username);
                player2NameMitte.setText(username);
                player2NamePressedCard.setText(username);
            } else if (Objects.equals(player, "player3")) {
                player3Name.setText(username);
                player3NameMitte.setText(username);
                player3NamePressedCard.setText(username);
            } else {
                System.out.println("[ERROR] Ungültiger Spieler in Spielraum ausgewählt");
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Ungültiger Spieler in Spielraum ausgewählt");
            e.printStackTrace();
        }
    }

    /**
     * Setzt die Gewinnpunkte eines Spielers.
     * @param player Der betroffene Spieler.
     * @param points Die anzuzeigenden Punkte.
     */
    private void setGamePoints(String player, int points){
        try{
            if (Objects.equals(player, "player1")){
                player1WonPartyPoints.setText("" + points);
            } else if (Objects.equals(player, "player2")) {
                player2WonPartyPoints.setText("" + points);
            } else if (Objects.equals(player, "player3")) {
                player3WonPartyPoints.setText("" + points);
            } else {
                System.out.println("[ERROR] Ungültiger Spieler bei Gewinnpunkten ausgewählt");
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Ungültiger Spieler bei Gewinnpunkten ausgewählt");
            e.printStackTrace();
        }
    }

    /**
     * Zeigt die Punkte in der aktuellen Runde
     * @param player der gewünschte Spieler
     * @param points die zu setzende Anzahl an Punkten
     */
    private void setSessionPoints(String player, int points){
        try{
            if (Objects.equals(player, "player1")){
                player1SumOfTrickPoints.setText("" + points);
            } else {
                System.out.println("[ERROR] Ungültiger Spieler bei Spielpunkten ausgewählt");
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Ungültiger Spieler bei Spielpunkten ausgewählt");
            e.printStackTrace();
        }
    }


    /**
     * Unterscheidet zwischen Spiel Phasen und ruft entsprechende Methode, wenn Spieler auf einer Karte anklickt.
     * @param cardNumber GUI Index von geklickt Karte (von 1,..,12)
     * @param playedCard ImageView Objekt von geklickte Karte
     */
    public void cardClicked(int cardNumber, ImageView playedCard){
        if(session.getGameState() == GameSession.GameState.FACE_DOWN_PHASE){
            session.faceDownCard(client.getUsername(), cardNumber);
            Message request = new Message(Message.MessageType.UPDATE_REQUEST, client.getUsername(), client.getCurrentRoom(), session);
            client.sendMessage(request);
        }else if(session.getGameState() == GameSession.GameState.PLAYING_CARDS){
            session.playCard(client.getUsername(), cardNumber);
            Message request = new Message(Message.MessageType.UPDATE_REQUEST, client.getUsername(), client.getCurrentRoom(), session);
            client.sendMessage(request);
        }
    }

    /**
     * Funktion, wenn Karte 1 angeklickt wurde
     */
    @FXML
    public void cardClicked1(){
        if(player1card1.getImage()!=null){
            cardClicked(1, player1card1);
        }
    }
    /**
     * Funktion, wenn Karte 2 angeklickt wurde
     */
    @FXML
    public void cardClicked2(){
        if (player1card2.getImage() != null) {
            cardClicked(2, player1card2);
        }
    }
    /**
     * Funktion, wenn Karte 3 angeklickt wurde
     */
    @FXML
    public void cardClicked3(){
        if (player1card3.getImage() != null) {
            cardClicked(3, player1card3);
        }
    }
    /**
     * Funktion, wenn Karte 4 angeklickt wurde
     */
    @FXML
    public void cardClicked4(){
        if (player1card4.getImage() != null) {
            cardClicked(4, player1card4);
        }
    }
    /**
     * Funktion, wenn Karte 5 angeklickt wurde
     */
    @FXML
    public void cardClicked5(){
        if (player1card5.getImage() != null) {
            cardClicked(5, player1card5);
        }
    }
    /**
     * Funktion, wenn Karte 6 angeklickt wurde
     */
    @FXML
    public void cardClicked6(){
        if (player1card6.getImage() != null) {
            cardClicked(6, player1card6);
        }
    }
    /**
     * Funktion, wenn Karte 7 angeklickt wurde
     */
    @FXML
    public void cardClicked7(){
        if (player1card7.getImage() != null) {
            cardClicked(7, player1card7);
        }
    }
    /**
     * Funktion, wenn Karte 8 angeklickt wurde
     */
    @FXML
    public void cardClicked8(){
        if (player1card8.getImage() != null) {
            cardClicked(8, player1card8);
        }
    }
    /**
     * Funktion, wenn Karte 9 angeklickt wurde
     */
    @FXML
    public void cardClicked9(){
        if (player1card9.getImage() != null) {
            cardClicked(9, player1card9);
        }
    }
    /**
     * Funktion, wenn Karte 10 angeklickt wurde
     */
    @FXML
    public void cardClicked10(){
        if (player1card10.getImage() != null) {
            cardClicked(10, player1card10);
        }
    }
    /**
     * Funktion, wenn Karte 11 angeklickt wurde
     */
    @FXML
    public void cardClicked11(){
        if (player1card11.getImage() != null) {
            cardClicked(11, player1card11);
        }
    }
    /**
     * Funktion, wenn Karte 12 angeklickt wurde
     */
    @FXML
    public void cardClicked12(){
        if (player1card12.getImage() != null) {
            cardClicked(12, player1card12);
        }
    }

    /* Ungenutzt
    /**
     * Funktion zum Ablegen der Karten von anderen Spielern
     * @param player Der legende Spieler ("player2" oder "player3")
     * @param card Die gespielte Karte (z.B. "Eidex6.png")
     * @param trump Boolean, ob die gespielte Karte die gedrückte Karte ist
     *//*
    public void cardPlayedByOtherPlayer(String player, String card, boolean trump){
        if(trump){
            if(player == "player2"){
                setImage(player2pressedCard, "Back.png");
            }
            else if(player == "player3"){
                setImage(player3pressedCard, "Back.png");
            }
            else{
                System.out.println("Falsche Eingabe als Spieler \n player2 oder player3 sind legale Ausdrücke.");
            }
        }
        else{
            if(player == "player2"){
                setImage(player2playedCard, card);
            }
            else if(player == "player3"){
                setImage(player3playedCard, card);
            }
            else{
                System.out.println("Falsche Eingabe als Spieler \n player2 oder player3 sind legale Ausdrücke.");
            }
        }
        removeCardFromPlayer(player);
    }
    */

    /* ungenutzt
    /**
     * Entfernt die hinterste Karte aus der Hand eines Gegenspielers, der eine Karte in der Spielmitte platziert
     * @param player Der ausspielende Gegenspieler
     *//*
    public void removeCardFromPlayer(String player){
        if (player == "player2"){
            getImageViewByString(player + "card" + cardsPlayer2).setImage(null);
            cardsPlayer2--;
        }
        else if(player == "player3"){
            getImageViewByString(player + "card" + cardsPlayer3).setImage(null);
            cardsPlayer3--;
        }
        else{
            System.out.println("Falsche Eingabe als Spieler \n player2 oder player3 sind legale Ausdrücke.");
        }
    }*/

    /**
     * Zeigt den gewonnenen eines Spielers an.
     *
     * @param player Der anzuzeigende Spieler.
     * @param lists
     */
    public void putCardsToWinningStack(String player, List<List<Card>> lists){
        if (player == "player1" && lists != null){
            int countedCards = 0;
            for(int i = lists.size()-1; i >= 0; i--){
                if(lists.get(i) != null){
                    for(int j = 0; j < 3; j++){
                        if(countedCards < 6){
                            ImageView imageView = getImageViewByString("player1wonCard" + (countedCards + 1));
                            setImage(imageView, lists.get(i).get(j).getImage());
                            String currString = tooltipPlayer1WonCards.getText();
                            countedCards++;
                        }
                        // Karte zu Tooltip hinzufügen -> In Tooltip sollen alle gewonnenen Karten sichtbar sein
                        String currString = tooltipPlayer1WonCards.getText();
                        if(currString.isEmpty()){
                            tooltipPlayer1WonCards.setText(lists.get(i).get(j).toString());
                        }
                        else{
                            tooltipPlayer1WonCards.setText(currString+ "\n" + lists.get(i).get(j).toString());
                        }
                        Tooltip.install(player1wonCards, tooltipPlayer1WonCards);
                    }
                }
            }
        }
        else if(player == "player2" && lists != null){
            int countedCards = 0;
            for(int i = lists.size()-1; i >= 0; i--){
                if(lists.get(i) != null){
                    for(int j = 0; j < 3; j++){
                        if(countedCards < 15){
                            ImageView imageView = getImageViewByString("player2wonCard" + (countedCards + 1));
                            setImage(imageView, lists.get(i).get(j).getImage());
                            countedCards++;
                        }
                        // Karte zu Tooltip hinzufügen -> In Tooltip sollen alle gewonnenen Karten sichtbar sein
                        String currString = tooltipPlayer2WonCards.getText();
                        if(currString.isEmpty()){
                            tooltipPlayer2WonCards.setText(lists.get(i).get(j).toString());
                        }
                        else{
                            tooltipPlayer2WonCards.setText(currString+ "\n" + lists.get(i).get(j).toString());
                        }
                        Tooltip.install(player2wonCards, tooltipPlayer2WonCards);
                    }
                }
            }
        }
        else if(player == "player3" && lists != null){
            int countedCards = 0;
            for(int i = lists.size()-1; i >= 0; i--){
                if(lists.get(i) != null){
                    for(int j = 0; j < 3; j++){
                        if(countedCards < 15){
                            ImageView imageView = getImageViewByString("player3wonCard" + (countedCards + 1));
                            setImage(imageView, lists.get(i).get(j).getImage());
                            countedCards++;
                        }
                        // Karte zu Tooltip hinzufügen -> In Tooltip sollen alle gewonnenen Karten sichtbar sein
                        String currString = tooltipPlayer3WonCards.getText();
                        if(currString.isEmpty()){
                            tooltipPlayer3WonCards.setText(lists.get(i).get(j).toString());
                        }
                        else{
                            tooltipPlayer3WonCards.setText(currString+ "\n" + lists.get(i).get(j).toString());
                        }
                        Tooltip.install(player3wonCards, tooltipPlayer3WonCards);
                    }
                }
            }
        }
    }

    /**
     * Zeigt grafisch, welcher Spieler am Zug ist
     * @param player Der Spieler, der am Zug ist
     */
    public void showTurn(String player){
        if ((player + "(Du)" ).equals(player1Name.getText())){
            player1box.setStyle("-fx-background-color: yellow; -fx-border-color: black;");
            player2box.setStyle("-fx-border-color: black;");
            player3box.setStyle("-fx-border-color: black;");
        }
        else if(player.equals(player2Name.getText())){
            player1box.setStyle("-fx-border-color: black;");
            player2box.setStyle("-fx-background-color: yellow; -fx-border-color: black;");
            player3box.setStyle("-fx-border-color: black;");
        }
        else if(player.equals(player3Name.getText())){
            player1box.setStyle("-fx-border-color: black;");
            player2box.setStyle("-fx-border-color: black;");
            player3box.setStyle("-fx-background-color: yellow; -fx-border-color: black;");
        }
        else{
            System.out.println("Falsche Eingabe als Spieler. \nEs muss der korrekte Spielername eingegeben werden.");
        }
    }

    /**
     * Zeigt den Spielmodus an
     * @param gametype Der aktuelle Spielmodus
     */
    private void setGameTypePlaceholder(String gametype){
        gameTypePlaceholder.setText(gametype);
    }

    /**
     * Zeigt die aktuelle Trumpffarbe an
     * @param type Typ dem Spiel
     */
    private void setTrumpPlaceholder(GameType type){
        if(type == GameType.Obenabe || type == GameType.Undenuf){
            trumpPlaceholder.setText("Kein Trumpf");
        }else {
            trumpPlaceholder.setText(session.getTrumpCard().getColour() + "-" + session.getTrumpCard().getValue());
        }
    }


    /**
     * Wechselt von Spielraum GUI zu Gewinnerboard GUI
     * @author Devashish Pisal
     */
    public void showResult() {
        if(session.getGameState() == GameSession.GameState.GAME_OVER){
            client.setLastMatchResult(session.getGameResult());
            try{
                Parent root = FXMLLoader.load(getClass().getResource("/com/group06/cosmiceidex/FXMLFiles/Gewinnerboard.fxml"));
                double width = client.getPrimaryStage().getWidth();
                double height = client.getPrimaryStage().getHeight();
                Scene scene = new Scene(root, width, height);
                client.getPrimaryStage().setScene(scene);
                client.getPrimaryStage().show();
            }catch(IOException e){
                System.out.println("[ERROR] [" + client.getUsername() + "] " + "Failed to switch to Gewinnerboard GUI" );
            }
        }
    }


    /**
     * Gibt das Imageview Objekt zu einem String zurück
     * @param imageView die gesuchte ImageView als String-Eingabe
     * @return die gesuchte ImageView
     */
    public ImageView getImageViewByString(String imageView){
        switch (imageView){
            case "player1card1" -> {return player1card1;}
            case "player1card2" -> {return player1card2;}
            case "player1card3" -> {return player1card3;}
            case "player1card4" -> {return player1card4;}
            case "player1card5" -> {return player1card5;}
            case "player1card6" -> {return player1card6;}
            case "player1card7" -> {return player1card7;}
            case "player1card8" -> {return player1card8;}
            case "player1card9" -> {return player1card9;}
            case "player1card10" -> {return player1card10;}
            case "player1card11" -> {return player1card11;}
            case "player1card12" -> {return player1card12;}

            case "player2card1" -> {return player2card1;}
            case "player2card2" -> {return player2card2;}
            case "player2card3" -> {return player2card3;}
            case "player2card4" -> {return player2card4;}
            case "player2card5" -> {return player2card5;}
            case "player2card6" -> {return player2card6;}
            case "player2card7" -> {return player2card7;}
            case "player2card8" -> {return player2card8;}
            case "player2card9" -> {return player2card9;}
            case "player2card10" -> {return player2card10;}
            case "player2card11" -> {return player2card11;}
            case "player2card12" -> {return player2card12;}

            case "player3card1" -> {return player3card1;}
            case "player3card2" -> {return player3card2;}
            case "player3card3" -> {return player3card3;}
            case "player3card4" -> {return player3card4;}
            case "player3card5" -> {return player3card5;}
            case "player3card6" -> {return player3card6;}
            case "player3card7" -> {return player3card7;}
            case "player3card8" -> {return player3card8;}
            case "player3card9" -> {return player3card9;}
            case "player3card10" -> {return player3card10;}
            case "player3card11" -> {return player3card11;}
            case "player3card12" -> {return player3card12;}

            case "player1pressedCard" -> {return player1pressedCard;}
            case "player2pressedCard" -> {return player2pressedCard;}
            case "player3pressedCard" -> {return player3pressedCard;}

            case "player1playedCard" -> {return player1playedCard;}
            case "player2playedCard" -> {return player2playedCard;}
            case "player3playedCard" -> {return player3playedCard;}

            case "player1wonCard1" -> {return player1wonCard1;}
            case "player1wonCard2" -> {return player1wonCard2;}
            case "player1wonCard3" -> {return player1wonCard3;}
            case "player1wonCard4" -> {return player1wonCard4;}
            case "player1wonCard5" -> {return player1wonCard5;}
            case "player1wonCard6" -> {return player1wonCard6;}

            case "player2wonCard1" -> {return player2wonCard1;}
            case "player2wonCard2" -> {return player2wonCard2;}
            case "player2wonCard3" -> {return player2wonCard3;}
            case "player2wonCard4" -> {return player2wonCard4;}
            case "player2wonCard5" -> {return player2wonCard5;}
            case "player2wonCard6" -> {return player2wonCard6;}
            case "player2wonCard7" -> {return player2wonCard7;}
            case "player2wonCard8" -> {return player2wonCard8;}
            case "player2wonCard9" -> {return player2wonCard9;}
            case "player2wonCard10" -> {return player2wonCard10;}
            case "player2wonCard11" -> {return player2wonCard11;}
            case "player2wonCard12" -> {return player2wonCard12;}
            case "player2wonCard13" -> {return player2wonCard13;}
            case "player2wonCard14" -> {return player2wonCard14;}
            case "player2wonCard15" -> {return player2wonCard15;}

            case "player3wonCard1" -> {return player3wonCard1;}
            case "player3wonCard2" -> {return player3wonCard2;}
            case "player3wonCard3" -> {return player3wonCard3;}
            case "player3wonCard4" -> {return player3wonCard4;}
            case "player3wonCard5" -> {return player3wonCard5;}
            case "player3wonCard6" -> {return player3wonCard6;}
            case "player3wonCard7" -> {return player3wonCard7;}
            case "player3wonCard8" -> {return player3wonCard8;}
            case "player3wonCard9" -> {return player3wonCard9;}
            case "player3wonCard10" -> {return player3wonCard10;}
            case "player3wonCard11" -> {return player3wonCard11;}
            case "player3wonCard12" -> {return player3wonCard12;}
            case "player3wonCard13" -> {return player3wonCard13;}
            case "player3wonCard14" -> {return player3wonCard14;}
            case "player3wonCard15" -> {return player3wonCard15;}

            default -> {return null;}
        }
    }

    public static SpielraumController getSpielraumControllerInstance() {
        return SpielraumControllerInstance;
    }
}
