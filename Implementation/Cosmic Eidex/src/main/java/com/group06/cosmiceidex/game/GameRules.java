package com.group06.cosmiceidex.game;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ---------------------------------------------------------------------------------------------------------------
 * ----------------------BOT-SIMULATIONSKLASSE FÜR EIGENTLICHE IMPLEMENTIERUNG IRRELEVANT-------------------------
 * ---------------------------------------------------------------------------------------------------------------
 */



/**
 * Klasse für die Regeln des Spiels
 */
public class GameRules {

    private static final Map<Value, Integer> TRUMP_POINTS = Map.of(
            Value.BUBE, 20, Value.NEUN, 14, Value.ASS, 11, Value.KOENIG, 4,
            Value.DAME, 3, Value.ZEHN, 10, Value.ACHT, 0, Value.SIEBEN, 0, Value.SECHS, 0
    );

    private static final Map<Value, Integer> OBENABE_POINTS = Map.of(
            Value.ASS, 11, Value.KOENIG, 4, Value.DAME, 3, Value.BUBE, 2, Value.ZEHN, 10,
            Value.NEUN, 0, Value.ACHT, 8, Value.SIEBEN, 0, Value.SECHS, 0
    );

    private static final Map<Value, Integer> UNDENUFE_POINTS = Map.of(
            Value.SECHS, 11, Value.SIEBEN, 0, Value.ACHT, 8, Value.NEUN, 0, Value.ZEHN, 10,
            Value.BUBE, 2, Value.DAME, 3, Value.KOENIG, 4, Value.ASS, 0
    );

    private static final List<Value> TRUMP_RANKING = List.of(
            Value.BUBE, Value.NEUN, Value.ASS, Value.KOENIG, Value.DAME, Value.ZEHN, Value.ACHT, Value.SIEBEN, Value.SECHS
    );

    private static final List<Value> NORMAL_RANKING = List.of(
            Value.ASS, Value.KOENIG, Value.DAME, Value.BUBE, Value.ZEHN, Value.NEUN, Value.ACHT, Value.SIEBEN, Value.SECHS
    );

    private static final List<Value> UNDENUFE_RANKING = List.of(
            Value.SECHS, Value.SIEBEN, Value.ACHT, Value.NEUN, Value.ZEHN, Value.BUBE, Value.DAME, Value.KOENIG, Value.ASS
    );

    /**
     * Gibt Punkte basierend auf Spielmodus zurück
     * @param card Karte
     * @param trumpColour Trumpffarbe falls Spielmodus = TRUMP sonst null
     * @param mode Spielmodus
     * @return Punktzahl der Karte
     */
    public static int getCardPoints(Card card, Colour trumpColour, GameMode mode) {
        if (mode == GameMode.TRUMP && card.getColour() == trumpColour) {
            return TRUMP_POINTS.getOrDefault(card.getValue(), 0);
        }
        if (mode == GameMode.OBENABE) {
            return OBENABE_POINTS.getOrDefault(card.getValue(), 0);
        }
        if (mode == GameMode.UNDENUFE) {

            return UNDENUFE_POINTS.getOrDefault(card.getValue(), 0);
        }

        return card.getValue().getPoints();
    }

    /**
     * Ermittelt Rangstärke der Karte je nach Spielmodus
     * @param card Karte
     * @param trumpColour Trumpffarbe falls vorhanden
     * @param mode Spielmodus
     * @param leadingColour Angespielte Farbe
     * @return Rang der Karte [Niedriger rang > hoher rang]
     */
    private static int getCardRank(Card card, Colour trumpColour, GameMode mode, Colour leadingColour) {
        List<Value> ranking;
        boolean isTrump = mode == GameMode.TRUMP && card.getColour() == trumpColour;
        boolean isLeadingColour = card.getColour() == leadingColour;

        if (isTrump) {
            ranking = TRUMP_RANKING;
        } else if (mode == GameMode.UNDENUFE) {
            ranking = UNDENUFE_RANKING;
        } else {
            ranking = NORMAL_RANKING;
        }

        int rank = ranking.indexOf(card.getValue());

        if (!isTrump && !isLeadingColour) {
            return Integer.MAX_VALUE;
        }
        return rank;
    }

