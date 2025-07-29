package com.group06.cosmiceidex.common;

import java.io.Serializable;
import java.util.Objects;

/**
 * Repräsentiert einen Benutzer mit Username und Passwort.
 * Verwaltet Nutzerdaten.
 */

public class User implements Serializable {
    private String username;
    private String password;

    public User() {
    }

    /**
     * Konstruktor mit Username und Passwort.
     * @param username
     * @param password
     */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Getter für Username
     * @return
     */
    public String getUsername() {
        return username;
    }

    /**
     * Setter für Username
     * @param username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Getter für Passwort
     * @return
     */
    public String getPassword() {
        return password;
    }

    /**
     * Setter für Passwort
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Prüft auf gleichen Username
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }

    /**
     * Berechnet Hashcode für das Objekt basierend auf Username
     * @return
     */
    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    /**
     * Gibt Username als Zeichenkette zurück
     * @return
     */
    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                '}';
    }
}
