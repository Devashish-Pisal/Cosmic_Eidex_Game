package com.group06.cosmiceidex.game;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class Player implements PlayerInterface, Serializable {
    // Data to store in DB
    private int numberOfWonTricksDuringEntireGame;
    private int totalWonTrickPointDuringEntireGame;




    private List<Card> hand;
    private final String username;

    private int sumOfTrickPoints;
    private int wonPartyPoints;

    private boolean isBot = false;
    private boolean isGameMaster;

    private boolean isMyTurn;

    /**
     * Konstruktor der Klasse Spieler
     * @param user Benutzername der Spieler
     */
    public Player(String user) {
        this.username = user;
        this.hand = new ArrayList<>();
        this.sumOfTrickPoints = 0;
        this.wonPartyPoints = 0;
        this.numberOfWonTricksDuringEntireGame = 0;
        this.totalWonTrickPointDuringEntireGame = 0;
    }

    /**
     * Karte im Hand einfügen
     * @param card Karte, der im Hand eingefügt werden soll
     */
    @Override
    public void addCardToHand(Card card) {
        if (card != null) {
            this.hand.add(card);
        }
    }

    /**
     * Karte ausspielen
     * @param card
     */
    @Override public void playCard(Card card) {
        this.hand.remove(card);
    }

    /**
     * Hand des Spielers leeren
     */
    @Override public void clearHand() {
        this.hand.clear();
    }




    // Getter-Setter Methoden

    @Override public String getUsername() {
        return username;
    }

    @Override public List<Card> getHand() {
        return new ArrayList<>(hand);
    }

    @Override public void setHand(List<Card> hand) {this.hand = hand;}

    @Override public int getSumOfTrickPoints() {
        return sumOfTrickPoints;
    }

    @Override public void setSumOfTrickPoints(int newPoints) {
        this.sumOfTrickPoints = newPoints;
    }

    @Override public void incrementSumOfTrickPoints(int points) {this.sumOfTrickPoints = sumOfTrickPoints + points;}

    @Override public int getWonPartyPoints() {return wonPartyPoints;}

    @Override public void setWonPartyPoints(int newPoints) {
        this.wonPartyPoints = newPoints;
    }

    @Override public void incrementWonPartyPoints(int points){this.wonPartyPoints = wonPartyPoints + points;}

    @Override public boolean isBot() {return isBot;}

    @Override public void setBot(boolean bot) {isBot = bot;}

    @Override public boolean isGameMaster() {return isGameMaster;}

    @Override public void setGameMaster(boolean gameMaster) {isGameMaster = gameMaster;}

    @Override public boolean isMyTurn() {return isMyTurn;}

    @Override public void setMyTurn(boolean myTurn) {isMyTurn = myTurn;}

    @Override public int getNumberOfWonTricksDuringEntireGame() {return numberOfWonTricksDuringEntireGame;}

    @Override public void setNumberOfWonTricksDuringEntireGame(int numberOfWonTricksDuringEntireGame) {this.numberOfWonTricksDuringEntireGame = numberOfWonTricksDuringEntireGame;}

    @Override public int getTotalWonTrickPointDuringEntireGame() {return totalWonTrickPointDuringEntireGame;}

    @Override public void setTotalWonTrickPointDuringEntireGame(int totalWonTrickPointDuringEntireGame) {this.totalWonTrickPointDuringEntireGame = totalWonTrickPointDuringEntireGame;}



    // Bot Methods, No need to Implement
    @Override
    public Card chooseCardToPress() {
        System.out.println("[ERROR] [" + getUsername() + "] Bot Method called for human player");
        return null;
    }
    @Override
    public Card chooseCardToPlay(Colour leadSuit, ConcurrentHashMap<String, Card> currentTrick, Card TrumpCard, GameType type) {
        System.out.println("[ERROR] [" + getUsername() + "] Bot Method called for human player");
        return null;
    }
}