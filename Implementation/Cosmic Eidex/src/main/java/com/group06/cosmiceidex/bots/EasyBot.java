package com.group06.cosmiceidex.bots;

import com.group06.cosmiceidex.game.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Leichtere Bot
 */
public class EasyBot implements PlayerInterface, Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private List<Card> hand;
    private int trickPoints;
    private int partyPoints;
    private boolean bot;
    private boolean isGameMaster;
    private boolean myTurn;
    private int totalTrickPoints;
    private int totalWonTricks;

    private Random random;

    /**
     * Konstruktor von EasyBot Objekt
     * @param username
     */
    public EasyBot(String username) {
        this.username = username;
        this.hand = new ArrayList<>();
        this.trickPoints = 0;
        this.partyPoints = 0;
        this.bot = true;
        this.isGameMaster = false;
        this.myTurn = false;
        this.totalTrickPoints = 0;
        this.totalWonTricks = 0;
        this.random = new Random();
    }

    /**
     * Drückt zufällige Karte aus der Hand
     * @return Gedrückte Karte
     */
    @Override
    public Card chooseCardToPress() {
        if (!hand.isEmpty()) {
            int index = random.nextInt(hand.size());
            return hand.remove(index);
        }
        return null;
    }

    /**
     * Spielt zufällige Karte aus, wenn in einem Stich erst am Zug. Sonst spielt erste Karte aus der Hand, der gleiche Farbe wie angespielte Karte hat
     * @param leadSuit Farbe des angespielten Karts
     * @param currentTrick Aktuelle Stich
     * @param TrumpCard Trumpf Karte
     * @param type Spiel Modus
     * @return Karte zum Ausspielen
     */
    @Override
    public Card chooseCardToPlay(Colour leadSuit, ConcurrentHashMap<String, Card> currentTrick, Card TrumpCard, GameType type) {
        // currentTrick, TrumpCard and type parameters more important in HardBot class, In this method less important
        if(leadSuit == null && !hand.isEmpty()){
            int index = random.nextInt(hand.size());
            return hand.remove(index);
        }else{ // playing same color card as lead suit
            if(!hand.isEmpty()){
                for(Card c : hand){
                    if(c.getColour() == leadSuit){
                        return c; // return first found card
                    }
                }
                return hand.getFirst(); // when there is no leadSuit card in hand, return first card
            }
        }
        return null;
    }


    /**
     * Fügt Karte im Hand
     * @param card
     */
    @Override
    public void addCardToHand(Card card) {
        hand.add(card);
    }

    /**
     * Entfernt Karte aus der Hand
     * @param card
     */
    @Override
    public void playCard(Card card) {
        hand.remove(card);
    }

    /**
     * Löscht den Hand
     */
    @Override
    public void clearHand() {
        hand.clear();
    }


    // Getter-Setter Methoden
    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public List<Card> getHand() {
        return hand;
    }

    @Override
    public void setHand(List<Card> hand) {
        this.hand = hand;
    }

    @Override
    public int getSumOfTrickPoints() {
        return trickPoints;
    }

    @Override
    public void setSumOfTrickPoints(int sumOfTrickPoints) {
        this.trickPoints = sumOfTrickPoints;
    }

    @Override
    public void incrementSumOfTrickPoints(int pointsToAdd) {
        this.trickPoints += pointsToAdd;
    }

    @Override
    public int getWonPartyPoints() {
        return partyPoints;
    }

    @Override
    public void setWonPartyPoints(int wonPartyPoints) {
        this.partyPoints = wonPartyPoints;
    }

    @Override
    public void incrementWonPartyPoints(int pointsToAdd) {
        this.partyPoints += pointsToAdd;
    }

    @Override
    public boolean isBot() {
        return bot;
    }

    @Override
    public void setBot(boolean bot) {
        this.bot = bot;
    }

    @Override
    public boolean isGameMaster() {
        return isGameMaster;
    }

    @Override
    public void setGameMaster(boolean isGameMaster) {
        this.isGameMaster = isGameMaster;
    }

    @Override
    public boolean isMyTurn() {
        return myTurn;
    }

    @Override
    public void setMyTurn(boolean myTurn) {
        this.myTurn = myTurn;
    }

    @Override
    public int getNumberOfWonTricksDuringEntireGame() {
        return totalWonTricks;
    }

    @Override
    public void setNumberOfWonTricksDuringEntireGame(int numberOfWonTricksDuringEntireGame) {
        this.totalWonTricks = numberOfWonTricksDuringEntireGame;
    }

    @Override
    public int getTotalWonTrickPointDuringEntireGame() {
        return totalTrickPoints;
    }

    @Override
    public void setTotalWonTrickPointDuringEntireGame(int totalWonTrickPointDuringEntireGame) {
        this.totalTrickPoints = totalWonTrickPointDuringEntireGame;
    }
}
