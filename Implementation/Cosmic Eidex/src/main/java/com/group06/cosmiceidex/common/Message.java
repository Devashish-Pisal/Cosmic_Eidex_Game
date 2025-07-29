package com.group06.cosmiceidex.common;

import java.io.Serial;
import java.io.Serializable;

/**
 * Haupt Kommunikationsprotokoll zwischen Client und Server.
 * Objekten von dieser Klasse wird verwendet für Nachrichten Austausch zwischen client und server.
 * @author Devashish Pisal
 */
public class Message implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public enum MessageType {
        // Für Authentifizierung
        LOGIN_REQUEST, LOGIN_RESPONSE,
        REGISTER_REQUEST, REGISTER_RESPONSE,


        // Für Lobby Verwaltung
        LOGGED_IN_USERS_REQUEST, LOGGED_IN_USERS_RESPONSE,
        LOBBY_CHAT_REQUEST, LOBBY_CHAT_RESPONSE,
        LEADERBOARD_DATA_REQUEST, LEADERBOARD_DATA_RESPONSE,
        COMPLETE_LEADERBOARD_REQUEST, COMPLETE_LEADERBOARD_RESPONSE,
        LOGOUT_REQUEST, LOGOUT_RESPONSE,
        ACTIVE_ROOMS_REQUEST, ACTIVE_ROOMS_RESPONSE,
        CHANGE_PASSWORD_REQUEST, CHANGE_PASSWORD_RESPONSE,


        // Für Raum Verwaltung
        CREATE_ROOM_REQUEST, CREATE_ROOM_RESPONSE,
        JOIN_ROOM_REQUEST, JOIN_ROOM_RESPONSE,
        LEAVE_ROOM_REQUEST, LEAVE_ROOM_RESPONSE,
        CLOSE_ROOM_REQUEST,
        ROOM_INFO_REQUEST, ROOM_INFO_RESPONSE,
        START_GAME_REQUEST, START_GAME_RESPONSE,
        GAME_ROOM_CHAT_REQUEST, GAME_ROOM_CHAT_RESPONSE,
        WAITING_ROOM_CHAT_REQUEST, WAITING_ROOM_CHAT_RESPONSE,
        ROOM_PARTICIPANTS_RESPONSE,
        ADD_BOT,
        REMOVE_BOT,

        // Spiel Logik
        UPDATE_REQUEST,
        UPDATE_RESPONSE,
        PLAY_CARD,
        GAME_UPDATE,
        INVALID_MOVE,

        // Fehlermeldungen/Status
        ERROR,
        LOGIN_ERROR,
        REGISTER_ERROR,
        CREATE_ROOM_ERROR,
        JOIN_ROOM_ERROR,
        CHANGE_PASSWORD_ERROR,
        SUCCESS
    }

    private MessageType type;
    private String sender;
    private String target; // Optional
    private Object payload;

    /**
     * Konstruktor für Message Objekt (ohne Empfänger)
     * @param type Type der Nachricht.
     * @param sender Absender der Nachricht.
     * @param payload Daten, die zum Server geschickt werden soll.
     * @author Devashish Pisal
     */
    public Message(MessageType type, String sender, Object payload) {
        this.type = type;
        this.sender = sender;
        this.payload = payload;
    }

    /**
     * Konstruktor für Message Objekt (mit bekannten Empfänger)
     * @param type Type der Nachricht.
     * @param sender Absender der Nachricht.
     * @param payload Daten, die zum Server geschickt werden soll.
     * @author Devashish Pisal
     */
    public Message(MessageType type, String sender, String target, Object payload) {
        this.type = type;
        this.sender = sender;
        this.target = target;
        this.payload = payload;
    }


    // Getter- und Setter-Methoden.
    public MessageType getType() {
        return type;
    }

    public String getSender() {
        return sender;
    }

    public String getTarget() {
        return target;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public void setType(MessageType type) {
        this.type = type;
    }
}