    /**
     * Ermittelt gewinnerkarte eines Stichs je nach Modus
     * @param trick Karten des aktuellen Stichs
     * @param trumpColour Trumpffarbe falls vorhanden
     * @param mode Spielmodus
     * @return Karte die Stich gewinnt
     */
    public static Card getTrickWinner(List<Card> trick, Colour trumpColour, GameMode mode) {
        if (trick == null || trick.isEmpty()) {
            return null;
        }

        Card winningCard = trick.get(0);
        Colour leadingColour = winningCard.getColour();

        for (int i = 1; i < trick.size(); i++) {
            Card currentCard = trick.get(i);
            int winningCardRank = getCardRank(winningCard, trumpColour, mode, leadingColour);
            int currentCardRank = getCardRank(currentCard, trumpColour, mode, leadingColour);

            if (currentCardRank < winningCardRank) {
                winningCard = currentCard;
            }
        }
        return winningCard;
    }

    /**
     * Bestimme legale Spielzüge
     * @param hand Hand des Bots
     * @param currentTrick gespielte Karten
     * @param trumpColour Trumpffarbe falls vorhanden
     * @param mode Spielmodus
     * @return Liste der legalen Karten zum ausspielen
     */
    public static List<Card> getLegalMoves(List<Card> hand, List<Card> currentTrick, Colour trumpColour, GameMode mode) {
        if (currentTrick.isEmpty()) {
            return new ArrayList<>(hand);
        }

        Card leadingCard = currentTrick.get(0);
        Colour leadingColour = leadingCard.getColour();

        boolean isTrumpLeading = mode == GameMode.TRUMP && leadingColour == trumpColour;

        Card trumpJack = hand.stream()
                .filter(c -> c.getValue() == Value.BUBE && c.getColour() == trumpColour)
                .findFirst().orElse(null);

        List<Card> canFollowSuit = hand.stream()
                .filter(c -> c.getColour() == leadingColour)
                .collect(Collectors.toList());

        if (isTrumpLeading && trumpJack != null) {
            canFollowSuit.remove(trumpJack);
        }

        if (!canFollowSuit.isEmpty()) {
            List<Card> legalMoves = new ArrayList<>(canFollowSuit);
            if (mode == GameMode.TRUMP) {
                hand.stream()
                        .filter(c -> c.getColour() == trumpColour)
                        .forEach(legalMoves::add);
            }
            return legalMoves;
        }

        List<Card> trumpsInTrick = currentTrick.stream()
                .filter(c -> c.getColour() == trumpColour)
                .collect(Collectors.toList());

        if (mode != GameMode.TRUMP || trumpsInTrick.isEmpty()) {
            return new ArrayList<>(hand);
        }

        Card highestTrumpInTrick = getTrickWinner(trumpsInTrick, trumpColour, GameMode.TRUMP);
        int highestTrumpRank = TRUMP_RANKING.indexOf(highestTrumpInTrick.getValue());

        List<Card> legalMoves = new ArrayList<>();
        List<Card> playerTrumps = hand.stream().filter(c -> c.getColour() == trumpColour).collect(Collectors.toList());
        List<Card> higherTrumps = playerTrumps.stream()
                .filter(c -> TRUMP_RANKING.indexOf(c.getValue()) < highestTrumpRank)
                .collect(Collectors.toList());

        if (!higherTrumps.isEmpty()) {
            legalMoves.addAll(higherTrumps);
            hand.stream().filter(c -> c.getColour() != trumpColour).forEach(legalMoves::add);
        } else {
            List<Card> nonTrumps = hand.stream().filter(c -> c.getColour() != trumpColour).collect(Collectors.toList());
            if (!nonTrumps.isEmpty()) {
                legalMoves.addAll(nonTrumps);
            } else {
                legalMoves.addAll(playerTrumps);
            }
        }

        if (trumpJack != null && !legalMoves.contains(trumpJack)) {
            legalMoves.add(trumpJack);
        }

        return legalMoves;
    }
}