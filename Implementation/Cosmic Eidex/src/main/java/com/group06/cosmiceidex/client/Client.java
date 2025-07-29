package com.group06.cosmiceidex.client;

import com.group06.cosmiceidex.common.LeaderboardEntry;
import com.group06.cosmiceidex.common.Message;
import com.group06.cosmiceidex.controllers.*;
import com.group06.cosmiceidex.game.GameSession;
import com.group06.cosmiceidex.game.PlayerInterface;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;

/**
 * Die Client-Klasse stellt einen Client im Spiel dar.
 * Client-Klasse verwaltet die Verbindung mit dem Server und die Kommunikation zwischen Client und Server.
 * Klasse schickt Anfragen zum Server und verwaltet Server Antworten
 * @author Devashish Pisal
 */
public class Client{

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String serverIP = "127.0.0.1";
    private int serverPort = 1234;
    private static Client clientInstance;
    private String username;
    private String currentRoom = "LOBBY";
    private boolean isGameMaster = false;
    private volatile boolean stopFlag = false;
    private Thread listenThread;
    public boolean connectionWithServerEstablished = false;
    private Stage primaryStage;
    private static GameSession session;
    private HashMap<Integer, PlayerInterface> lastMatchResult;

    /**
     * Konstrukteur für Client Objekt
     * @author Devashish Pisal
     */
    private Client(){}

    /**
     * Gibt einzige Instanz der Client-Klasse zurück.
     * Falls bisher noch keine Instanz existiert, wird eine neue Instanz erstellt.
     * @return Einzige Client-Instanz
     * @author Devashish Pisal
     */
    public static Client getClientInstance(){
        if(clientInstance == null){
            clientInstance = new Client();
        }
        return clientInstance;
    }

    /**
     * Stellt eine Verbindung zum Server über ein Socket her und initialisiert die Kommunikationskanäle.
     * Startet einen Thread zum Empfangen von Nachrichten vom Server.
     * @param host Die IP-Adresse oder der Hostname des Servers (darf nicht null sein)
     * @param port Der Port, auf dem der Server läuft (darf nicht null sein)
     * @param username Der Benutzername für die Verbindung (darf nicht null sein)
     * @author Devashish Pisal
     */
    public void connectToServer(String host, int port, String username) throws IOException {
        try{
            if(socket == null) {
                socket = new Socket();
                socket.connect(new InetSocketAddress(host,port), 1000); // timeout verringern
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
            }
            this.username = username;
            connectionWithServerEstablished = true;
            System.out.println("[INFO] [" + username + "] Connected to server on IP Address: " + host + " and PORT:" + port);

            // listenForMessages Methode auf andere Thread laufen lassen.
            stopFlag = false;
            listenThread = new Thread(this::listenForMessages);
            listenThread.start();

        }catch(Exception e){
            System.out.println("[ERROR] [" + username + "] Unable to connect to server on IP Address: " + host + " and PORT:" + port);
            socket = null;
            throw e;
        }
    }

    /**
     * Sendet eine Nachricht/Objekt an den Server über den Object Output stream.
     * @param obj Das zu Server sendende Objekt (muss serialisierbar sein und darf nicht null sein)
     * @author Devashish Pisal
     */
    public void sendMessage(Object obj){
        try{
            out.writeObject(obj);
            out.flush();
            if(obj instanceof Message){
                Message msg = (Message) obj;
                System.out.println("[INFO] [" + username + "] " + msg.getType() + " Message sent to clientHandler");
            }
        } catch (IOException e) {
            System.out.println("[ERROR] [" + username + "] Unable to send message to clientHandler");
            e.printStackTrace();
        }
    }

    /**
     * Kriegt Nachrichten vom Server in einem separaten Thread und leitet der Nachricht zu handleServerMessage() weiter.
     * Wenn das stopFlag auf true gesetzt wird, wird der Thread beendet.
     * @author Devashish Pisal
     */
    private void listenForMessages() {
        try {
            while (!stopFlag) {
                Object message = in.readObject();
                if (message instanceof Message) {
                    Message msg = (Message) message;
                    System.out.println("[INFO] [" + username + "] " + msg.getType() + " Message received");
                    handleServerMessage(msg);
                }
            }
        } catch (Exception e) {
            if (!stopFlag) {
                System.out.println("[ERROR] [" + username + "] Something went wrong while listening for messages from clientHandler");
                e.printStackTrace();
            }
        }
    }

