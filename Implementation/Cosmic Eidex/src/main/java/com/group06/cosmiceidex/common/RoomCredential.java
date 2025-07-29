package com.group06.cosmiceidex.common;

import javafx.fxml.Initializable;

import java.io.Serializable;

/**
 * Objekt dieser Klasse beinhaltet die Spielraumdaten (Raumname und Passwort)
 * @author Devashish Pisal
 */
public class RoomCredential implements Serializable {

    private final String roomName;
    private final String password;

    /**
     * Konstruktor
     * @param name Name des Spielraums.
     * @param password Passwort des Spielraums.
     * @author Devashish Pisal
     */
    public RoomCredential(String name, String password){
        this.password = password;
        this.roomName = name;
    }

    // Getter Methoden
    public String getRoomName() {
        return roomName;
    }
    public String getPassword() {
        return password;
    }
}
