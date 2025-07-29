package com.group06.cosmiceidex.game;

import com.group06.cosmiceidex.bot.EasyBot;
import com.group06.cosmiceidex.bot.HardBot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * ---------------------------------------------------------------------------------------------------------------
 * ----------------------BOT-SIMULATIONSKLASSE FÜR EIGENTLICHE IMPLEMENTIERUNG IRRELEVANT-------------------------
 * ---------------------------------------------------------------------------------------------------------------
 */




/**
 * Simuliert Spielrunde
 */
public class GameController {
    private final List<PlayerInterface> players;
    private int dealerIndex;
    private GameMode gameMode;
    private Colour trumpColour;

    private Map<PlayerInterface, Card> pressedCards;
    private Map<PlayerInterface, List<Card>> wonTricks;

    private static class TrickPlay {
        final PlayerInterface player;
        final Card card;
        TrickPlay(PlayerInterface player, Card card) { this.player = player; this.card = card; }
        PlayerInterface getPlayer() { return player; }
        Card getCard() { return card; }
    }

    /**
     * Konstruktur -> Initialisierung der Spieler und setzt Geber
     * @param p1 1. Spieler (Geber)
     * @param p2 2. Spieler
     * @param p3 3. Spieler
     */
    public GameController(PlayerInterface p1, PlayerInterface p2, PlayerInterface p3) {
        this.players = List.of(p1, p2, p3);
        this.dealerIndex = 0;
    }

    /**
     * Spielrunde durchführen
     */
    public void playRound() {
        System.out.println("--- Neue Runde beginnt ---");
        setupRound();
        handlePressing();
        playAllTricks();
        calculateAndShowScores();
        System.out.println("--- Runde beendet ---\n");
        this.dealerIndex = (this.dealerIndex + 1) % 3;
    }

    /**
     * Kartenmischen/austeilen, Spielmodus und Spielerhände/Punktestände
     */
    private void setupRound() {
        this.pressedCards = new HashMap<>();
        this.wonTricks = new HashMap<>();

        for (PlayerInterface player : players) {
            player.clearHand();
            player.setSumOfTrickPoints(0);
            this.wonTricks.put(player, new ArrayList<>());
        }

        Deck deck = new Deck();
        deck.shuffle();

        for(int i = 0; i < players.size(); i++) {
            if (i != dealerIndex) {
                for (int j = 0; j < 12; j++) {
                    players.get(i).addCardToHand(deck.dealCard());
                }
            } else {
                for (int j = 0; j < 11; j++) {
                    players.get(i).addCardToHand(deck.dealCard());
                }
            }
        }


        Card trumpCard = deck.dealCard();
        System.out.println("Aufgedeckte Trumpfkarte: " + trumpCard);


        players.get(dealerIndex).addCardToHand(trumpCard);

        if (trumpCard.getValue() == Value.ASS) {
            this.gameMode = GameMode.OBENABE;
            this.trumpColour = null;
            System.out.println("Spielmodus ist OBENABE");
        } else if (trumpCard.getValue() == Value.SECHS) {
            this.gameMode = GameMode.UNDENUFE;
            this.trumpColour = null;
            System.out.println("Spielmodus ist UNDENUFE");
        } else {
            this.gameMode = GameMode.TRUMP;
            this.trumpColour = trumpCard.getColour();
            System.out.println("Trumpffarbe ist " + this.trumpColour);
        }
    }

    /**
     * Funktion für das Drücken der Karten
     */
    private void handlePressing() {
        System.out.println("\n--- Drücken Phase ---");
        for (PlayerInterface player : players) {
            Card pressedCard;
            if (player instanceof HardBot) {
                pressedCard = ((HardBot) player).chooseCardToPress(this.gameMode, this.trumpColour);
            } else if (player instanceof EasyBot) {
                pressedCard = ((EasyBot) player).chooseCardToPress();
            } else {
                pressedCard = player.getHand().get(0);
            }
            player.playCard(pressedCard);
            this.pressedCards.put(player, pressedCard);
        }
    }

    /**
     * Führt 11 Stiche gemäß der Spielregeln durch
     */
    private void playAllTricks() {
        int currentPlayerIndex = (this.dealerIndex + 1) % 3;
        PlayerInterface lastTrickWinner = null;

        for (int i = 0; i < 11; i++) {
            System.out.println("\n--- Stich " + (i + 1) + " ---");
            List<Card> currentTrickCards = new ArrayList<>();
            List<TrickPlay> trickPlays = new ArrayList<>();

            for (int j = 0; j < 3; j++) {
                PlayerInterface currentPlayer = players.get(currentPlayerIndex);
                Card playedCard;

                if (currentPlayer instanceof HardBot) {
                    playedCard = ((HardBot) currentPlayer).chooseCardToPlay(currentTrickCards, this.trumpColour, this.gameMode);
                } else if (currentPlayer instanceof EasyBot) {
                    playedCard = ((EasyBot) currentPlayer).chooseCardToPlay(currentTrickCards, this.trumpColour, this.gameMode);
                } else {
                    playedCard = currentPlayer.getHand().get(0);
                }

                currentPlayer.playCard(playedCard);
                currentTrickCards.add(playedCard);
                trickPlays.add(new TrickPlay(currentPlayer, playedCard));
                currentPlayerIndex = (currentPlayerIndex + 1) % 3;
            }

            Card winningCard = GameRules.getTrickWinner(currentTrickCards, this.trumpColour, this.gameMode);
            PlayerInterface trickWinner = trickPlays.stream()
                    .filter(play -> play.getCard().equals(winningCard))
                    .map(TrickPlay::getPlayer)
                    .findFirst().orElse(null);

            if (trickWinner != null) {
                int trickPoints = currentTrickCards.stream()
                        .mapToInt(c -> GameRules.getCardPoints(c, this.trumpColour, this.gameMode))
                        .sum();
                System.out.println(trickWinner.getUsername() + " gewinnt den Stich mit der Karte " + winningCard + " für " + trickPoints + " Punkte.");

                trickWinner.incrementSumOfTrickPoints(trickPoints);
                this.wonTricks.get(trickWinner).addAll(currentTrickCards);

                currentPlayerIndex = players.indexOf(trickWinner);
                lastTrickWinner = trickWinner;
            }
        }

        if (lastTrickWinner != null) {
            System.out.println(lastTrickWinner.getUsername() + " erhält 5 Extrapunkte für den letzten Stich.");
            lastTrickWinner.incrementSumOfTrickPoints(5);
        }
    }

    /**
     * Ausgabe des Endstands
     */
    private void calculateAndShowScores() {
        System.out.println("\n--- Endabrechnung der Runde ---");
        for(PlayerInterface player : players) {
            Card pressedCard = this.pressedCards.get(player);
            int pressedCardPoints = GameRules.getCardPoints(pressedCard, this.trumpColour, this.gameMode);

            player.incrementSumOfTrickPoints(pressedCardPoints);

            System.out.println("\nSpieler: " + player.getUsername());
            System.out.println("  Gewonnene Karten: " + this.wonTricks.get(player));
            System.out.println("  Gedrückte Karte: " + pressedCard + " (" + pressedCardPoints + " Pkt.)");
            System.out.println("  Gesamtpunkte: " + player.getSumOfTrickPoints());
        }
    }
}