    /**
     * Klassifiziert die einkommenden Nachrichten vom Server und leitet sie an entsprechende Methoden.
     * Die Nachrichten werden basierend auf dem Typ kategorisiert und verarbeitet.
     * @param message Die empfangene Nachricht vom Server.
     * @see Message
     * @author Devashish Pisal
     */
    public void handleServerMessage(Object message){
        if(message instanceof Message){
            Message msg = (Message) message;
            Message.MessageType messageType = msg.getType();

            Set<Message.MessageType> authenticationMessageTypes = EnumSet.of(Message.MessageType.LOGIN_RESPONSE, Message.MessageType.LOGIN_ERROR,
                    Message.MessageType.REGISTER_RESPONSE, Message.MessageType.REGISTER_ERROR);
            Set<Message.MessageType> lobbyManagementMessageTypes = EnumSet.of(Message.MessageType.LOGGED_IN_USERS_RESPONSE, Message.MessageType.LOBBY_CHAT_RESPONSE,
                    Message.MessageType.LEADERBOARD_DATA_RESPONSE, Message.MessageType.COMPLETE_LEADERBOARD_RESPONSE, Message.MessageType.LOGOUT_RESPONSE,
                    Message.MessageType.ACTIVE_ROOMS_RESPONSE, Message.MessageType.CHANGE_PASSWORD_ERROR, Message.MessageType.CHANGE_PASSWORD_RESPONSE);
            Set<Message.MessageType> roomManagementMessageTypes = EnumSet.of(Message.MessageType.CREATE_ROOM_RESPONSE, Message.MessageType.JOIN_ROOM_RESPONSE,
                    Message.MessageType.LEAVE_ROOM_RESPONSE, Message.MessageType.ROOM_INFO_RESPONSE, Message.MessageType.WAITING_ROOM_CHAT_RESPONSE,
                    Message.MessageType.CREATE_ROOM_ERROR, Message.MessageType.JOIN_ROOM_ERROR, Message.MessageType.ROOM_PARTICIPANTS_RESPONSE,
                    Message.MessageType.START_GAME_RESPONSE, Message.MessageType.GAME_ROOM_CHAT_RESPONSE);
            Set<Message.MessageType> gameLogicMessageTypes = EnumSet.of( Message.MessageType.UPDATE_RESPONSE, Message.MessageType.INVALID_MOVE,  Message.MessageType.GAME_UPDATE);

            if(authenticationMessageTypes.contains(messageType)){
                handleAuthenticationMessage(msg);
            }else if(lobbyManagementMessageTypes.contains(messageType)){
                handleLobbyManagementMessage(msg);
            }else if(roomManagementMessageTypes.contains(messageType)){
                handleRoomManagementMessage(msg);
            }else if(gameLogicMessageTypes.contains(messageType)){
                handleGameLogicMessage(msg);
            }
        }
    }

    /**
     * Verarbeitet Authentifizierungsnachrichten vom Server.
     * Behandelt Login-, Registrierungsantworten und entsprechende Fehlermeldungen.
     * @param msg Authentifizierungsnachricht
     * @see Message
     * @author Devashish Pisal
     */
    public void handleAuthenticationMessage(Message msg){
        if(msg.getType() == Message.MessageType.LOGIN_RESPONSE && msg.getPayload().equals("LOGGED-IN-SUCCESSFULLY")){
            this.username = msg.getTarget();
            Platform.runLater(()->{
                LoginController controller = LoginController.getInstance();
                controller.switchToLobbyGUI(primaryStage);
            });
        }else if(msg.getType() == Message.MessageType.LOGIN_ERROR){
            Platform.runLater(()->{
                LoginController controller = LoginController.getInstance();;
                controller.showError("Login Fehler", (String) msg.getPayload());
            });
        }else if(msg.getType() == Message.MessageType.REGISTER_ERROR){
            Platform.runLater(()->{
                RegistrierenController controller = RegistrierenController.getInstance();
                controller.showError("Registrierung Fehler", (String) msg.getPayload());
            });
        }else if(msg.getType() == Message.MessageType.REGISTER_RESPONSE){
            Platform.runLater(()->{
                RegistrierenController controller = RegistrierenController.getInstance();
                controller.showSuccess("Registrierung erfolgreich", "Benutzer wurde erfolgreich registriert. Sie können sich nun einloggen.");
                controller.switchToLoginGUI(primaryStage);
            });
        }
    }

