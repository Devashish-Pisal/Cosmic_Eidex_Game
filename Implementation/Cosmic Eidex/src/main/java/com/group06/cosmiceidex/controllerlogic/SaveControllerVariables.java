package com.group06.cosmiceidex.controllerlogic;

/**
 * Klasse zum Speichern von Variablen, auf die alle Controllerklassen zugreifen können sollen.
 * Die Variablen sind keiner spezifischen Klasse zugeordnet und daher extern gespeichert.
 */

public class SaveControllerVariables {
    private static String prevScene = null;

    private static String selectedSpielraum = null;

    public static String getPrevScene() {
        return prevScene;
    }

    public static void setPrevScene(String prevScene) {
        SaveControllerVariables.prevScene = prevScene;
    }

    public static String getSelectedSpielraum() {
        return selectedSpielraum;
    }

    public static void setSelectedSpielraum(String selectedSpielraum) {
        SaveControllerVariables.selectedSpielraum = selectedSpielraum;
    }
}
