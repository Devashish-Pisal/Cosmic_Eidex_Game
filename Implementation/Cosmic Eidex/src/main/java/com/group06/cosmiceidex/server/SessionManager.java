package com.group06.cosmiceidex.server;

import com.group06.cosmiceidex.common.Message;
import com.group06.cosmiceidex.common.RoomCredential;
import com.group06.cosmiceidex.exceptions.RoomDoesNotExists;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Verwaltet Zuordnung zwischen Spielräume und drin befindete ClientHandlers.
 * Die Klasse ist Verantwortlich für ein ClientHandler in einem Spielraum einzufügen, löschen und Nachrichten in einem Raum an allen zu senden.
 * @author Devashish Pisal
 */
public class SessionManager {
    private static SessionManager sessionManagerInstance;
    private ConcurrentHashMap<String,ClientHandler> loggedInClients;
    private ConcurrentHashMap<String, Set<ClientHandler>> activeRooms;
    protected List<RoomCredential> roomCredentials;
    private int easyBotCounter = 1;
    private int hardBotCounter = 1;
    private ConcurrentHashMap<String, List<String>> roomBots = new ConcurrentHashMap<>();

    /**
     * Klassen Konstruktor.
     * Alle Hashmaps wird initialisiert und Raum LOBBY eingefügt.
     * @author Devashish Pisal
     */
    private SessionManager(){
        loggedInClients = new ConcurrentHashMap<>();
        activeRooms = new ConcurrentHashMap<>();
        roomCredentials = Collections.synchronizedList(new ArrayList<>());
        activeRooms.put("LOBBY", ConcurrentHashMap.newKeySet());
        // Anpassung TB: Bot-Liste für LOBBY initialisieren
        roomBots.put("LOBBY", Collections.synchronizedList(new ArrayList<>()));
    }

    /**
     * Gibt einzige Instanz der SessionManager-Klasse zurück.
     * Falls bisher noch keine Instanz existiert, wird eine neue Instanz erstellt.
     * @return Einzige SessionManager-Instanz
     * @author Devashish Pisal
     */
    public static SessionManager getSessionManagerInstance(){
        if(sessionManagerInstance == null){
            sessionManagerInstance = new SessionManager();
        }
        return sessionManagerInstance;
    }

    /**
     * Fügt übergebene ClientHandler im Raum ein.
     * @param roomName Ziel Raum
     * @param ch ClientHandler, der eingefügt werden soll.
     * @author Devashish Pisal
     */
    public void addClientHandlerToRoom(String roomName, ClientHandler ch){
        String username = ch.getUsername();
        loggedInClients.putIfAbsent(username, ch);

        activeRooms.computeIfAbsent(roomName, k -> ConcurrentHashMap.newKeySet());
        roomBots.computeIfAbsent(roomName, k -> Collections.synchronizedList(new ArrayList<>()));

        activeRooms.computeIfPresent(roomName, (key, clientSet) -> {
            boolean isDuplicate = clientSet.stream()
                    .anyMatch(c -> c.getUsername().equals(username));
            if (!isDuplicate) {
                clientSet.add(ch);
            }
            return clientSet;
        });
        refreshLobbyRooms();
        System.out.println("[INFO] [SessionManager] ClientHandler of " + username + " added to room " + roomName);
    }

    /**
     * Liefert Liste von ClientHandlers zurück, die im gegebene Raum befinden.
     * @param roomName Gezielte Raum.
     * @throws RoomDoesNotExists Falls Raum nicht existiert, dann wird diese Ausnahme geworfen.
     * @return Die Sammlung von ClientHandler, die im gezielte Raum befinden.
     * @author Devashish Pisal
     */
    public Set<ClientHandler> getClientHandlerListOfRoom (String roomName) throws RoomDoesNotExists{
        if(activeRooms.containsKey(roomName)) {
            return activeRooms.get(roomName);
        }else{
            System.out.println("[WARN] [SessionManager] " + roomName + " room does not exists" );
            throw new RoomDoesNotExists("Room with room Name '" + roomName + "' is not available");
        }
    }