    /**
     * Verarbeitet Lobby-Management-Nachrichten vom Server.
     * Behandelt Bestenliste-, Chat- und Logout-Server Antworten.
     * @param msg Lobby-Management-Nachricht
     * @see Message
     * @author Devashish Pisal
     */
    public void handleLobbyManagementMessage(Message msg){
        if(msg.getType() == Message.MessageType.LOBBY_CHAT_RESPONSE){
            Platform.runLater(()->{
                LobbyController controller = LobbyController.getInstance();
                controller.addMessageToChatBox(msg.getSender(), (String) msg.getPayload());
            });
        }else if(msg.getType() == Message.MessageType.LOGOUT_RESPONSE){
            this.username = null;
            this.currentRoom = "LOBBY";
            this.connectionWithServerEstablished = false;
            this.stopFlag = true;
            handleDisconnect();
        }else if(msg.getType() == Message.MessageType.LEADERBOARD_DATA_RESPONSE){
            Platform.runLater(()->{
                LobbyController controller = LobbyController.getInstance();
                List<LeaderboardEntry> list = (List<LeaderboardEntry>) msg.getPayload();
                controller.representBestenliste(list);
            });
        }else if(msg.getType() == Message.MessageType.COMPLETE_LEADERBOARD_RESPONSE){
            Platform.runLater(()->{
                BestenlisteController controller = BestenlisteController.getInstance();
                List<LeaderboardEntry> list = (List<LeaderboardEntry>) msg.getPayload();
                controller.bestenlisteZeigen(list);
            });
        }else if(msg.getType() == Message.MessageType.ACTIVE_ROOMS_RESPONSE && currentRoom.equals("LOBBY")){
            HashMap<String, List<String>> activeRooms = (HashMap<String, List<String>>) msg.getPayload();
            Platform.runLater(()->{
                LobbyController controller = LobbyController.getInstance();
                if(controller != null) {
                    controller.spielraumBox.getChildren().clear();
                    Set<String> rooms = activeRooms.keySet();
                    for (String room : rooms) {
                        List<String> players = activeRooms.get(room);
                        controller.addSpielraumToSpielraumBox(room, (players.isEmpty()) ? null : players.get(0), (players.size() < 2) ? null : players.get(1), (players.size() < 3) ? null : players.get(2));
                    }
                }
            });
        }else if(msg.getType() == Message.MessageType.CHANGE_PASSWORD_ERROR){
            Platform.runLater(()->{
                ProfileinstellungenController controller = ProfileinstellungenController.getProfileinstellungenControllerInstance();
                controller.showError("Passwort Änderung Fehler", (String) msg.getPayload());
            });
        }else if(msg.getType() == Message.MessageType.CHANGE_PASSWORD_RESPONSE){
            Platform.runLater(()->{
                ProfileinstellungenController controller = ProfileinstellungenController.getProfileinstellungenControllerInstance();
                controller.showSuccess("Erfolg", (String) msg.getPayload());
                controller.switchToLobbyGUI(primaryStage);
            });
        }
    }

    /**
     * Verarbeitet Spielraum Verwaltung Nachrichten vom Server.
     * Behandelt Raum Erstellung, Raum Beitreten, Raum verlassen Antworten und auch entsprechende Fehlermeldungen.
     * @param msg Raum-Management Nachricht.
     * @see Message
     * @author Devashish Pisal
     */
    public void handleRoomManagementMessage(Message msg){
        if(msg.getType() == Message.MessageType.CREATE_ROOM_ERROR){
            Platform.runLater(()->{
                SpielraumErstellenController controller = SpielraumErstellenController.getSpielraumErstellenControllerInstance();
                controller.showError("Spielraum Fehler", (String) msg.getPayload());
            });
        }else if(msg.getType() == Message.MessageType.CREATE_ROOM_RESPONSE){
            this.currentRoom = (String) msg.getPayload();
            this.isGameMaster = true;
            Platform.runLater(()->{
                SpielraumErstellenController controller = SpielraumErstellenController.getSpielraumErstellenControllerInstance();
                controller.switchToWarteraumGUI(primaryStage);
            });
        }else if(msg.getType() == Message.MessageType.JOIN_ROOM_ERROR){
            Platform.runLater(()->{
                SpielraumLoginController controller = SpielraumLoginController.getSpielraumLoginControllerInstance();
                controller.showError("Spielraum Fehler", (String) msg.getPayload());
            });
        }else if(msg.getType() == Message.MessageType.JOIN_ROOM_RESPONSE){
            this.currentRoom = (String) msg.getPayload();
            Platform.runLater(()->{
                SpielraumLoginController controller = SpielraumLoginController.getSpielraumLoginControllerInstance();
                controller.switchToWarteraumGUI(primaryStage);
            });
        }else if(msg.getType() == Message.MessageType.WAITING_ROOM_CHAT_RESPONSE){
            Platform.runLater(()->{
                WarteraumController controller = WarteraumController.getWarteraumControllerInstance();
                controller.addMessageToChatBox(msg.getSender(), (String) msg.getPayload());
            });
        }else if(msg.getType() == Message.MessageType.LEAVE_ROOM_RESPONSE){
            isGameMaster = false;
            currentRoom = "LOBBY";
            Platform.runLater(()->{
                WarteraumController controller = WarteraumController.getWarteraumControllerInstance();
                controller.switchToLobbyGUI(primaryStage);
            });
        }else if(msg.getType() == Message.MessageType.ROOM_PARTICIPANTS_RESPONSE){
            Platform.runLater(()->{
                WarteraumController controller = WarteraumController.getWarteraumControllerInstance();
                controller.displayRoomParticipants((List<String>) msg.getPayload());
            });
        }else if(msg.getType() == Message.MessageType.START_GAME_RESPONSE){
            session = (GameSession) msg.getPayload();
            Platform.runLater(()->{
                WarteraumController controller = WarteraumController.getWarteraumControllerInstance();
                controller.switchToSpielraumGUI(primaryStage);
            });
        }else if(msg.getType() == Message.MessageType.GAME_ROOM_CHAT_RESPONSE){
            Platform.runLater(()->{
                SpielraumController controller = SpielraumController.getSpielraumControllerInstance();
                controller.addMessageToChatBox(msg.getSender(), (String) msg.getPayload());
            });
        }
    }

