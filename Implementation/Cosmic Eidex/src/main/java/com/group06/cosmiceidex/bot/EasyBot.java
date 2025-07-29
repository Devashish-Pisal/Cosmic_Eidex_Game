package com.group06.cosmiceidex.bot;

import com.group06.cosmiceidex.game.*;
import java.util.List;
import java.util.Random;

/**
 * Einfacher Bot der zufällige Aktionen ausführt.
 */
public class EasyBot extends Player {

    private final Random random = new Random();

    /**
     * Easybot mit Username erstellen
     * @param username Nutzername
     */
    public EasyBot(String username) {
        super(username);
        setBot(true);
    }

    /**
     * Wähle zufällige Karte zum Drücken
     * @return Ausgewählte Karte zum Drücken
     */
    public Card chooseCardToPress() {
        if (getHand().isEmpty()) {
            return null;
        }
        int randomIndex = random.nextInt(getHand().size());
        Card cardToPress = getHand().get(randomIndex);
        System.out.println(getUsername() + " drückt die Karte: " + cardToPress);
        return cardToPress;
    }

    /**
     * Wählt zufällige regelkonforme Karte
     * @param currentTrick Gespielte Karten
     * @param trumpColour Trumpffarbe falls vorhanden
     * @param mode Spielmodus
     * @return Gültige Karte zum spielen
     */
    public Card chooseCardToPlay(List<Card> currentTrick, Colour trumpColour, GameMode mode) {
        List<Card> legalMoves = GameRules.getLegalMoves(getHand(), currentTrick, trumpColour, mode);

        if (legalMoves.isEmpty()) {
            System.out.println("WARNUNG: " + getUsername() + " hat keine legalen Züge!");
            return getHand().get(random.nextInt(getHand().size()));
        }

        int randomIndex = random.nextInt(legalMoves.size());
        Card cardToPlay = legalMoves.get(randomIndex);

        System.out.println(getUsername() + " spielt die Karte: " + cardToPlay + " (Hand: " + getHand() + ")");
        return cardToPlay;
    }
}