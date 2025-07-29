package com.group06.cosmiceidex.controllerlogic;

import com.group06.cosmiceidex.exceptions.AuthException;

import java.util.regex.Pattern;

import static com.group06.cosmiceidex.controllers.RegistrierenController.getInstance;

public class RegistrierenControllerLogic {
    /**
     * Validiert die Mindestanforderungen an ein Passwort (Länge > 7, Ziffern, Groß-/Kleinbuchstaben, Sonderzeichen)
     * @param username Username des Benutzers
     * @param password Passwort
     * @param repeatedPassword Wiederholtes Passwort
     * @return Boolean, ob angegebene Werte zulässig sind
     * @throws AuthException
     */
    public static String[] validateCredentials(String username, String password, String repeatedPassword) throws AuthException {
        String SEPARATOR = ":";
        String[] returnArray = new String[2];
        // Prüfen, dass Benutzername nicht leer ist
        if (username == null || username.trim().isEmpty()) {
            returnArray[0] = ("Benutzername Fehler");
            returnArray[1] = ("Benutzername darf nicht leer sein!");
            return returnArray;
        }
        // Prüfen, dass Benutzername nicht das Trennzeichen enthält
        if (username.trim().contains(SEPARATOR)) {
            returnArray[0] = ("Benutzername Fehler");
            returnArray[1] = ("Benutzername darf das Zeichen '\" + SEPARATOR + \"' nicht enthalten.");
            return returnArray;
        }

        if(username.startsWith("EasyBot-") || username.startsWith("HardBot-")){
            returnArray[0] = ("Benutzername Fehler");
            returnArray[1] = ("Benutzername darf nicht '" + username + "' sein. Da es ein reservierte Benutzername ist." );
            return returnArray;
        }

        if(username.contains(" ")){
            returnArray[0] = ("Benutzername Fehler");
            returnArray[1] = ("Benutzername darf kein Leerzeichen erhalten.");
            return returnArray;
        }

        if(username.equals("SYSTEM")){
            returnArray[0] = ("Benutzername Fehler");
            returnArray[1] = ("Benutzername darf nicht 'SYSTEM' sein!");
            return returnArray;
        }

        if(username.equals("SERVER")){
            returnArray[0] = ("Benutzername Fehler");
            returnArray[1] = ("Benutzername darf nicht 'SERVER' sein!");
            return returnArray;
        }

        // Prüfen, dass Passwort nicht leer ist
        if (password == null || password.isEmpty()) {
            returnArray[0] = ("Passwort Fehler");
            returnArray[1] = ("Passwort darf nicht leer sein!");
            return returnArray;
        }

        // Prüfen, dass Passwort nicht das Trennzeichen enthält
        if (password.contains(SEPARATOR)) {
            returnArray[0] = ("Passwort Fehler");
            returnArray[1] = ("Passwort darf das Zeichen '\" + SEPARATOR + \"' nicht enthalten.");
            return returnArray;
        }

        if (password.contains(" ")) {
            returnArray[0] = ("Passwort Fehler");
            returnArray[1] = ("Das Passwort darf kein Leerzeichen enthalten.");
            return returnArray;
        }

        // Prüfen, dass Passwort und Wiederholung übereinstimmen
        if (!password.equals(repeatedPassword)) {
            returnArray[0] = ("Passwort Fehler");
            returnArray[1] = ("Die Passwörter sind nicht gleich.");
            return returnArray;
        }

        if (password.length() < 8) {
            returnArray[0] = ("Passwort Fehler");
            returnArray[1] = ("Passwort muss mindestens 8 Zeichen lang sein.");
            return returnArray;
        }
        if (!Pattern.compile(".*[0-9].*").matcher(password).find()) {
            returnArray[0] = ("Passwort Fehler");
            returnArray[1] = ("Passwort muss mindestens eine Zahl enthalten.");
            return returnArray;
        }
        if (!Pattern.compile(".*[a-z].*").matcher(password).find()) {
            returnArray[0] = ("Passwort Fehler");
            returnArray[1] = ("Passwort muss mindestens einen Kleinbuchstaben enthalten.");
            return returnArray;
        }
        if (!Pattern.compile(".*[A-Z].*").matcher(password).find()) {
            returnArray[0] = ("Passwort Fehler");
            returnArray[1] = ("Passwort muss mindestens einen Großbuchstaben enthalten.");
            return returnArray;
        }
        if (!Pattern.compile(".*[!@#&()–[{}]:;',?/*~$^+=<>\\\\-_].*").matcher(password).find()) {
            returnArray[0] = ("Passwort Fehler");
            returnArray[1] = ("Passwort muss mindestens ein Sonderzeichen enthalten (z.B. !@#$).");
            return returnArray;
        }
        return null;
    }
}
