package com.group06.cosmiceidex.bots;

import com.group06.cosmiceidex.game.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import java.io.Serializable;

/**
 * Fortgeschrittener Bot
 */
public class HardBot implements PlayerInterface, Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private List<Card> hand;
    private boolean isBot;
    private boolean isGameMaster;
    private boolean isMyTurn;
    private int sumOfTrickPoints;
    private int wonPartyPoints;
    private int numberOfWonTricksDuringEntireGame;
    private int totalWonTrickPointDuringEntireGame;


    private GameSession gameSession;

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
     * Konstruktor für HardBot
     */
    public HardBot(String username) {
        this.username = username;
        this.hand = new ArrayList<>();
        this.isBot = true;
        this.isGameMaster = false;
        this.isMyTurn = false;
        this.sumOfTrickPoints = 0;
        this.wonPartyPoints = 0;
        this.numberOfWonTricksDuringEntireGame = 0;
        this.totalWonTrickPointDuringEntireGame = 0;
        this.gameSession = null;
    }


    /**
     * Wählt Karte zum Drücken je nach Spielmodus
     * @return zu drückende Karte
     */
    @Override
    public Card chooseCardToPress() {
        if (gameSession == null) return fallbackRandomCard();

        GameType mode = gameSession.getGameType();
        Colour trumpColour = gameSession.getTrumpSuit();
        List<Card> currentHand = getHand();
        if (currentHand.isEmpty()) return null;

        System.out.println("Debug hand: " + currentHand);

        Card cardToPress;

        switch (mode) {
            case Obenabe:
                cardToPress = currentHand.stream()
                        .min(Comparator.comparingInt(c -> this.calculateCardPoints(c, mode, trumpColour)))
                        .orElse(currentHand.get(0));
                break;
            case Undenuf:
                cardToPress = currentHand.stream()
                        .filter(c -> c.getValue() != Value.SECHS)
                        .max(Comparator.comparingInt(c -> c.getValue().ordinal()))
                        .orElse(currentHand.get(0));
                break;
            case Normal:
            default:
                cardToPress = currentHand.stream()
                        .filter(c -> this.calculateCardPoints(c, mode, trumpColour) == 0 && c.getColour() != trumpColour)
                        .min(Comparator.comparingInt(c -> (int) currentHand.stream().filter(h -> h.getColour() == c.getColour()).count()))
                        .orElse(currentHand.get(0));
                break;
        }
        System.out.println(getUsername() + " drückt die Karte: " + cardToPress);
        getHand().remove(cardToPress);
        return cardToPress;
    }

    /**
     * Wählt Karte zum Spielen unter berücksichtigung von Farbe, aktuelle Karten im Stich, Trumpf und Spielmodus
     * @param leadSuit Angespielte Farbe
     * @param currentTrick bereits gespielte Karten im Stich
     * @param TrumpCard Trumpfkarte
     * @param type Spielmodus
     * @return Zu spielende Karte
     */
    @Override
    public Card chooseCardToPlay(Colour leadSuit, ConcurrentHashMap<String, Card> currentTrick, Card TrumpCard, GameType type) {
        if (gameSession == null) return fallbackRandomCard();

        List<Card> currentTrickList = new ArrayList<>(currentTrick.values());
        GameType mode = type;
        Colour trumpColour = (mode == GameType.Normal && TrumpCard != null) ? TrumpCard.getColour() : null;

        List<Card> legalMoves = this.getLegalMoves(getHand(), currentTrickList, trumpColour, mode);


        if (legalMoves.isEmpty()) {
            //Sollte nicht passieren
            return fallbackRandomCard();
        }

        if (legalMoves.size() == 1) {
            return legalMoves.get(0);
        }

        Card choice = currentTrickList.isEmpty()
                ? chooseCardToLead(legalMoves, trumpColour, mode)
                : chooseCardToFollow(legalMoves, currentTrickList, trumpColour, mode);

        System.out.println(getUsername() + " (Hard) spielt die Karte: " + choice);
        return choice;
    }

    /**
     * Wählt Karte zum eröffnen
     * @param legalMoves Legale Karten
     * @param trumpColour Trumpffarbe
     * @param mode Spielmodus
     * @return Zu spielende Karte
     */
    private Card chooseCardToLead(List<Card> legalMoves, Colour trumpColour, GameType mode) {
        Comparator<Card> rankComparator = (c1, c2) -> {
            Card winning = this.getTrickWinner(List.of(c1, c2), trumpColour, mode);
            return winning.equals(c1) ? -1 : 1;
        };
        return (mode == GameType.Undenuf)
                ? legalMoves.stream().min(rankComparator).orElse(legalMoves.get(0))
                : legalMoves.stream().max(rankComparator).orElse(legalMoves.get(0));
    }

    /**
     * Wählt Karte für laufenden Stich
     * @param legalMoves Legale Karten
     * @param currentTrick Karten im Stich
     * @param trumpColour Trumpffarbe
     * @param mode Spielmodus
     * @return Zu spielende Karte
     */
    private Card chooseCardToFollow(List<Card> legalMoves, List<Card> currentTrick, Colour trumpColour, GameType mode) {
        List<Card> winningMoves = legalMoves.stream()
                .filter(move -> {
                    List<Card> potentialTrick = new ArrayList<>(currentTrick);
                    potentialTrick.add(move);
                    return this.getTrickWinner(potentialTrick, trumpColour, mode).equals(move);
                })
                .collect(Collectors.toList());

        if (!winningMoves.isEmpty()) {
            Comparator<Card> rankComparator = (c1, c2) -> {
                Card winning = this.getTrickWinner(List.of(c1, c2), trumpColour, mode);
                return winning.equals(c1) ? -1 : 1;
            };
            return (mode == GameType.Undenuf)
                    ? winningMoves.stream().max(rankComparator).orElse(winningMoves.get(0))
                    : winningMoves.stream().min(rankComparator).orElse(winningMoves.get(0));
        } else {
            return (mode == GameType.Undenuf)
                    ? legalMoves.stream().max(Comparator.comparingInt(c -> c.getValue().ordinal())).orElse(legalMoves.get(0))
                    : legalMoves.stream().min(Comparator.comparingInt(c -> this.calculateCardPoints(c, mode, trumpColour))).orElse(legalMoves.get(0));
        }
    }

    /**
     * Gibt zufällige Karte zurück, wenn keine GameSession vorhanden ist.
     * Diese Funktion ist eine Notfall funktion, falls andere Teile des Codes nicht wie gewollt funktionieren.
     * @return random Karte oder null
     */
    private Card fallbackRandomCard() {
        System.err.println("HardBot " + username + " hat keine GameSession Referenz oder keine legalen Karten!");
        if (getHand() == null || getHand().isEmpty()) return null;
        return getHand().get(new Random().nextInt(getHand().size()));
    }

    /**
     * Berechnet Kartenpunkte
     * @param card Zu bewertende Karte
     * @param gameType Spielmodus
     * @param trumpSuit Trumpffarbe
     * @return Punktzahl der Karte
     */
    private int calculateCardPoints(Card card, GameType gameType, Colour trumpSuit) {
        if (gameType == null) return 0;
        switch (gameType) {
            case Obenabe:
                switch (card.getValue()) {
                    case ASS: return 11;
                    case KOENIG: return 4;
                    case DAME: return 3;
                    case BUBE: return 2;
                    case ZEHN: return 10;
                    case ACHT: return 8;
                    default: return 0;
                }
            case Undenuf:
                switch (card.getValue()) {
                    case SECHS: return 11;
                    case ACHT: return 8;
                    case KOENIG: return 4;
                    case DAME: return 3;
                    case BUBE: return 2;
                    case ZEHN: return 10;
                    default: return 0;
                }
            case Normal:
                switch (card.getValue()) {
                    case BUBE: return (card.getColour() == trumpSuit) ? 20 : 2;
                    case NEUN: return (card.getColour() == trumpSuit) ? 14 : 0;
                    case ASS: return 11;
                    case KOENIG: return 4;
                    case DAME: return 3;
                    case ZEHN: return 10;
                    default: return 0;
                }
        }
        return 0;
    }


    /**
     * Bestimmt Rang von einer Karte in abhänigkeit von Spielmodus
     * @param card Zu bewertende Karte
     * @param trumpColour Trumpffarbe
     * @param mode Spielmodus
     * @param leadingColour Zuerst gespielte Karte
     * @return Rang der Karte (kleiner = besser)
     */
    private int getCardRank(Card card, Colour trumpColour, GameType mode, Colour leadingColour) {
        List<Value> ranking;
        boolean isTrump = mode == GameType.Normal && card.getColour() == trumpColour;

        if (isTrump) {
            ranking = TRUMP_RANKING;
        } else if (mode == GameType.Undenuf) {
            ranking = UNDENUFE_RANKING;
        } else {
            ranking = NORMAL_RANKING;
        }

        int rank = ranking.indexOf(card.getValue());

        if (mode == GameType.Normal && !isTrump && card.getColour() != leadingColour) {
            return Integer.MAX_VALUE;
        }
        return rank;
    }

    /**
     * Ermittelt Gewinnerkarte eines Stichs
     * @param trick Karten im Stich
     * @param trumpColour Trumpffarbe
     * @param mode Spielmodus
     * @return Karte die gewinnt
     */
    private Card getTrickWinner(List<Card> trick, Colour trumpColour, GameType mode) {
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
     * Findet legale Karten
     * @param hand Hand des Bots
     * @param currentTrick Karten im Stich
     * @param trumpColour Trumpffarbe
     * @param mode Spielmodus
     * @return Liste legaler Karten
     */
    //Im folgenden habe ich die Regeln konkret hingeschrieben falls ihr einen Fehler seht bitte melden (TB)
    public List<Card> getLegalMoves(List<Card> hand, List<Card> currentTrick, Colour trumpColour, GameType mode) {
        // Wenn der Bot den Stich eröffnet, darf er jede Karte spielen.
        if (currentTrick.isEmpty()) {
            return new ArrayList<>(hand);
        }

        Colour leadingColour = currentTrick.get(0).getColour();
        List<Card> possibleMoves = new ArrayList<>();

        // angespielte Farbe muss bedient werden darf aber eingestochen werden
        // Sonderregel Trumpf-Bube
        boolean hasTrumpJack = hand.stream().anyMatch(c -> c.getValue() == Value.BUBE && c.getColour() == trumpColour);
        if (mode == GameType.Normal && leadingColour == trumpColour && hasTrumpJack) {
            // Der Spieler ist von Bedienpflicht befreit und darf jede Karte spielen.
            possibleMoves.addAll(hand);
        } else {
            // Angespielte Farbe oder Trumpf spielen wenn möglich.
            List<Card> followingSuitCards = hand.stream()
                    .filter(c -> c.getColour() == leadingColour)
                    .collect(Collectors.toList());

            List<Card> trumpCards = new ArrayList<>();
            if (mode == GameType.Normal) {
                trumpCards = hand.stream()
                        .filter(c -> c.getColour() == trumpColour)
                        .collect(Collectors.toList());
            }

            if (!followingSuitCards.isEmpty() || !trumpCards.isEmpty()) {
                possibleMoves.addAll(followingSuitCards);
                possibleMoves.addAll(trumpCards);
            } else {
                // Kann weder bedienen noch trumpfen (kann alles spielen)
                possibleMoves.addAll(hand);
            }
        }

        // Wenn nicht im Trumpf gibt es keine Trumpf- oder Untertrumpf-Regeln.
        if (mode != GameType.Normal) {
            return possibleMoves;
        }

        // Untertrumpfen verboten
        Card highestTrumpInTrick = currentTrick.stream()
                .filter(c -> c.getColour() == trumpColour)
                .min(Comparator.comparingInt(c -> getCardRank(c, trumpColour, mode, trumpColour))) // Niedrigerer Rang-Wert ist besser
                .orElse(null);

        // Wenn kein Trumpf im Stich liegt ist Untertrumpfen egal
        if (highestTrumpInTrick == null) {
            return possibleMoves;
        }

        int highestTrumpRank = getCardRank(highestTrumpInTrick, trumpColour, mode, trumpColour);

        // Existiert nicht Trumpf Karte oder höherer Trumpf?
        boolean hasAlternative = possibleMoves.stream()
                .anyMatch(c -> c.getColour() != trumpColour || getCardRank(c, trumpColour, mode, trumpColour) < highestTrumpRank);

        if (hasAlternative) {
            // Ja
            return possibleMoves.stream()
                    .filter(c -> c.getColour() != trumpColour || getCardRank(c, trumpColour, mode, trumpColour) < highestTrumpRank)
                    .collect(Collectors.toList());
        } else {
            // Nein Bot muss untertrumpfen
            return possibleMoves;
        }
    }

    /**
     * Karte Hand des Bots hinzufügen
     * @param card hinzuzufügende Karte
     */
    @Override
    public void addCardToHand(Card card) { if (this.hand != null) this.hand.add(card); }


    /**
     * Entfernt Karte aus Bot Hand
     * @param card zu entfernende Karte
     */
    @Override
    public void playCard(Card card) { if (this.hand != null) this.hand.remove(card); }

    /**
     * Erhöht Punktesumme
     * @param points zu addierende Punkte
     */
    @Override
    public void incrementSumOfTrickPoints(int points) { this.sumOfTrickPoints += points; }

    /**
     * Erhöht Punktesumme
     * @param points zu addierende Punkte
     */
    @Override
    public void incrementWonPartyPoints(int points) { this.wonPartyPoints += points; }

    /**
     * Entfernt alle Karten
     */
    @Override
    public void clearHand() { if (this.hand != null) this.hand.clear(); }

    // Getter und Setter
    @Override
    public String getUsername() { return this.username; }
    @Override
    public List<Card> getHand() { return this.hand; }
    @Override
    public void setHand(List<Card> hand) { this.hand = hand; }
    @Override
    public int getSumOfTrickPoints() { return this.sumOfTrickPoints; }
    @Override
    public void setSumOfTrickPoints(int sum) { this.sumOfTrickPoints = sum; }
    @Override
    public int getWonPartyPoints() { return this.wonPartyPoints; }
    @Override
    public void setWonPartyPoints(int points) { this.wonPartyPoints = points; }
    @Override
    public boolean isBot() { return this.isBot; }
    @Override
    public void setBot(boolean bot) { this.isBot = bot; }
    @Override
    public boolean isGameMaster() { return this.isGameMaster; }
    @Override
    public void setGameMaster(boolean isGameMaster) { this.isGameMaster = isGameMaster; }
    @Override
    public boolean isMyTurn() { return this.isMyTurn; }
    @Override
    public void setMyTurn(boolean myTurn) { this.isMyTurn = myTurn; }
    @Override
    public int getNumberOfWonTricksDuringEntireGame() { return this.numberOfWonTricksDuringEntireGame; }
    @Override
    public void setNumberOfWonTricksDuringEntireGame(int num) { this.numberOfWonTricksDuringEntireGame = num; }
    @Override
    public int getTotalWonTrickPointDuringEntireGame() { return this.totalWonTrickPointDuringEntireGame; }
    @Override
    public void setTotalWonTrickPointDuringEntireGame(int points) { this.totalWonTrickPointDuringEntireGame = points; }
    //Nötig um GameSession zu holen
    public void setGameSession(GameSession gameSession) {
        if (this.isBot()) {
            this.gameSession = gameSession;
        }
    }
}