    /**
     * Entfernt ClientHandler von einem Raum.
     * @param roomName Der Raum, indem der ClientHandler sein soll.
     * @param username Eindeutige Benutzername der ClientHandler bzw Benutzername des Clients
     * @return Entworfene ClientHandler oder falls ClientHandler im entsprechende Raum nicht gefunden dann null
     * @author Devashish Pisal
     */
    public ClientHandler removeClientHandlerFromRoom(String roomName, String username) {
        ClientHandler[] removedHandler = new ClientHandler[1];

        activeRooms.computeIfPresent(roomName, (key, clientSet) -> {
            removedHandler[0] = clientSet.stream()
                    .filter(c -> c.getUsername().equals(username))
                    .findFirst()
                    .orElse(null);

            if (removedHandler[0] != null) {
                clientSet.remove(removedHandler[0]);
                System.out.println("[INFO] Removed " + username + " from " + roomName);
            } else {
                System.out.println("[WARN] User " + username + " not found in " + roomName);
            }
            return (clientSet.isEmpty() && !roomName.equals("LOBBY")) ? null : clientSet;
        });
        return removedHandler[0]; // ClientHandler zurückgeben (falls nicht gefunden, dann null)
    }

    /**
     * Sendet eine Nachricht zu jeder Client, der im gezielte Raum befindet mithilfe von sendMessageToClient() Methode
     * @param roomName Gezielte Raum.
     * @param msg Nachricht, der gesendet werden soll.
     * @see ClientHandler#sendMessageToClient(Object)
     * @author Devashish Pisal
     */
    public void broadcastMessageInRoom(String roomName, Message msg){
        Set<ClientHandler> clientHandlers = activeRooms.get(roomName);
        if (clientHandlers == null) {
            System.out.println("[WARN] [SessionManager] Unable to to broadcast message in room " + roomName);
            return;
        }
        for(ClientHandler clientHandler : clientHandlers){
            clientHandler.sendMessageToClient(msg);
        }
    }

    /**
     * Entfernt Benutzername vom Client aus Liste von angemeldete Benutzer.
     * Entfernt ClientHandler des Benutzers vom Liste von der ClientHandlers
     * @param username Benutzername der Client
     * @param currentRoom Raum, in dem Benutzer gerade befindet.
     * @author Devashish Pisal
     */
    public void logOut(String username, String currentRoom) {
        ClientHandler targetHandler = loggedInClients.remove(username);

        if (targetHandler != null) {
            activeRooms.computeIfPresent(currentRoom, (room, clientSet) -> {
                clientSet.remove(targetHandler);
                refreshLobbyRooms();
                return (clientSet.isEmpty() && !currentRoom.equals("LOBBY")) ? null : clientSet;  // Remove room if empty
            });

            System.out.println("[INFO] [SessionManager] " + username + " logged out");
        } else {
            System.out.println("[WARN] [SessionManager] " + username + " not found in loggedInClients");
        }
    }


    /**
     * Erstellt HashMap von allem derzeit existierende Spielräumen und dazugehörende Benutzern
     * @return HashMap von allem derzeit existierende Spielräumen und dazugehörende Benutzern außer Raum LOBBY.
     * @author Devashish Pisal
     */
    public HashMap<String, List<String>> constructMapForActiveRooms(){
        HashMap<String, List<String>> result = new HashMap<>();
        for (Map.Entry<String, Set<ClientHandler>> entry : getActiveRooms().entrySet()) {
            if (!"LOBBY".equals(entry.getKey())) {  // Skip LOBBY
                List<String> players = entry.getValue().stream()
                        .map(ClientHandler::getUsername)
                        .collect(Collectors.toList());

                if(roomBots.containsKey(entry.getKey())){
                    List<String> bots = roomBots.get(entry.getKey());
                    players.addAll(bots);
                }
                result.put(entry.getKey(), players);
            }
        }
        return result;
    }