    /**
     * Verarbeitet Server Spiel Antworten wie Karte ausspielen, usw und entsprechende Fehlermeldungen.
     * @param msg Antwort Nachricht vom Server.
     * @see Message
     * @author Devashish Pisal
     */
    public void handleGameLogicMessage(Message msg){
        if(msg.getType() == Message.MessageType.UPDATE_RESPONSE){
            GameSession session = (GameSession) msg.getPayload();
            this.setSession(session);
            if(session.getGameState() == GameSession.GameState.GAME_OVER){
                setCurrentRoom("LOBBY");
                setGameMaster(false);
            }
            Platform.runLater(()->{
                SpielraumController controller = SpielraumController.getSpielraumControllerInstance();
                controller.update();
            });
        }
    }


    /**
     * Schließt die Verbindung zum Server und alle Ressourcen.
     * Setzt das stopFlag auf true, um den Nachrichtenempfang zu beenden.
     * @author Devashish Pisal
     */
    public void handleDisconnect() {
        stopFlag = true; // Thread 'listenForMessages' signalisieren.
        try {
            if (socket != null) {
                socket.close();
            }
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            System.out.println("[INFO] [" + username + "] Client resources closed");
        } catch (IOException e) {
            System.out.println("[ERROR] [" + username + "] Error occurred while closing client");
            e.printStackTrace();
        }
        clientInstance = null;

        Platform.exit();
        // Force exit
        //System.exit(0);
    }

    /**
     * Setter Methode für Primary-Stage.
     * Zusätzliche wird hier das Ereignis behandelt, wenn Client versucht JavaFX fenster zu schließen.
     * @param primaryStage Haupt-Container für alle JavaFX Objekten.
     * @author Devashish Pisal
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setOnCloseRequest(event -> {
            if(connectionWithServerEstablished && username != null){
                if(username.equals("SYSTEM")){
                    System.out.println("[INFO] [" + username + "] GUI has closed during log in process");
                    handleDisconnect();
                }else{
                    event.consume();
                    Platform.runLater(()->{
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Warnung");
                        alert.setHeaderText(null);
                        alert.setContentText("Bitte für Austritt Ausloggen/Zurück/Verlassen Buttons verwenden");
                        alert.showAndWait();
                    });
                    System.out.println("[INFO] [" + username + "] User tried to use GUI close button");
                }
            }else{
                System.out.println("[INFO] [SYSTEM] GUI has closed before user logs in");
            }
        });
    }


    // Getter- und Setter Methoden
    public String getUsername() {return username;}

    public void setUsername(String username) {this.username = username;}

    public String getCurrentRoom() {return currentRoom;}

    public void setCurrentRoom(String currentRoom) {this.currentRoom = currentRoom;}

    public boolean getIsGameMaster() {return isGameMaster;}

    public void setGameMaster(boolean gameMaster) {isGameMaster = gameMaster;}

    public void setStopFlag(boolean flag) {this.stopFlag = flag;}

    public boolean getStopFlag(){return stopFlag;};

    public Stage getPrimaryStage() {return primaryStage;}

    public String getServerIP() {return serverIP;}

    public void setServerIP(String serverIP) {this.serverIP = serverIP;}

    public int getServerPort() {return serverPort;}

    public void setServerPort(int serverPort) {this.serverPort = serverPort;}

    public GameSession getSession() {return session;}

    public void setSession(GameSession session) {this.session = session;}

    public HashMap<Integer, PlayerInterface> getLastMatchResult() {return lastMatchResult;}

    public void setLastMatchResult(HashMap<Integer, PlayerInterface> lastMatchResult) {this.lastMatchResult = lastMatchResult;}

    // Diese Methoden nur für Testing verwenden.
    public void setMockParameters(Socket mockSocket, ObjectOutputStream mockOut, ObjectInputStream mockIn) throws IOException {
        this.socket = mockSocket;
        this.out = mockOut;
        this.in = mockIn;
    }

    public static void resetInstance() {
        clientInstance = null;
    }
}