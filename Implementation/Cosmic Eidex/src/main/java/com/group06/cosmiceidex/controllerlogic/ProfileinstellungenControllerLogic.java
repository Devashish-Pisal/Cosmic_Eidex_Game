package com.group06.cosmiceidex.controllerlogic;

import com.group06.cosmiceidex.exceptions.AuthException;

import java.util.regex.Pattern;

public class ProfileinstellungenControllerLogic {
    /**
     * Validiert die Eingabe des neuen Passworts und dessen Wiederholung.
     *
     * @param password         Das gewünschte Passwort
     * @param repeatedPassword Die Wiederholung des gewünschten Passworts
     * @return boolean, ob die Eingaben valide sind.
     * @throws AuthException
     */
    public static String[] validatePassword(String password, String repeatedPassword) throws AuthException {
        String[] returnArray = new String[2];

        // Prüfen, dass Passwort nicht leer ist
        if (password == null || password.isEmpty()) {
            returnArray[0] = ("Passwort Fehler");
            returnArray[1] = ("Passwort darf nicht leer sein!");
            return returnArray;
        }
        // Prüfen, dass Passwort und Wiederholung übereinstimmen
        if (!password.equals(repeatedPassword)) {
            returnArray[0] = ("Passwort Fehler");
            returnArray[1] = ("Die Passwörter sind nicht gleich.");
            return returnArray;
        }
        if (password.contains(" ")) {
            returnArray[0] = ("Passwort Fehler");
            returnArray[1] = ("Das Passwort darf kein Leerzeichen enthalten.");
            return returnArray;
        }
        if (password.length() < 8) {
            returnArray[0] = ("Passwort Fehler");
            returnArray[1] = ("Passwort muss mindestens 8 Zeichen lang sein.");
            return returnArray;
        }
        if (!Pattern.compile(".*[0-9].*").matcher(password).find()) {
            returnArray[0] = ("Passwort Fehler");
            returnArray[1] = ("Passwort muss mindestens einen Zahl enthalten.");
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