    /**
     * Prüft die Korrektheit von Raumname und Passwort.
     * @param room Objekt, der Raumname und Passwort enthält.
     * @see RoomCredential
     * @throws RoomDoesNotExists Falls Raum nicht existiert, dann wird diese Ausnahme geworfen.
     * @return Falls Raum gefunden und passwort ist richtig wird True zurückgegeben, falls Passwort falsch ist, wird False zurückgegeben.
     * @author Devashish Pisal
     */
    public boolean validateRoomCredentials(RoomCredential room) throws RoomDoesNotExists{
        String name = room.getRoomName();
        String password = room.getPassword();
        boolean roomFound = false;
        for(RoomCredential cred : roomCredentials){
            if(cred.getRoomName().equals(name)) {
                roomFound = true;
                if(cred.getPassword().equals(password)){
                    return true;
                }
            }
        }
        if(roomFound){
            return false;
        }else{
            throw new RoomDoesNotExists("Spielraum '" + name + "' existiert nicht!");
        }
    }

    /**
     * Aktualisiert Spielraum Liste, die jeder im Lobby befindete Client sieht.
     * @see Message
     * @author Devashish Pisal
     */
    protected void refreshLobbyRooms(){
        HashMap<String,List<String>> map = SessionManager.getSessionManagerInstance().constructMapForActiveRooms();
        Message automatedResponse = new Message(Message.MessageType.ACTIVE_ROOMS_RESPONSE, "SERVER", "LOBBY", map);
        SessionManager.getSessionManagerInstance().broadcastMessageInRoom("LOBBY", automatedResponse);
    }

    /**
     * Löscht der gegebene Raum und fügt alle ClientHandlers im Raum LOBBY ein.
     * @param roomName Gezielt Raum, der gelöscht werden soll.
     * @see Message
     * @author Devashish Pisal
     */
    public void closeRoom(String roomName){
        if(!roomName.equals("LOBBY")) {
            Set<ClientHandler> clientHandlers = activeRooms.remove(roomName);
            roomBots.remove(roomName);

            List<RoomCredential> credentials = new ArrayList<>(roomCredentials);
            RoomCredential found = null;
            for(RoomCredential cred : credentials){
                if(cred.getRoomName().equals(roomName)){
                    found = cred;
                    break;
                }
            }
            if(found != null){
                roomCredentials.remove(found);
            }else{
                System.out.println("[WARN] [SessionManager] Unable to remove Room Credentials");
            }
            if(clientHandlers != null) {
                for (ClientHandler ch : clientHandlers) {
                    addClientHandlerToRoom("LOBBY", ch);
                    ch.isGameMaster = false;
                    ch.currentRoom = "LOBBY";
                    Message response = new Message(Message.MessageType.LEAVE_ROOM_RESPONSE, "SERVER", ch.getUsername(), null);
                    ch.sendMessageToClient(response);
                }
            }
        }
    }

    /**
     * Erstellen eine Liste von Raum Teilnehmern
     * @param roomName Spielraum Name
     * @return Liste der Benutzernamen mit Erkennung der Raum Ersteller
     * @see ClientHandler
     * @author Devashish Pisal
     */
    public List<String> refreshRoomParticipants(String roomName){
        List<String> list = new ArrayList<>();
        Set<ClientHandler> handlers = activeRooms.get(roomName);
        for(ClientHandler ch : handlers){
            if(ch.getIsGameMaster()){
                list.add(ch.getUsername() + " (Admin)");
            }else{
                list.add(ch.getUsername());
            }
        }
        return list;
    }


