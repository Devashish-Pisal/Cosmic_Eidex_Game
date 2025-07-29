package com.group06.cosmiceidex.server;

import com.group06.cosmiceidex.common.LeaderboardEntry;
import com.group06.cosmiceidex.common.Message;
import com.group06.cosmiceidex.common.RoomCredential;
import com.group06.cosmiceidex.common.User;
import com.group06.cosmiceidex.exceptions.AuthException;
import com.group06.cosmiceidex.exceptions.RoomDoesNotExists;
import com.group06.cosmiceidex.game.GameSession;
import com.group06.cosmiceidex.game.Player;
import com.group06.cosmiceidex.game.PlayerInterface;
import com.group06.cosmiceidex.bots.*;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Thread zuständig für Kommunikation mit einzelne zugewiesene Client.
 * Empfängt Nachrichten von dem Client und schickt Antworten.
 * @author Devashish Pisal
 */
public class ClientHandler implements Runnable{
    private Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String username;
    protected String currentRoom = "LOBBY";
    protected boolean isGameMaster = false;
    private static final ScheduledExecutorService botExecutor = Executors.newSingleThreadScheduledExecutor();

    /**
     * Klassen Konstruktor.
     * Initialisiert Input und Output streams, um Kommunikation zwischen Client und Server zu gestalten.
     * @param socket Server akzeptierte Client Socket.
     * @param server Server instanz
     * @author Devashish Pisal
     */
    public ClientHandler(Socket socket, Server server){
        this.clientSocket = socket;
        try{
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        }catch(Exception e){
            System.out.println("[ERROR] [" + username + "] Unable to initialize Object streams");
            e.printStackTrace();
        }
    }


    /**
     * Empfängt die Nachrichten vom Client mithilfe von Input stream und leitet es weiter zur Verarbeitung.
     * Beim Fehler wird Verbindung abgeschlossen.
     * @author Devashish Pisal
     */
    @Override
    public void run() {
        try {
            while (true) {
                Object msg = in.readObject();
                if (msg instanceof Message) {
                    Message message = (Message) msg;
                    System.out.println("[INFO] [" + username + "] " + message.getType() + " Message received");
                    handleClientMessage(message);
                }
            }
        }catch (Exception e){
            System.out.println("[WARN] [" + username + "]" + " Something went wrong while listening for messages from client");
            handleClientHandlerDisconnect();
            System.out.println("[WARN] [" + username + "]" + " Message : " + e.getMessage());
        }
    }

