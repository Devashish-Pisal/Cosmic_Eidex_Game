package com.group06.cosmiceidex.game;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public interface PlayerInterface {

    void addCardToHand(Card card);
    void playCard(Card card);
    void clearHand();
    String getUsername();
    List<Card> getHand();

    void setHand(List<Card> hand);

    int getSumOfTrickPoints();
    void setSumOfTrickPoints(int sumOfTrickPoints);
    void incrementSumOfTrickPoints(int pointsToAdd);
    int getWonPartyPoints();
    void setWonPartyPoints(int wonPartyPoints);
    void incrementWonPartyPoints(int pointsToAdd);
    boolean isBot();
    void setBot(boolean bot);
    boolean isGameMaster();
    void setGameMaster(boolean isGameMaster);
    boolean isMyTurn();
    void setMyTurn(boolean myTurn);


    // Methods for Bot
    Card chooseCardToPress();
    Card chooseCardToPlay(Colour leadSuit, ConcurrentHashMap<String, Card> currentTrick, Card TrumpCard, GameType type);


    int getNumberOfWonTricksDuringEntireGame();
    void setNumberOfWonTricksDuringEntireGame(int numberOfWonTricksDuringEntireGame);
    int getTotalWonTrickPointDuringEntireGame();
    void setTotalWonTrickPointDuringEntireGame(int totalWonTrickPointDuringEntireGame);
}