    /**
     * Löscht der gegebene Raum und fügt alle ClientHandlers im Raum LOBBY ein.
     * @param roomName Gezielt Raum, der gelöscht werden soll.
     * @see Message
     * @author Devashish Pisal
     */
    public void closeRoomWithoutSendingResponse(String roomName){
        if(!roomName.equals("LOBBY")) {
            Set<ClientHandler> clientHandlers = activeRooms.remove(roomName);
            roomBots.remove(roomName);
            List<RoomCredential> credentials = new ArrayList<>(roomCredentials);
            RoomCredential found = null;
            for(RoomCredential cred : credentials){
                if(cred.getRoomName().equals(roomName)){
                    found = cred;
                    break;
                }
            }
            if(found != null){
                roomCredentials.remove(found);
            }else{
                System.out.println("[WARN] [SessionManager] Unable to remove Room Credentials");
            }
            if(clientHandlers != null) {
                for (ClientHandler ch : clientHandlers) {
                    addClientHandlerToRoom("LOBBY", ch);
                    ch.isGameMaster = false;
                    ch.currentRoom = "LOBBY";
                }
            }
        }
    }

    // Funktion zum debuggen
    // TODO: Ganz am Ende des Projekts diese Funktion löschen.
    /*public void printMap(String fromMethod){
        for(String room : activeRooms.keySet()){
            Set<ClientHandler> handlers = activeRooms.get(room);
            System.out.print(fromMethod + " | Room : " + room + " Players : ");
            for(ClientHandler handler : handlers){
                System.out.print(handler.getUsername() + " ");
            }
            System.out.println();
        }
    }*/


    // Anpassung TB: Botmethoden hinzugefügt:

    /**
     * Fügt Hard und Easy Bots hinzu (nicht mehr als 3 Spieler + Bots)
     * @param roomName
     * @param botType
     */
    public void addBotToRoom(String roomName, String botType) {
        List<String> bots = roomBots.get(roomName);
        Set<ClientHandler> humans = activeRooms.get(roomName);
        if (bots != null && humans != null && humans.size() + bots.size() < 3) {
            String botName = botType.equals("Easy")
                    ? "EasyBot-" + easyBotCounter++
                    : "HardBot-" + hardBotCounter++;
            bots.add(botName);
            broadcastRoomUpdate(roomName);
        }
    }


    // Erstelle Raum Liste (Spieler + Bots)
    public List<String> createParticipantsListIncludingBots(String roomName){
        List<String> result = new ArrayList<>();
        Set<ClientHandler> handlers = activeRooms.get(roomName);
        if(roomBots.containsKey(roomName)) {
            result = roomBots.get(roomName);
        }
        for(ClientHandler handler : handlers){
            result.add(handler.getUsername());
        }
        return result;
    }

    /**
     * Entfernt Bots und resetet zähler
     * @param roomName
     */
    public void removeBotsFromRoom(String roomName) {
        List<String> bots = roomBots.get(roomName);
        if (bots != null) {
            bots.clear();
            easyBotCounter = 1;
            hardBotCounter = 1;
            broadcastRoomUpdate(roomName);
        }
    }

    /**
     * Sendet Kombinierte Spielerliste an clients
     * @param roomName
     */
    public void broadcastRoomUpdate(String roomName) {
        // Menschen
        List<String> participants = refreshRoomParticipants(roomName);
        // Bots
        List<String> bots = roomBots.getOrDefault(roomName, Collections.emptyList());
        participants.addAll(bots);

        Message updateMessage = new Message(
                Message.MessageType.ROOM_PARTICIPANTS_RESPONSE,
                "SERVER",
                roomName,
                participants
        );
        broadcastMessageInRoom(roomName, updateMessage);
        refreshLobbyRooms();
    }


    // Getter-Setter Methoden
    public ConcurrentHashMap<String, ClientHandler> getLoggedInClients() {return loggedInClients;}
    public ConcurrentHashMap<String, Set<ClientHandler>> getActiveRooms() {return activeRooms;}
    public ConcurrentHashMap<String, List<String>> getRoomBots(){return roomBots;}


    //Nur für Testing verwenden (Zweck : Mock Injection)
    public List<RoomCredential> getRoomCredentials() {return roomCredentials;}
    public void setRoomCredentials(List<RoomCredential> roomCredentials) {this.roomCredentials = roomCredentials;}
}

