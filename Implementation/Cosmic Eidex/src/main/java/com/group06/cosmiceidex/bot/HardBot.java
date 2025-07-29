package com.group06.cosmiceidex.bot;

import com.group06.cosmiceidex.game.*;

import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Verbesserter Bot der sich dem aktuellen Spielmodus anpasst.
 */
public class HardBot extends Player {

    /**
     * Erstellt einen Hardbot mit übergebenem Nutzernamen
     * @param username Nutzername
     */
    public HardBot(String username) {
        super(username);
        setBot(true);
    }

    /**
     * Wählt eine Karte basierend auf dem Spielmodus
     * @param mode Spielmodus
     * @param trumpColour Trumpffarbe falls vorhanden
     * @return Zu drückende Karte
     */
    public Card chooseCardToPress(GameMode mode, Colour trumpColour) {
        List<Card> hand = getHand();
        Card cardToPress = null;

        if (hand.isEmpty()) return null;

        switch (mode) {
            case OBENABE:
                // Lege die schwächste Karte ab
                cardToPress = hand.stream()
                        .min(Comparator.comparingInt(c -> GameRules.getCardPoints(c, null, GameMode.OBENABE)))
                        .orElse(hand.get(0));
                break;
            case UNDENUFE:
                // Lege die höchste Karte != 6 ab
                cardToPress = hand.stream()
                        .filter(c -> c.getValue() != Value.SECHS)
                        .max(Comparator.comparingInt(c -> c.getValue().ordinal()))
                        .orElse(hand.get(0));
                break;

            case TRUMP:
            default:
                // Lege eine Karte mit 0 Punkten von einer kurzen Farbe ab
                cardToPress = hand.stream()
                        .filter(c -> GameRules.getCardPoints(c, trumpColour, mode) == 0 && c.getColour() != trumpColour)
                        .min(Comparator.comparingInt(c -> (int) hand.stream().filter(h -> h.getColour() == c.getColour()).count()))
                        .orElse(hand.get(0));
                break;
        }

        System.out.println(getUsername() + " drückt die Karte: " + cardToPress);
        return cardToPress;
    }

    /**
     * Wählt Karte zum Spielen basierend auf Spielmodus
     * @param currentTrick Gespielte Karten
     * @param trumpColour Trumpffarbe falls vorhanden
     * @param mode Spielmodus
     * @return Zu spielende Karte
     */
    public Card chooseCardToPlay(List<Card> currentTrick, Colour trumpColour, GameMode mode) {
        List<Card> legalMoves = GameRules.getLegalMoves(getHand(), currentTrick, trumpColour, mode);

        if (legalMoves.size() == 1) {
            Card cardToPlay = legalMoves.get(0);
            System.out.println(getUsername() + " (Hard) spielt die einzige legale Karte: " + cardToPlay + " (Hand: " + getHand() + ")");
            return cardToPlay;
        }

        Card choice;
        if (currentTrick.isEmpty()) {
            choice = chooseCardToLead(legalMoves, trumpColour, mode);
        } else {
            choice = chooseCardToFollow(legalMoves, currentTrick, trumpColour, mode);
        }

        System.out.println(getUsername() + " (Hard) spielt die Karte: " + choice + " (Hand: " + getHand() + ")");
        return choice;
    }

    /**
     * Strategie falls Bot Stich eröffnet
     * @param legalMoves Liste gültiger Karten
     * @param trumpColour Trumpffarbe falls vorhanden
     * @param mode Spielmodus
     * @return Zu spielende Karte
     */
    private Card chooseCardToLead(List<Card> legalMoves, Colour trumpColour, GameMode mode) {
        Comparator<Card> rankComparator = (c1, c2) -> {
            Card winning = GameRules.getTrickWinner(List.of(c1, c2), trumpColour, mode);
            return winning.equals(c1) ? -1 : 1;
        };

        if (mode == GameMode.UNDENUFE) {
            return legalMoves.stream().min(rankComparator).orElse(legalMoves.get(0));
        } else {
            return legalMoves.stream().max(rankComparator).orElse(legalMoves.get(0));
        }
    }

    /**
     * Strategie falls Bot nicht den Stich eröffnet
     * @param legalMoves Liste gültiger Karten
     * @param currentTrick Aktuelle Karten im Stich
     * @param trumpColour Trumpffarbe falls vorhanden
     * @param mode Spielmodus
     * @return Zu spielende Karte
     */
    private Card chooseCardToFollow(List<Card> legalMoves, List<Card> currentTrick, Colour trumpColour, GameMode mode) {
        Card currentWinningCard = GameRules.getTrickWinner(currentTrick, trumpColour, mode);

        List<Card> winningMoves = legalMoves.stream()
                .filter(move -> {
                    List<Card> potentialTrick = new ArrayList<>(currentTrick);
                    potentialTrick.add(move);
                    return GameRules.getTrickWinner(potentialTrick, trumpColour, mode).equals(move);
                })
                .collect(Collectors.toList());

        if (!winningMoves.isEmpty()) {
            // Spiele billigste Gewinnkarte bei chance auf Sieg
            Comparator<Card> rankComparator = (c1, c2) -> {
                Card winning = GameRules.getTrickWinner(List.of(c1, c2), trumpColour, mode);
                return winning.equals(c1) ? -1 : 1;
            };

            // Bei Udenufe höchster Rang
            // Bei Obenabe/Trump niedrigster Rang
            // (Siehe GameRules.java -> getCardRank)
            if (mode == GameMode.UNDENUFE) {
                return winningMoves.stream().max(rankComparator).orElse(winningMoves.get(0));
            } else {
                return winningMoves.stream().min(rankComparator).orElse(winningMoves.get(0));
            }
        } else {
            // Falls gewinnen nicht möglich spiele schlechteste Karte um sie los zu werden
            if (mode == GameMode.UNDENUFE) {
                return legalMoves.stream()
                        .max(Comparator.comparingInt(c -> c.getValue().ordinal()))
                        .orElse(legalMoves.get(0));
            } else {
                return legalMoves.stream()
                        .min(Comparator.comparingInt(c -> GameRules.getCardPoints(c, trumpColour, mode)))
                        .orElse(legalMoves.get(0));
            }
        }
    }
}