    /**
     * Kategorisiert die Nachrichten je nach der Typ und leitet die weiter zu entsprechenden Methode.
     * @param message Empfangene Client Anfrage.
     * @see Message
     * @author Devashish Pisal
     */
    public void handleClientMessage(Object message){
        if(message instanceof Message){
            Message msg = (Message) message;
            Message.MessageType messageType = msg.getType();

            Set<Message.MessageType> authenticationMessageTypes = EnumSet.of(Message.MessageType.LOGIN_REQUEST, Message.MessageType.REGISTER_REQUEST);
            Set<Message.MessageType> lobbyManagementMessageTypes = EnumSet.of(Message.MessageType.LOGGED_IN_USERS_REQUEST, Message.MessageType.LOBBY_CHAT_REQUEST,
                    Message.MessageType.LEADERBOARD_DATA_REQUEST, Message.MessageType.COMPLETE_LEADERBOARD_REQUEST, Message.MessageType.LOGOUT_REQUEST,
                    Message.MessageType.ACTIVE_ROOMS_REQUEST, Message.MessageType.CHANGE_PASSWORD_REQUEST);
            Set<Message.MessageType> roomManagementMessageTypes = EnumSet.of(Message.MessageType.CREATE_ROOM_REQUEST, Message.MessageType.JOIN_ROOM_REQUEST,
                    Message.MessageType.LEAVE_ROOM_REQUEST, Message.MessageType.ROOM_INFO_REQUEST, Message.MessageType.WAITING_ROOM_CHAT_REQUEST, Message.MessageType.START_GAME_REQUEST,
                    Message.MessageType.CLOSE_ROOM_REQUEST, Message.MessageType.ADD_BOT, Message.MessageType.REMOVE_BOT, Message.MessageType.GAME_ROOM_CHAT_REQUEST);
            Set<Message.MessageType> gameLogicMessageTypes = EnumSet.of( Message.MessageType.PLAY_CARD,  Message.MessageType.GAME_UPDATE, Message.MessageType.UPDATE_REQUEST);

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
     * Verarbeitet Authentifizierungsanfragen vom Client.
     * Behandelt Login-, Registrierungsanfragen und schickt Antworten zum Client.
     * @param msg Authentifizierungsanfrage
     * @see Message
     * @see SessionManager
     * @see DatabaseService
     * @author Devashish Pisal
     */
    public void handleAuthenticationMessage(Message msg){
        if(msg.getType() == Message.MessageType.LOGIN_REQUEST){
            User userCredentials = (User) msg.getPayload();
            String username = userCredentials.getUsername();
            String password = userCredentials.getPassword();
            User foundUserCredentials = null;
            try{
                foundUserCredentials = DatabaseService.getDatabaseServiceInstance().checkPassword(username, password);
            } catch(SQLException e){
                Message response = new Message(Message.MessageType.LOGIN_ERROR, "SERVER", "SYSTEM", e.getMessage());
                sendMessageToClient(response);
                return;
            }
            if(foundUserCredentials != null){
                boolean userAlreadyLoggedIn = SessionManager.getSessionManagerInstance().getLoggedInClients().containsKey(username);
                if(userAlreadyLoggedIn){
                    Message response = new Message(Message.MessageType.LOGIN_ERROR, "SERVER", "SYSTEM", "Benutzer mit Benutzername " + username +
                            " bereits eingeloggt!");
                    sendMessageToClient(response);

                }else {
                    this.username = username;
                    this.currentRoom = "LOBBY";
                    SessionManager.getSessionManagerInstance().addClientHandlerToRoom("LOBBY", this);
                    Message response = new Message(Message.MessageType.LOGIN_RESPONSE, "SERVER", username, "LOGGED-IN-SUCCESSFULLY");
                    sendMessageToClient(response);
                }
            }else{
                Message response = new Message(Message.MessageType.LOGIN_ERROR, "SERVER", "SYSTEM", "Benutzername oder Passwort ist falsch!");
                sendMessageToClient(response);
            }
        }else if(msg.getType() == Message.MessageType.REGISTER_REQUEST){
            User userCredentials = (User) msg.getPayload();
            try{
                DatabaseService.getDatabaseServiceInstance().registerUser(userCredentials.getUsername(),userCredentials.getPassword());
                Message response = new Message(Message.MessageType.REGISTER_RESPONSE, "SERVER", "SYSTEM","USER-REGISTERED-SUCCESSFULLY" );
                sendMessageToClient(response);
            }catch(AuthException e){
                Message response = new Message(Message.MessageType.REGISTER_ERROR, "SERVER", "SYSTEM", e.getMessage());
                sendMessageToClient(response);
            }
        }
    }


    /**
     * Verarbeitet Lobby-Management-Anfragen vom Client.
     * Behandelt Bestenliste-, Chat- und Logout-Anfragen.
     * @param msg Lobby-Management-Anfrage
     * @see Message
     * @see SessionManager
     * @author Devashish Pisal
     */
    public void handleLobbyManagementMessage(Message msg){
        if(msg.getType() == Message.MessageType.LOBBY_CHAT_REQUEST){
            Message response = new Message(Message.MessageType.LOBBY_CHAT_RESPONSE, msg.getSender(), msg.getPayload());
            SessionManager.getSessionManagerInstance().broadcastMessageInRoom("LOBBY", response);
        }else if(msg.getType() == Message.MessageType.LOGOUT_REQUEST){
            SessionManager.getSessionManagerInstance().logOut(msg.getSender(),currentRoom);
            Message response = new Message(Message.MessageType.LOGOUT_RESPONSE, "SERVER", msg.getTarget(), null);
            sendMessageToClient(response);
            this.currentRoom = "LOBBY";
            this.username = null;
        }else if(msg.getType() == Message.MessageType.LEADERBOARD_DATA_REQUEST){
            DatabaseService service = DatabaseService.getDatabaseServiceInstance();
            List<LeaderboardEntry> leaderboard = service.getTopTen();
            if(leaderboard != null && !leaderboard.isEmpty()) {
                Message response = new Message(Message.MessageType.LEADERBOARD_DATA_RESPONSE, "SERVER", msg.getSender(), leaderboard);
                sendMessageToClient(response);
            }
        }else if(msg.getType() == Message.MessageType.COMPLETE_LEADERBOARD_REQUEST){
            DatabaseService service = DatabaseService.getDatabaseServiceInstance();
            List<LeaderboardEntry> leaderboard = service.getFullLeaderboard();
            if(leaderboard != null && !leaderboard.isEmpty()) {
                Message response = new Message(Message.MessageType.COMPLETE_LEADERBOARD_RESPONSE, "SERVER", msg.getSender(), leaderboard);
                sendMessageToClient(response);
            }
        }else if(msg.getType() == Message.MessageType.ACTIVE_ROOMS_REQUEST && currentRoom.equals("LOBBY")){
            HashMap<String,List<String>> map = SessionManager.getSessionManagerInstance().constructMapForActiveRooms();
            Message response = new Message(Message.MessageType.ACTIVE_ROOMS_RESPONSE, "SERVER", null, map);
            sendMessageToClient(response);
        }else if(msg.getType() == Message.MessageType.CHANGE_PASSWORD_REQUEST){
            int id = DatabaseService.getDatabaseServiceInstance().findIdOfUser(msg.getSender());
            if(id == -1){
                Message error = new Message(Message.MessageType.CHANGE_PASSWORD_ERROR, "SERVER", msg.getSender(), "Beim Ändern des Passworts ist etwas schief gelaufen!");
                sendMessageToClient(error);
            }else{
                DatabaseService.getDatabaseServiceInstance().changePassword(id, (String) msg.getPayload());
                Message response = new Message(Message.MessageType.CHANGE_PASSWORD_RESPONSE, "SERVER", msg.getSender(), "Password erfolgreich geändert!");
                sendMessageToClient(response);
            }
        }
    }

    /**
     * Verarbeitet Spielraum Verwaltung Anfragen vom Client.
     * Behandelt Raum Erstellung, Raum Beitreten, Raum verlassen Anfragen.
     * @param msg Raum-Management Nachricht.
     * @see Message
     * @see SessionManager
     * @author Devashish Pisal
     */
    public void handleRoomManagementMessage(Message msg){
        if(msg.getType() == Message.MessageType.CREATE_ROOM_REQUEST){
            RoomCredential creds = (RoomCredential) msg.getPayload();
            String roomName = creds.getRoomName().trim();
            if(SessionManager.getSessionManagerInstance().getActiveRooms().containsKey(roomName)){
                Message errorResponse = new Message(Message.MessageType.CREATE_ROOM_ERROR, "SERVER", msg.getSender(), "Spielraum '" + roomName + "' bereits existiert!");
                sendMessageToClient(errorResponse);
            }else{
                ClientHandler ch = SessionManager.getSessionManagerInstance().removeClientHandlerFromRoom(currentRoom, username);
                SessionManager.getSessionManagerInstance().addClientHandlerToRoom(roomName,ch);
                List<RoomCredential> list = SessionManager.getSessionManagerInstance().getRoomCredentials();
                list.add(creds);
                SessionManager.getSessionManagerInstance().setRoomCredentials(list);
                this.currentRoom = roomName;
                this.isGameMaster = true;
                Message response = new Message(Message.MessageType.CREATE_ROOM_RESPONSE, "SERVER", msg.getSender(), roomName);
                sendMessageToClient(response);
                List<String> usernames = SessionManager.getSessionManagerInstance().refreshRoomParticipants(roomName);
                Message participants = new Message(Message.MessageType.ROOM_PARTICIPANTS_RESPONSE, "SERVER", msg.getSender(), usernames);
                sendMessageToClient(participants);
            }
        }else if(msg.getType() == Message.MessageType.JOIN_ROOM_REQUEST){
            RoomCredential credentials = (RoomCredential) msg.getPayload();
            Message response = null;
            try{
                int numberOfHumanPlayers =  SessionManager.getSessionManagerInstance().getActiveRooms().get(credentials.getRoomName()).size();
                int numberOfBots = SessionManager.getSessionManagerInstance().getRoomBots().get(credentials.getRoomName()).size();
                int numberOfPlayersInRoom = numberOfHumanPlayers + numberOfBots;
                if(numberOfPlayersInRoom >= 3){
                    response = new Message(Message.MessageType.JOIN_ROOM_ERROR, "SERVER", msg.getSender(), "Der Raum ist voll!");
                    sendMessageToClient(response);
                    return;
                }
                boolean passwordIsCorrect = SessionManager.getSessionManagerInstance().validateRoomCredentials(credentials);
                if(passwordIsCorrect){
                    ClientHandler ch = SessionManager.getSessionManagerInstance().removeClientHandlerFromRoom(currentRoom, username);
                    SessionManager.getSessionManagerInstance().addClientHandlerToRoom(credentials.getRoomName(), ch);
                    this.currentRoom = credentials.getRoomName();
                    response = new Message(Message.MessageType.JOIN_ROOM_RESPONSE, "SERVER", msg.getSender(), credentials.getRoomName());
                    sendMessageToClient(response);
                    SessionManager.getSessionManagerInstance().broadcastRoomUpdate(credentials.getRoomName());
//                    List<String> usernames = SessionManager.getSessionManagerInstance().refreshRoomParticipants(currentRoom);
//                    Message participants = new Message(Message.MessageType.ROOM_PARTICIPANTS_RESPONSE, "SERVER", usernames);
//                    SessionManager.getSessionManagerInstance().broadcastMessageInRoom(currentRoom, participants);
                }else{
                    response = new Message(Message.MessageType.JOIN_ROOM_ERROR, "SERVER", msg.getSender(), "Passwort ist falsch!");
                    sendMessageToClient(response);
                }
            }catch(RoomDoesNotExists e){
                response = new Message(Message.MessageType.JOIN_ROOM_ERROR, "SERVER", msg.getSender(), e.getMessage());
                sendMessageToClient(response);
            }
        }else if(msg.getType() == Message.MessageType.WAITING_ROOM_CHAT_REQUEST){
            Message response = new Message(Message.MessageType.WAITING_ROOM_CHAT_RESPONSE, msg.getSender(), msg.getTarget(), msg.getPayload());
            SessionManager.getSessionManagerInstance().broadcastMessageInRoom(msg.getTarget(), response);
        }else if(msg.getType() == Message.MessageType.CLOSE_ROOM_REQUEST && isGameMaster){
            SessionManager.getSessionManagerInstance().closeRoom((String) msg.getPayload());
        }else if(msg.getType() == Message.MessageType.LEAVE_ROOM_REQUEST && !isGameMaster){
            String previousRoom = getCurrentRoom();
            ClientHandler ch = SessionManager.getSessionManagerInstance().removeClientHandlerFromRoom((String) msg.getPayload(), msg.getSender());
            SessionManager.getSessionManagerInstance().addClientHandlerToRoom("LOBBY", ch);
            ch.currentRoom = "LOBBY";
            Message response = new Message(Message.MessageType.LEAVE_ROOM_RESPONSE, "SERVER", msg.getSender(), null);
            ch.sendMessageToClient(response);
            SessionManager.getSessionManagerInstance().broadcastRoomUpdate(previousRoom);
//            List<String> usernames = SessionManager.getSessionManagerInstance().refreshRoomParticipants((String) msg.getPayload());
//            Message participants = new Message(Message.MessageType.ROOM_PARTICIPANTS_RESPONSE, "SERVER", usernames);
//            SessionManager.getSessionManagerInstance().broadcastMessageInRoom((String) msg.getPayload(), participants);
        }else if(msg.getType() == Message.MessageType.START_GAME_REQUEST){
            GameSession session = setupAndStartGameSession(msg);
            session.dealCards();
            Message response = new Message(Message.MessageType.START_GAME_RESPONSE, "SERVER", currentRoom, session);
            SessionManager.getSessionManagerInstance().broadcastMessageInRoom(currentRoom, response);
        } else if (msg.getType() == Message.MessageType.ADD_BOT) {
            if (getIsGameMaster()) {
                String botType = (String) msg.getPayload();
                SessionManager.getSessionManagerInstance().addBotToRoom(this.currentRoom, botType);
            }
        } else if (msg.getType() == Message.MessageType.REMOVE_BOT) {
            if (getIsGameMaster()) {
                SessionManager.getSessionManagerInstance().removeBotsFromRoom(getCurrentRoom());
            }
        } else if(msg.getType() == Message.MessageType.GAME_ROOM_CHAT_REQUEST){
            String text = (String) msg.getPayload();
            Message response = new Message(Message.MessageType.GAME_ROOM_CHAT_RESPONSE, msg.getSender(), msg.getTarget(), text);
            SessionManager.getSessionManagerInstance().broadcastMessageInRoom(msg.getTarget(), response);
        }
    }

    /**
     * Verarbeitet Client Spiel Anfragen, wie Karte ausspielen, usw und entsprechende Fehlermeldungen.
     * @param msg Anfrage Nachricht vom Client.
     * @see Message
     * @author Devashish Pisal
     */
    public void handleGameLogicMessage(Message msg){
        if(msg.getType() == Message.MessageType.UPDATE_REQUEST){
            GameSession session = (GameSession) msg.getPayload();
            HashMap<Integer, PlayerInterface> result = session.getGameResult();
            if(result != null && session.getGameState() == GameSession.GameState.GAME_OVER){
                for (int i = 1; i <= 3; i++) {
                    PlayerInterface player = result.get(i);
                    int playerId = DatabaseService.getDatabaseServiceInstance().findIdOfUser(player.getUsername());
                    if(playerId != -1){
                        int winCount = DatabaseService.getDatabaseServiceInstance().findWinCount(playerId);
                        int trickCount = DatabaseService.getDatabaseServiceInstance().findTrickCount(playerId);
                        int pointsCount = DatabaseService.getDatabaseServiceInstance().findPointsCount(playerId);
                        if(winCount < 0){winCount = 0;}
                        if(trickCount < 0){trickCount = 0;}
                        if(pointsCount < 0){pointsCount = 0;}
                        if((i == 1) && ((winCount+1) >= 0) && (trickCount + player.getNumberOfWonTricksDuringEntireGame() >= 0) && (pointsCount + player.getTotalWonTrickPointDuringEntireGame() >= 0)) {
                            try {
                                DatabaseService.getDatabaseServiceInstance().upsertLeaderboard(playerId, winCount + 1, trickCount + player.getNumberOfWonTricksDuringEntireGame(), pointsCount + player.getTotalWonTrickPointDuringEntireGame());
                            } catch (SQLException e) {
                                System.out.println("[ERROR] [" + player.getUsername() + "] Unable to update score of winner in database, because of exception : " + e.getMessage());
                            }
                        } else{
                            try {
                                DatabaseService.getDatabaseServiceInstance().upsertLeaderboard(playerId, winCount, trickCount + player.getNumberOfWonTricksDuringEntireGame(), pointsCount + player.getTotalWonTrickPointDuringEntireGame());
                            } catch (SQLException e) {
                                System.out.println("[ERROR] [" + player.getUsername() + "] Unable to update score of winner in database, because of exception : " + e.getMessage());
                            }
                        }
                    }else{
                        System.out.println("[ERROR] [" + player.getUsername() + "] Unable to update score of winner in database");
                    }
                }
            }

            // send human player move
            Message response = new Message(Message.MessageType.UPDATE_RESPONSE, msg.getSender(), msg.getTarget(), session);
            SessionManager.getSessionManagerInstance().broadcastMessageInRoom(msg.getTarget(), response);

            //Make bot move
            sendBotMoveMessages(session, msg);

            // if game is over store result in DB
            if(result != null && session.getGameState() == GameSession.GameState.GAME_OVER){SessionManager.getSessionManagerInstance().closeRoomWithoutSendingResponse(getCurrentRoom());}
        }
    }



    /**
     * Spielt Bot Zug mit 3 Sekunden Verzögerung, wenn Bot am Zug ist
     * @param session Aktuelle Session Objekt
     * @param msg Erhaltene UPDATE_REQUEST Nachricht vom Client
     * @see PlayerInterface
     * @author Devashish Pisal
     */
    public void sendBotMoveMessages(GameSession session, Message msg){
        botExecutor.schedule(() -> {
            PlayerInterface bot = session.getPlayersWithUsernames().get(session.getUsernameOfPlayerOnTurn());
            if ((bot.getUsername().startsWith("EasyBot-") || bot.getUsername().startsWith("HardBot-")) && bot.isBot() && bot.isMyTurn()) {
                session.playBotMove();
                Message response2 = new Message(Message.MessageType.UPDATE_RESPONSE, msg.getSender(), msg.getTarget(), session);
                SessionManager.getSessionManagerInstance().broadcastMessageInRoom(msg.getTarget(), response2);


                PlayerInterface nextPlayer = session.getPlayersWithUsernames().get(session.getUsernameOfPlayerOnTurn());
                if((nextPlayer.getUsername().startsWith("EasyBot-") || nextPlayer.getUsername().startsWith("HardBot-")) && nextPlayer.isBot() && nextPlayer.isMyTurn()){
                    sendBotMoveMessages(session, msg);
                }
                }
        }, 3, TimeUnit.SECONDS);
    }



    /**
     * Initialisiert Spielsession und markiert den ersten Spieler als Spielleiter
     * Für HardBots wird außerdem die GameSession gesetzt
     * @param msg Liste von Spielernamen (genau 3) wobei mit EasyBot und HardBot beginnende Namen als Bot
     *            interpretiert werden
     * @return GameSession mit drei Spielern
     * @author TB
     */
    public GameSession setupAndStartGameSession(Message msg) {
        @SuppressWarnings("unchecked")
        String roomName = (String) msg.getPayload();
        List<String> playerNames = SessionManager.getSessionManagerInstance().createParticipantsListIncludingBots(roomName);
        List<PlayerInterface> playersForSession = new ArrayList<>();

        for (String name : playerNames) {
            if (name.startsWith("EasyBot-")) {
                playersForSession.add(new EasyBot(name));
            } else if (name.startsWith("HardBot-")) {
                playersForSession.add(new HardBot(name));
            } else {
                playersForSession.add(new Player(name));
            }
        }

        // GameMaster markieren
        for (PlayerInterface p : playersForSession) {
            if (!(p instanceof EasyBot) && !(p instanceof HardBot)) {
                Player human = (Player) p;
                if (getUsername().equals(human.getUsername())) {
                    human.setGameMaster(true);
                }
            }
        }



        if (playersForSession.size() != 3) {
            throw new IllegalStateException("Es müssen 3 Spieler (inkl. Bots) vorhanden sein!");
        }

        // Erstelle GameSession und speichere sie lokal
        GameSession session = new GameSession(
                playersForSession.get(0),
                playersForSession.get(1),
                playersForSession.get(2)
        );

        // Gehe durch Spieler und setze Session für HardBots
        for (PlayerInterface player : playersForSession) {
            if (player instanceof HardBot) {
                ((HardBot) player).setGameSession(session);
            }
        }

        return session;
    }



    /**
     * Schickt Nachricht zu zugewiesene Client mithilfe von Output streams.
     * @param msg Antwort Nachricht, der zu Client gesendet werden soll.
     * @author Devashish Pisal
     */
    public void sendMessageToClient(Object msg){
        try{
            out.reset();
            out.writeObject(msg);
            out.flush();
            if(msg instanceof  Message){
                System.out.println("[INFO] [" + username + "] " + ((Message) msg).getType() + " Message sent to client");
            }
        }catch (Exception e){
            System.out.println("[ERROR] [" + username + "] " + "unable to send message to client");
            e.printStackTrace();
        }
    }

    /**
     * Schließt die Verbindung zum Client und alle Ressourcen.
     * @author Devashish Pisal
     */
    public void handleClientHandlerDisconnect(){
        try{
            out.close();
            in.close();
            clientSocket.close();
            System.out.println("[INFO] [" + username + "] ClientHandler resources closed");
        }catch (Exception e){
            System.out.println("[ERROR] [" + username + "] Error occurred while closing clientHandler");
            e.printStackTrace();
        }
    }

    // Getter und Setter Methoden
    public String getUsername() {return username;}

    public void setUsername(String username) {this.username = username;}

    public String getCurrentRoom() {return currentRoom;}

    public void setCurrentRoom(String currentRoom) {this.currentRoom = currentRoom;}

    public boolean getIsGameMaster() {return isGameMaster;}

    public void setGameMaster(boolean gameMaster) {isGameMaster = gameMaster;}

    public Socket getClientSocket() {return clientSocket;}

    public ObjectInputStream getIn() {return in;}

    public ObjectOutputStream getOut() {return out;}

    // Diese Method nur für Testing verwenden (für Mocks-Injection).
    public void setMockParameters( ObjectOutputStream mockOut, ObjectInputStream mockIn) throws IOException {
        this.out = mockOut;
        this.in = mockIn;
    }
}
