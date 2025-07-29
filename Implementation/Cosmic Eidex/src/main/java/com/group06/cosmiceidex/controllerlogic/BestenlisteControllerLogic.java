package com.group06.cosmiceidex.controllerlogic;

import com.group06.cosmiceidex.common.LeaderboardEntry;

public class BestenlisteControllerLogic {
    /**
     * Erstellt den Eintrag für einen Spieler in der Bestenliste
     * @param entry Der Eintrag
     * @param i Die Ranglistenposition
     * @return Der fertige String zum Einfügen in die Bestenliste
     */
    public static String createEntry(LeaderboardEntry entry, int i){
        String text = String.format("%d. : %s | Siege: %d | Stiche: %d | Punkte: %d",
                i + 1,
                entry.getUserName(),
                entry.getWins(),
                entry.getTricks(),
                entry.getPoints());
        return text;
    }
}
