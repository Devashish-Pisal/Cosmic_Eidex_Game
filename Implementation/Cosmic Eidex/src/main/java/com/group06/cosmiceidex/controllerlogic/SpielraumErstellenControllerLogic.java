package com.group06.cosmiceidex.controllerlogic;

import com.group06.cosmiceidex.common.Message;
import com.group06.cosmiceidex.common.RoomCredential;

public class SpielraumErstellenControllerLogic {
    /**
     * Validiert die Eingaben bei der Erstellung eines Spielraums. Gibt null zurück, falls alles gültig ist
     * @param gameroomname Der Name für den Spielraum
     * @param password Das Passwort
     * @param repeatedPassword Das wiederholte Passwort
     * @return Ein Array mit Titel und Inhalt der Fehlermeldung. Null, falls alles gültig ist
     */
    public static String[] validateGameroom(String gameroomname, String password, String repeatedPassword){
        String[] returnArray = new String[2];
        if(gameroomname.isEmpty()){
            returnArray[0] = ("Spielraum Name Fehler");
            returnArray[1] = ("Spielraum Name darf nicht leer sein!");
            return returnArray;
        }
        else if(gameroomname.contains(" ")){
            returnArray[0] = ("Spielraum Name Fehler");
            returnArray[1] = ("Spielraum Name darf kein Leerzeichen erhalten!");
            return returnArray;
        }
        else if(gameroomname.equals("LOBBY") || gameroomname.equals("Lobby")){
            returnArray[0] = ("Spielraum Name Fehler");
            returnArray[1] = ("Spielraum Name darf nicht 'LOBBY' sein!");
            return returnArray;
        }
        else if(gameroomname.length() > 10) {
            returnArray[0] = ("Spielraum Name Fehler");
            returnArray[1] = ("Spielraum Name ist zu groß!");
            return returnArray;
        }
        else if(password.isEmpty()){
            returnArray[0] = ("Passwort Fehler");
            returnArray[1] = ("Passwort Feld darf nicht leer sein!");
            return returnArray;
        }
        else if(repeatedPassword.isEmpty()){
            returnArray[0] = ("Passwort Fehler");
            returnArray[1] = ("Das wiederholte Passwort Feld darf nicht leer sein!");
            return returnArray;
        }
        else if(password.contains(" ")){
            returnArray[0] = ("Passwort Fehler");
            returnArray[1] = ("Das Passwort Feld darf kein Leerzeichen enthalten!");
            return returnArray;
        }
        else if(!password.equals(repeatedPassword)){
            returnArray[0] = ("Passwort Fehler");
            returnArray[1] = ("Die Passwörter sind nicht gleich!");
            return returnArray;
        }
        else if(password.length() > 10){
            returnArray[0] = ("Passwort Fehler");
            returnArray[1] = ("Passwort ist zu groß!");
            return returnArray;
        }
        else {
            return null;
        }
    }
}
