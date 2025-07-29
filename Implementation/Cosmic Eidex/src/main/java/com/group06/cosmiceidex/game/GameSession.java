package com.group06.cosmiceidex.game;

import com.group06.cosmiceidex.bots.EasyBot;

import java.io.Serializable;
import java.time.temporal.ValueRange;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GameSession implements Serializable {
    private ArrayList<String> logData;
    private int partyCount;

    private HashMap<Integer, PlayerInterface> gameResult;
    public ConcurrentHashMap<String, PlayerInterface> playersWithUsernames;
    private List<String> usernames;
    private String usernameOfPlayerOnTurn;
    private int indexOfPlayerOnTurn;

    private GameState gameState = GameState.WAITING_TO_START;
    private Deck deck;
    private boolean gameStarted = false;

    private boolean trumpDecided = false;
    private Colour trumpSuit = null;
    private Card trumpCard;
    private GameType gameType;

    private ConcurrentHashMap<String, Card> pressedCards;
    private ConcurrentHashMap<String, Card> currentTrick;
    private ConcurrentHashMap<String, List<List<Card>>> wonTricksMapping;
    private Colour leadSuit;


    public enum GameState {
        WAITING_TO_START,
        DEALING_CARDS,
        FACE_DOWN_PHASE,
        TRUMP_DECISION,
        PLAYING_CARDS,
        TRICK_COMPLETE,
        PARTY_COMPLETE,
        GAME_OVER
    }


    /**
     * Konstruktor der Klasse GameSession
     * @param player1 Spieler oder Bot
     * @param player2 Spieler oder Bot
     * @param player3 Spieler oder Bot
     * @see PlayerInterface
     */
    public GameSession(PlayerInterface player1, PlayerInterface player2, PlayerInterface player3){
        playersWithUsernames = new ConcurrentHashMap<>();
        currentTrick = new ConcurrentHashMap<>();
        pressedCards = new ConcurrentHashMap<>();
        wonTricksMapping = new ConcurrentHashMap<>();
        logData = new ArrayList<>();
        partyCount = 1;

        this.deck = new Deck();
        this.usernames = new ArrayList<>();
        this.playersWithUsernames.put(player1.getUsername(), player1);
        this.playersWithUsernames.put(player2.getUsername(), player2);
        this.playersWithUsernames.put(player3.getUsername(), player3);
        this.gameStarted = false;
        this.trumpDecided = false;
    }


    /**
     * Karten Verteilen am Anfange dem Spiel und nach Ende der Partie
     * Wechseln der GameState
     * @see PlayerInterface
     */
    public synchronized void dealCards() {
        if(getGameState() == GameState.WAITING_TO_START) {
            if (gameStarted) {
                System.out.println("[ERROR] [GameSession] Game already started");
            }

            // Deck mischen
            deck.shuffle();

            setGameState(GameState.DEALING_CARDS);

            // Karten an alle Spieler verteilen (jeder bekommt 12 Karten)
            int counter = 0;
            for(Map.Entry<String, PlayerInterface> entry : playersWithUsernames.entrySet()){
                for(int i=0; i<12; i++){
                    Card card = deck.dealCard();
                    entry.getValue().addCardToHand(card);

                    if(counter == 2 && i == 11){
                        //Letzte Karte ist Trumpf
                        setTrumpCard(card);
                        setTrumpSuit(trumpCard.getColour());
                        setTrumpDecided(true);
                        decideGameType();
                    }
                }
                if(entry.getValue().isGameMaster()){
                    // GameMaster spielt erst
                    usernameOfPlayerOnTurn = entry.getKey();
                }
                usernames.add(entry.getKey());
                counter++;
            }
            if(getTrumpCard() != null) {
                getLogData().add("[" + partyCount + "] Trumpf bestimmt : " + getTrumpCard().getColour() + '-' + getTrumpCard().getValue() + ".");
            }

            playersWithUsernames.get(usernameOfPlayerOnTurn).setMyTurn(true);
            indexOfPlayerOnTurn = usernames.indexOf(usernameOfPlayerOnTurn);
            gameStarted = true;
            setGameState(GameState.FACE_DOWN_PHASE);
        }else if(getGameState() == GameState.PARTY_COMPLETE){
            resetGameSessionData();
            setGameState(GameState.DEALING_CARDS);
            // Karten an alle Spieler verteilen (jeder bekommt 12 Karten)
            int counter = 0;
            for(Map.Entry<String, PlayerInterface> entry : playersWithUsernames.entrySet()){
                for(int i=0; i<12; i++){
                    Card card = deck.dealCard();
                    entry.getValue().addCardToHand(card);

                    if(counter == 2 && i == 11){
                        //Letzte Karte ist Trumpf
                        setTrumpCard(card);
                        setTrumpSuit(trumpCard.getColour());
                        setTrumpDecided(true);
                        decideGameType();
                        if(getTrumpCard() != null) {
                            System.out.println("TRUMP : " + trumpCard.getColour() + "-" + trumpCard.getValue());
                        }
                    }
                }
                counter++;
            }

            if(getTrumpCard() != null) {
                getLogData().add("[" + partyCount + "] Trumpf bestimmt : " + getTrumpCard().getColour() + '-' + getTrumpCard().getValue() + ".");
            }
            setGameState(GameState.FACE_DOWN_PHASE);
        }
    }

    /**
     * Zurücksetzen alle Daten nach Partie Ende
     * @see PlayerInterface
     * @author Devashish Pisal
     */
    public synchronized void resetGameSessionData(){
        storeDataBeforeResetting();
        getPressedCards().clear();
        getCurrentTrick().clear();
        getWonTricksMapping().clear();
        deck = new Deck();
        deck.shuffle();
        setTrumpDecided(false);
        setTrumpCard(null);
        setTrumpSuit(null);
        setLeadSuit(null);
        PlayerInterface player0 = getPlayersWithUsernames().get(getUsernames().get(0));
        PlayerInterface player1 = getPlayersWithUsernames().get(getUsernames().get(1));
        PlayerInterface player2 = getPlayersWithUsernames().get(getUsernames().get(2));
        player0.setSumOfTrickPoints(0);
        player1.setSumOfTrickPoints(0);
        player2.setSumOfTrickPoints(0);
    }




    /**
     * Entscheiden den nächsten Spieler am Zug
     * @author Devashish Pisal
     */
    public synchronized void decideNextTurnPlayer(){
        if(getGameState() == GameState.FACE_DOWN_PHASE) {
            playersWithUsernames.get(usernameOfPlayerOnTurn).setMyTurn(false);
            indexOfPlayerOnTurn = (indexOfPlayerOnTurn + 1) % 3;
            usernameOfPlayerOnTurn = usernames.get(indexOfPlayerOnTurn);
            playersWithUsernames.get(usernameOfPlayerOnTurn).setMyTurn(true);
        }else if(getGameState() == GameState.PLAYING_CARDS){
            playersWithUsernames.get(usernameOfPlayerOnTurn).setMyTurn(false);
            indexOfPlayerOnTurn = (indexOfPlayerOnTurn + 1) % 3;
            usernameOfPlayerOnTurn = usernames.get(indexOfPlayerOnTurn);
            playersWithUsernames.get(usernameOfPlayerOnTurn).setMyTurn(true);
        }
        decideGameState();
    }


    /**
     * Führt einen Bot Spielzug durch, sofern er am Zug ist
     */
    public synchronized void playBotMove(){
        PlayerInterface player = getPlayersWithUsernames().get(usernameOfPlayerOnTurn);
        if(player.isBot() && player.isMyTurn()){
            PlayerInterface bot = getPlayersWithUsernames().get(usernameOfPlayerOnTurn);
            if(bot != null && usernameOfPlayerOnTurn.equals(bot.getUsername()) && getGameState() == GameState.FACE_DOWN_PHASE){
                Card chosenCard = bot.chooseCardToPress();
                if(chosenCard != null) {
                    getPressedCards().putIfAbsent(bot.getUsername(), chosenCard);
                    int gainedPoints = calculateCardPoints(chosenCard);
                    bot.setSumOfTrickPoints(bot.getSumOfTrickPoints() + gainedPoints);
                    getLogData().add("[" + partyCount + "] Spieler " + bot.getUsername() + " hat eine Karte gedrückt.");
                    decideNextTurnPlayer();
                }
            }else if (bot != null && usernameOfPlayerOnTurn.equals(bot.getUsername()) && getGameState() == GameState.PLAYING_CARDS) {
                Card cardToPlay = bot.chooseCardToPlay(leadSuit, getCurrentTrick(), trumpCard, getGameType());
                if (cardToPlay != null) {
                    if(leadSuit == null){
                        getCurrentTrick().clear();
                        leadSuit = cardToPlay.getColour();
                        getLogData().add("[" + partyCount + "] Die angespielte Farbe ist " + leadSuit + ".");
                    }
                    playersWithUsernames.get(bot.getUsername()).playCard(cardToPlay);
                    getCurrentTrick().put(bot.getUsername(), cardToPlay);
                    getLogData().add("[" + partyCount + "] Spieler " + bot.getUsername() + " hat " + cardToPlay + " gespielt.");
                    decideNextTurnPlayer();
                }
            }
        }
    }



    /**
     * GameState des GameSession-Objekts entscheiden
     */
    public synchronized void decideGameState(){
        if(getGameState() == GameState.FACE_DOWN_PHASE){
            if(pressedCards.size() == 3){
                setGameState(GameState.PLAYING_CARDS);
            }
        }else if(getGameState() == GameState.PLAYING_CARDS){
            if(getCurrentTrick().size() == 3) {
                setGameState(GameState.TRICK_COMPLETE);
                decideTrickWinner();
            }
        }
    }


    /**
     * Karte gedruckt legen
     * @param username Benutzername des Spielers, der Karte gedruckt hat
     * @param cardNumber Karten-Index auf GUI (von 1 bis 12)
     * @see PlayerInterface
     */
    public synchronized void faceDownCard(String username, int cardNumber) {
        if(getGameState() == GameState.FACE_DOWN_PHASE) {
            if (getIndexOfPlayerOnTurn() < 0 || getIndexOfPlayerOnTurn() >= getPlayersWithUsernames().size()) {
                System.out.println("[ERROR] [" + getUsernameOfPlayerOnTurn() + "] Invalid indexOfPlayerOnTurn");
                return;
            }
            if(!getPlayersWithUsernames().containsKey(username)){
                System.out.println("[ERROR] [" + getUsernameOfPlayerOnTurn() + "] Map does not contains username : " + username);
            }
            PlayerInterface player = getPlayersWithUsernames().get(username);

            List<Card> list = player.getHand();
            Card card = list.remove(cardNumber-1);
            player.setHand(list);
            pressedCards.putIfAbsent(username, card);
            int gainedPoint = calculateCardPoints(card);
            player.setSumOfTrickPoints(player.getSumOfTrickPoints() + gainedPoint);

            getLogData().add("[" + partyCount + "] Spieler " + username + " hat eine Karte gedrückt.");
            decideNextTurnPlayer();
        }else{
            System.out.println("[ERROR] [" + getUsernameOfPlayerOnTurn() + "] Wrong GameState expected GameState : FACE_DOWN_PHASE but actual GameState : " + getGameState());
        }
    }


    /**
     * Karte ausspielen
     * @param username Benutzername des Spielers, der Karte ausgespielt hat
     * @param cardNumber Karten-Index auf GUI (von 1 bis 12)
     * @see PlayerInterface
     */
    public synchronized void playCard(String username, int cardNumber) {
        if(getGameState() == GameState.PLAYING_CARDS) {
            PlayerInterface player = getPlayersWithUsernames().get(username);
            List<Card> hand = player.getHand();
            Card card = hand.get(cardNumber-1);

            if(leadSuit == null){
                getCurrentTrick().clear();
                leadSuit = card.getColour();
                getLogData().add("[" + partyCount + "] Die angespielte Farbe ist " + leadSuit + ".");
                hand.remove(cardNumber-1);
                player.setHand(hand);
                getCurrentTrick().put(username, card);
                getLogData().add("[" + partyCount + "] Spieler " + username + " hat " + card.getColour() + "-" + card.getValue() + " gespielt.");
                decideNextTurnPlayer();
            }else{
                if(card.getColour() == leadSuit){ // players hand contains lead suit card
                    hand.remove(cardNumber-1);
                    player.setHand(hand);
                    getCurrentTrick().put(username, card);
                    getLogData().add("[" + partyCount + "] Spieler " + username + " hat " + card.getColour() + "-" + card.getValue() + " gespielt.");
                    decideNextTurnPlayer();
                }else if(trumpSuit != null && card.getColour() == trumpSuit){ // player plays trump card
                    if(checkForUnderTrumping(card, player)){
                        System.out.println("[WARN] [" + getUsernameOfPlayerOnTurn() + "] Player tried to Under Trump");
                    }else {
                        hand.remove(cardNumber - 1);
                        player.setHand(hand);
                        getCurrentTrick().put(username, card);
                        getLogData().add("[" + partyCount + "] Spieler " + username + " hat " + card.getColour() + "-" + card.getValue() + " gespielt.");
                        decideNextTurnPlayer();
                    }
                }else if(getTrumpSuit() != null && getLeadSuit() == getTrumpSuit() && card.getColour() != trumpSuit  && handContainsOnlyOneTrumpCard(username)){
                    // Spezielle Bube Regel
                    hand.remove(cardNumber-1);
                    player.setHand(hand);
                    getCurrentTrick().put(username, card);
                    getLogData().add("[" + partyCount + "] Spieler " + username + " hat " + card.getColour() + "-" + card.getValue() + " gespielt.");
                    decideNextTurnPlayer();
                }else if(card.getColour() != leadSuit && !playerHandContainsSpecificColor(username, leadSuit)){ // players hand does not contain card with lead suit
                    hand.remove(cardNumber-1);
                    player.setHand(hand);
                    getCurrentTrick().put(username, card);
                    getLogData().add("[" + partyCount + "] Spieler " + username + " hat " + card.getColour() + "-" + card.getValue() + " gespielt.");
                    decideNextTurnPlayer();
                }else{
                    System.out.println("[ERROR] [" + getUsernameOfPlayerOnTurn() + "] Invalid Move");
                }
            }
        }else{
            System.out.println("[ERROR] [" + getUsernameOfPlayerOnTurn() + "] Wrong GameState expected GameState : PLAYING_CARDS but actual GameState : " + getGameState());
        }
    }


    /**
     * Prüfen nach Untertrumpfen
     * @param playerCard Ausgespielte Karte
     * @param player Spieler am Zug (Dritte Spieler)
     * @return true, falls der Spieler versucht zu untertrumpfen, sonst false
     * @see PlayerInterface
     * @author Devashish Pisal
     */
    public synchronized boolean checkForUnderTrumping(Card playerCard, PlayerInterface player){
        ArrayList<Card> playedCards = new ArrayList<>(getCurrentTrick().values());
        if(playedCards.size() < 2 || leadSuit == getTrumpSuit()){
            return false; // No under trumping
        }else if(leadSuit != getTrumpSuit() && playedCards.size() == 2){
            Card trumpCardInTrick = null; // card played by middle (2nd Player) player
            for(int i = 0; i<playedCards.size(); i++){
                if(playedCards.get(i).getColour() == getTrumpSuit()){
                    trumpCardInTrick = playedCards.get(i);
                    break;
                }
            }
            if(trumpCardInTrick == null){
                return false;
            }else {
                int playerCardRank = calculateCardRank(playerCard);
                int trumpCardInTrickRank = calculateCardRank(trumpCardInTrick);
                if((playerCardRank < trumpCardInTrickRank) && (player.getHand().size() > 1) && handContainsNonTrumpCard(getTrumpSuit(), player.getHand())){
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Prüfen, ob hand des Spielers Trumpfkarte enthält
     * @param trumpSuit Trumpffarbe derzeitige Partie
     * @param hand Spieler Hand
     * @return true, falls Hand enthält Trumpffarbe Karte
     * @see Card
     * @author Devashish Pisal
     */
    public synchronized boolean handContainsNonTrumpCard(Colour trumpSuit, List<Card> hand){
        if(trumpSuit != null){
            for (Card card : hand) {
                if (card.getColour() != trumpSuit) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Feststellen der Gewinner des Stiches
     * @see PlayerInterface
     */
    public synchronized void decideTrickWinner() {
        if(getGameState() == GameState.TRICK_COMPLETE) {
            if (getCurrentTrick().size() != 3) {
                System.out.println("[ERROR] [" + getUsernameOfPlayerOnTurn() + "] Trick is not complete yet");
            }else {
                String user0 = getUsernames().get(0);
                String user1 = getUsernames().get(1);
                String user2 = getUsernames().get(2);


                int user0Rank = calculateCardRank(getCurrentTrick().get(user0));
                int user1Rank = calculateCardRank(getCurrentTrick().get(user1));
                int user2Rank = calculateCardRank(getCurrentTrick().get(user2));

                int user0Points = calculateCardPoints(getCurrentTrick().get(user0));
                int user1Points = calculateCardPoints(getCurrentTrick().get(user1));
                int user2Points = calculateCardPoints(getCurrentTrick().get(user2));

                PlayerInterface player = null;
                if(user0Rank > user1Rank && user0Rank > user2Rank){
                    player = getPlayersWithUsernames().get(user0);
                }else if(user1Rank > user0Rank && user1Rank > user2Rank){
                    player = getPlayersWithUsernames().get(user1);
                }else if(user2Rank > user0Rank && user2Rank > user1Rank){
                    player = getPlayersWithUsernames().get(user2);
                }else if(user0Rank == user1Rank && user0Rank > user2Rank && user1Rank > user2Rank){
                    if(trumpSuit != null && getCurrentTrick().get(user0).getColour() == trumpSuit && getCurrentTrick().get(user1).getColour() != trumpSuit){
                        player = getPlayersWithUsernames().get(user0);
                    }else if(trumpSuit != null && getCurrentTrick().get(user1).getColour() == trumpSuit && getCurrentTrick().get(user0).getColour() != trumpSuit){
                        player = getPlayersWithUsernames().get(user1);
                    }else if(leadSuit != null && getCurrentTrick().get(user0).getColour() == leadSuit){
                        player = getPlayersWithUsernames().get(user0);
                    }else{
                        player = getPlayersWithUsernames().get(user1);
                    }
                }else if(user1Rank == user2Rank && user1Rank > user0Rank && user2Rank > user0Rank){
                    if(trumpSuit != null && getCurrentTrick().get(user1).getColour() == trumpSuit && getCurrentTrick().get(user2).getColour() != trumpSuit){
                        player = getPlayersWithUsernames().get(user1);
                    }else if(trumpSuit != null && getCurrentTrick().get(user2).getColour() == trumpSuit && getCurrentTrick().get(user1).getColour() != trumpSuit){
                        player = getPlayersWithUsernames().get(user2);
                    }else if(leadSuit != null && getCurrentTrick().get(user1).getColour() == leadSuit){
                        player = getPlayersWithUsernames().get(user1);
                    }else{
                        player = getPlayersWithUsernames().get(user2);
                    }
                }else if(user0Rank == user2Rank && user0Rank > user1Rank && user2Rank > user1Rank){
                    if(trumpSuit != null && getCurrentTrick().get(user0).getColour() == trumpSuit && getCurrentTrick().get(user2).getColour() != trumpSuit){
                        player = getPlayersWithUsernames().get(user0);
                    }else if(trumpSuit != null && getCurrentTrick().get(user2).getColour() == trumpSuit && getCurrentTrick().get(user0).getColour() != trumpSuit){
                        player = getPlayersWithUsernames().get(user2);
                    }else if(leadSuit != null && getCurrentTrick().get(user0).getColour() == leadSuit){
                        player = getPlayersWithUsernames().get(user0);
                    }else{
                        player = getPlayersWithUsernames().get(user2);
                    }
                }else if(user0Points == user1Points && user1Points == user2Points){
                    Card player0Card = getCurrentTrick().get(user0);
                    Card player1Card = getCurrentTrick().get(user1);
                    Card player2Card = getCurrentTrick().get(user2);
                    if(trumpSuit != null && player0Card.getColour() == trumpSuit && player1Card.getColour() != trumpSuit && player2Card.getColour() != trumpSuit){
                        player = getPlayersWithUsernames().get(user0);
                    }else if(trumpSuit != null && player1Card.getColour() == trumpSuit && player0Card.getColour() != trumpSuit && player2Card.getColour() != trumpSuit){
                        player = getPlayersWithUsernames().get(user1);
                    }else if(trumpSuit != null && player2Card.getColour() == trumpSuit && player0Card.getColour() != trumpSuit && player1Card.getColour() != trumpSuit){
                        player = getPlayersWithUsernames().get(user2);
                    }else if(leadSuit != null && player0Card.getColour() == leadSuit && player1Card.getColour() != leadSuit && player2Card.getColour() != leadSuit){
                        player = getPlayersWithUsernames().get(user0);
                    }else if(leadSuit != null && player1Card.getColour() == leadSuit && player0Card.getColour() != leadSuit && player2Card.getColour() != leadSuit){
                        player = getPlayersWithUsernames().get(user1);
                    }else{
                        player = getPlayersWithUsernames().get(user2);
                    }
                }



                PlayerInterface p0 = getPlayersWithUsernames().get(getUsernames().get(0));
                PlayerInterface p1 = getPlayersWithUsernames().get(getUsernames().get(1));
                PlayerInterface p2 = getPlayersWithUsernames().get(getUsernames().get(2));

                boolean isLastTrickOfParty = p0.getHand().isEmpty() && p1.getHand().isEmpty() && p2.getHand().isEmpty();
                if(isLastTrickOfParty){
                    player.setSumOfTrickPoints(player.getSumOfTrickPoints() + user0Points + user1Points + user2Points + 5);
                    getLogData().add("[" + partyCount + "] Spieler " + player.getUsername() + " hat den letzten Stich der Partie " + partyCount + " gewonnen.");
                    getLogData().add("[" + partyCount + "] Das Ergebnis der Partie ist : " + p0.getUsername() + " = " + p0.getSumOfTrickPoints() +
                            ", " + p1.getUsername() + " = " + p1.getSumOfTrickPoints() + ", " + p2.getUsername() + " = " + p2.getSumOfTrickPoints() + ".");
                }else {
                    player.setSumOfTrickPoints(player.getSumOfTrickPoints() + user0Points + user1Points + user2Points);
                    getLogData().add("[" + partyCount + "] Spieler " + player.getUsername() + " hat den Stich gewonnen.");
                }

                List<Card> currentTrick = new ArrayList<>(getCurrentTrick().values());
                if(getWonTricksMapping().containsKey(player.getUsername())){
                    List<List<Card>> wonTricks = getWonTricksMapping().get(player.getUsername());
                    wonTricks.add(currentTrick);
                    getWonTricksMapping().replace(player.getUsername(), wonTricks);
                }else{
                    List<List<Card>> list = new ArrayList<>();
                    list.add(currentTrick);
                    getWonTricksMapping().put(player.getUsername(),list);
                }

                // last trick winner starts next trick
                getPlayersWithUsernames().get(usernameOfPlayerOnTurn).setMyTurn(false);
                player.setMyTurn(true);
                usernameOfPlayerOnTurn = player.getUsername();
                setIndexOfPlayerOnTurn(getUsernames().indexOf(usernameOfPlayerOnTurn));
                leadSuit = null;

                PlayerInterface player0 = getPlayersWithUsernames().get(getUsernames().get(0));
                PlayerInterface player1 = getPlayersWithUsernames().get(getUsernames().get(1));
                PlayerInterface player2 = getPlayersWithUsernames().get(getUsernames().get(2));

                if(player0.getHand().isEmpty() && player1.getHand().isEmpty() && player2.getHand().isEmpty()){
                    setGameState(GameState.PARTY_COMPLETE);
                    getLogData().add("[" + partyCount + "] Partie beendet.");
                    setPartyCount(getPartyCount() + 1);
                    updateWonPartyPoints();

                }else {
                    setGameState(GameState.PLAYING_CARDS);
                }
            }
        }else{
            System.out.println("[ERROR] [" + getUsernameOfPlayerOnTurn() + "] Wrong GameState expected GameState : TRICK_COMPLETE but actual GameState : " + getGameState());
        }
    }


    /**
     * Gewinner der Partie entscheiden
     * @see PlayerInterface
     * @author Devashish Pisal
     */
    public synchronized void updateWonPartyPoints() {
        if(getWonTricksMapping().size() == 1){
            // Wenn ein Spieler alle Stiche gewinnt
            PlayerInterface player = null;
            for(Map.Entry<String, List<List<Card>>> entry : getWonTricksMapping().entrySet()){
                player = getPlayersWithUsernames().get(entry.getKey());
            }
            if(player != null) {
                player.setWonPartyPoints(player.getWonPartyPoints() + 2);
                decideGameWinner();
                return;
            }
        }else{
            PlayerInterface player0 = getPlayersWithUsernames().get(getUsernames().get(0));
            PlayerInterface player1 = getPlayersWithUsernames().get(getUsernames().get(1));
            PlayerInterface player2 = getPlayersWithUsernames().get(getUsernames().get(2));

            //Haben aber alle drei Spieler je 6 Gewinnpunkt und schafft keiner einen Match (Schwarz), so
            //steigt derjenige auf 7 und gewinnt, der die Runde verliert (Mitte oder mehr als 100 Punkte)
            if(player0.getWonPartyPoints() == 6 && player1.getWonPartyPoints() == 6 && player2.getWonPartyPoints() == 6){
                // Wenn Spieler mehr als 100 Punkten gekreigt hat
                if(player0.getSumOfTrickPoints() >= 100){
                    player0.setWonPartyPoints(player0.getWonPartyPoints() + 1);
                    decideGameWinner();
                    return;
                }else if(player1.getSumOfTrickPoints() >= 100){
                    player1.setWonPartyPoints(player1.getWonPartyPoints() + 1);
                    decideGameWinner();
                    return;
                }else if(player2.getSumOfTrickPoints() >= 100){
                    player2.setWonPartyPoints(player2.getWonPartyPoints() + 1);
                    decideGameWinner();
                    return;
                }else if(player0.getSumOfTrickPoints() < 100 && player1.getSumOfTrickPoints() < 100 && player2.getSumOfTrickPoints() < 100){ // Wenn alle Spieler unter 100 sind
                    if(player0.getSumOfTrickPoints() < player1.getSumOfTrickPoints() && player0.getSumOfTrickPoints() < player2.getSumOfTrickPoints()){
                        if(player1.getSumOfTrickPoints() <= player2.getSumOfTrickPoints()){
                            player1.setWonPartyPoints(player1.getWonPartyPoints() + 1);
                            decideGameWinner();
                            return;
                        }else if(player2.getSumOfTrickPoints() < player1.getSumOfTrickPoints()){
                            player2.setWonPartyPoints(player2.getWonPartyPoints() + 1);
                            decideGameWinner();
                            return;
                        }
                    }else if(player1.getSumOfTrickPoints() <= player0.getSumOfTrickPoints() && player1.getSumOfTrickPoints() <= player2.getSumOfTrickPoints()){
                        if(player0.getSumOfTrickPoints() <= player2.getSumOfTrickPoints()){
                            player0.setWonPartyPoints(player0.getWonPartyPoints() + 1);
                            decideGameWinner();
                            return;
                        }else if(player2.getSumOfTrickPoints() < player0.getSumOfTrickPoints()){
                            player2.setWonPartyPoints(player2.getWonPartyPoints() + 1);
                            decideGameWinner();
                            return;
                        }
                    }else if(player2.getSumOfTrickPoints() <= player0.getSumOfTrickPoints() && player2.getSumOfTrickPoints() <= player1.getSumOfTrickPoints()){
                        if(player0.getSumOfTrickPoints() <= player1.getSumOfTrickPoints()){
                            player0.setWonPartyPoints(player0.getWonPartyPoints() + 1);
                            decideGameWinner();
                            return;
                        }else if(player1.getSumOfTrickPoints() < player0.getSumOfTrickPoints()){
                            player1.setWonPartyPoints(player0.getWonPartyPoints() + 1);
                            decideGameWinner();
                            return;
                        }
                    }
                }
            }


            // Wenn ein Spieler mehr als 100 Punkte gewonnen hat
            if(player0.getSumOfTrickPoints() >= 100){
                if(player1.getWonPartyPoints() == 6 && player2.getWonPartyPoints() == 6){
                    int player1TrickPoints = player1.getSumOfTrickPoints();
                    int player2TrickPoints = player2.getSumOfTrickPoints();
                    if(player1TrickPoints < player2TrickPoints){
                        player1.setWonPartyPoints(player1.getWonPartyPoints() - 1);
                        return;
                    }else if(player2TrickPoints < player1TrickPoints){
                        player2.setWonPartyPoints(player2.getWonPartyPoints() - 1);
                        return;
                    }else{
                        return; // beide Spieler bleiben auf 6
                    }
                }else {
                    player1.setWonPartyPoints(player1.getWonPartyPoints() + 1);
                    player2.setWonPartyPoints(player2.getWonPartyPoints() + 1);
                    decideGameWinner();
                    return;
                }
            }else if(player1.getSumOfTrickPoints() >= 100){
                if(player0.getWonPartyPoints() == 6 && player2.getWonPartyPoints() == 6){
                    int player0TrickPoints = player0.getSumOfTrickPoints();
                    int player2TrickPoints = player2.getSumOfTrickPoints();
                    if(player0TrickPoints < player2TrickPoints){
                        player0.setWonPartyPoints(player0.getWonPartyPoints() - 1);
                        return;
                    }else if(player2TrickPoints < player0TrickPoints){
                        player2.setWonPartyPoints(player2.getWonPartyPoints() - 1);
                        return;
                    }else{
                        return; // beide Spieler bleiben auf 6
                    }
                }else {
                    player0.setWonPartyPoints(player0.getWonPartyPoints() + 1);
                    player2.setWonPartyPoints(player2.getWonPartyPoints() + 1);
                    decideGameWinner();
                    return;
                }
            }else if(player2.getSumOfTrickPoints() >= 100){
                if(player0.getWonPartyPoints() == 6 && player1.getWonPartyPoints() == 6){
                    int player0TrickPoints = player0.getSumOfTrickPoints();
                    int player1TrickPoints = player1.getSumOfTrickPoints();
                    if(player0TrickPoints < player1TrickPoints){
                        player0.setWonPartyPoints(player0.getWonPartyPoints() - 1);
                        return;
                    }else if(player1TrickPoints < player0TrickPoints){
                        player1.setWonPartyPoints(player1.getWonPartyPoints() - 1);
                        return;
                    }else{
                        return; // beide Spieler bleiben auf 6
                    }
                }else {
                    player0.setWonPartyPoints(player0.getWonPartyPoints() + 1);
                    player1.setWonPartyPoints(player1.getWonPartyPoints() + 1);
                    decideGameWinner();
                    return;
                }
            }



            int player0Points = player0.getSumOfTrickPoints();
            int player1Points = player1.getSumOfTrickPoints();
            int player2Points = player2.getSumOfTrickPoints();

            // Wenn alle Spieler unter 100 Punkte sind und zwei spieler gleich viel punkte haben
            if(player0Points < 100 && player1Points < 100 && player2Points  < 100){
                if(player0Points == player1Points){
                    player2.setWonPartyPoints(player2.getWonPartyPoints() + 2);
                    decideGameWinner();
                    return;
                }else if(player1Points == player2Points){
                    player0.setWonPartyPoints(player0.getWonPartyPoints() + 2);
                    decideGameWinner();
                    return;
                }else if(player2Points == player0Points){
                    player1.setWonPartyPoints(player1.getWonPartyPoints() + 2);
                    decideGameWinner();
                    return;
                }

                // Wenn alle drei Spieler weniger als 100 Punkte haben, dann bekommt der Spieler mit
                // den meisten und der mit den wenigsten Punkten je 1 Gewinnpunkt.
                if(player0Points < player1Points && player0Points < player2Points){
                    if(player1Points < player2Points){
                        if(player0.getWonPartyPoints() == 6 && player2.getWonPartyPoints() == 6){
                            player0.setWonPartyPoints(player0.getWonPartyPoints() - 1);
                            decideGameWinner();
                            return;
                        } else {
                            player0.setWonPartyPoints(player0.getWonPartyPoints() + 1);
                            player2.setWonPartyPoints(player2.getWonPartyPoints() + 1);
                            decideGameWinner();
                            return;
                        }
                    }else if(player2Points < player1Points){
                        if(player0.getWonPartyPoints() == 6 && player1.getWonPartyPoints() == 6){
                            player0.setWonPartyPoints(player0.getWonPartyPoints() - 1);
                            decideGameWinner();
                            return;
                        }else {
                            player0.setWonPartyPoints(player0.getWonPartyPoints() + 1);
                            player1.setWonPartyPoints(player1.getWonPartyPoints() + 1);
                            decideGameWinner();
                            return;
                        }
                    }
                }else if(player1Points < player0Points && player1Points < player2Points){
                    if(player0Points < player2Points){
                        if(player1.getWonPartyPoints() == 6 && player2.getWonPartyPoints() == 6) {
                            player1.setWonPartyPoints(player1.getWonPartyPoints() - 1);
                            decideGameWinner();
                            return;
                        }else{
                            player1.setWonPartyPoints(player1.getWonPartyPoints() + 1);
                            player2.setWonPartyPoints(player2.getWonPartyPoints() + 1);
                            decideGameWinner();
                            return;
                        }
                    }else if(player2Points < player0Points){
                        if(player1.getWonPartyPoints() == 6 && player0.getWonPartyPoints() == 6){
                            player1.setWonPartyPoints(player1.getWonPartyPoints() - 1);
                            decideGameWinner();
                            return;
                        }else {
                            player1.setWonPartyPoints(player1.getWonPartyPoints() + 1);
                            player0.setWonPartyPoints(player0.getWonPartyPoints() + 1);
                            decideGameWinner();
                            return;
                        }
                    }
                }else if(player2Points < player0Points && player2Points < player1Points){
                    if(player0Points < player1Points){
                        if(player2.getWonPartyPoints() == 6 && player1.getWonPartyPoints() == 6){
                            player2.setWonPartyPoints(player2.getWonPartyPoints() - 1);
                            decideGameWinner();
                            return;
                        }else {
                            player2.setWonPartyPoints(player2.getWonPartyPoints() + 1);
                            player1.setWonPartyPoints(player1.getWonPartyPoints() + 1);
                            decideGameWinner();
                            return;
                        }
                    }else if(player1Points < player0Points){
                        if(player2.getWonPartyPoints() == 6 && player0.getWonPartyPoints() == 6) {
                            player2.setWonPartyPoints(player2.getWonPartyPoints() - 1);
                            decideGameWinner();
                            return;
                        }else{
                            player2.setWonPartyPoints(player2.getWonPartyPoints() + 1);
                            player0.setWonPartyPoints(player0.getWonPartyPoints() + 1);
                            decideGameWinner();
                            return;
                        }
                    }
                }
            }
        }
    }


    /**
     * Gewinner das gesamte Spiel entscheiden
     * @see PlayerInterface
     */
    public synchronized void decideGameWinner(){
        PlayerInterface player1 = getPlayersWithUsernames().get(getUsernames().get(0));
        PlayerInterface player2 = getPlayersWithUsernames().get(getUsernames().get(1));
        PlayerInterface player3 = getPlayersWithUsernames().get(getUsernames().get(2));

        HashMap<Integer, PlayerInterface> result = new HashMap<>();

        int player1WonPoints = player1.getWonPartyPoints();
        int player2WonPoints = player2.getWonPartyPoints();
        int player3WonPoints = player3.getWonPartyPoints();

        if(player1WonPoints >= 7 && player2WonPoints < 7 && player3WonPoints <7){
            result.put(1, player1);
            if(player3WonPoints < player2WonPoints){
                result.put(2, player2);
                result.put(3, player3);
                setGameResult(result);
                setGameState(GameState.GAME_OVER);
                getLogData().add("[" + partyCount + "] Spiel beendet.");
            }else if(player2WonPoints < player3WonPoints){
                result.put(2, player3);
                result.put(3, player2);
                setGameResult(result);
                setGameState(GameState.GAME_OVER);
                getLogData().add("[" + partyCount + "] Spiel beendet.");
            }else if(player2WonPoints == player3WonPoints){
                if(player3.getSumOfTrickPoints() <= player2.getSumOfTrickPoints()){ // Deciding winner on the basis of trick points
                    result.put(2, player2);
                    result.put(3, player3);
                    setGameResult(result);
                    setGameState(GameState.GAME_OVER);
                    getLogData().add("[" + partyCount + "] Spiel beendet.");
                }else if(player2.getSumOfTrickPoints() < player3.getSumOfTrickPoints()){
                    result.put(2, player3);
                    result.put(3, player2);
                    setGameResult(result);
                    setGameState(GameState.GAME_OVER);
                    getLogData().add("[" + partyCount + "] Spiel beendet.");
                }
            }
        }else if(player2WonPoints >= 7 && player1WonPoints < 7 && player3WonPoints < 7){
            result.put(1, player2);
            if(player1WonPoints < player3WonPoints){
                result.put(2, player3);
                result.put(3, player1);
                setGameResult(result);
                setGameState(GameState.GAME_OVER);
                getLogData().add("[" + partyCount + "] Spiel beendet.");
            }else if(player3WonPoints < player1WonPoints){
                result.put(2, player1);
                result.put(3, player3);
                setGameResult(result);
                setGameState(GameState.GAME_OVER);
                getLogData().add("[" + partyCount + "] Spiel beendet.");
            }else if(player1WonPoints == player3WonPoints){
                if(player3.getSumOfTrickPoints() <= player1.getSumOfTrickPoints()){ // Deciding winner on the basis of trick points
                    result.put(2, player1);
                    result.put(3, player3);
                    setGameResult(result);
                    setGameState(GameState.GAME_OVER);
                    getLogData().add("[" + partyCount + "] Spiel beendet.");
                }else if(player1.getSumOfTrickPoints() < player3.getSumOfTrickPoints()){
                    result.put(2, player3);
                    result.put(3, player1);
                    setGameResult(result);
                    setGameState(GameState.GAME_OVER);
                    getLogData().add("[" + partyCount + "] Spiel beendet.");
                }
            }
        }else if(player3WonPoints >= 7 && player1WonPoints <  7 && player2WonPoints < 7){
            result.put(1, player3);
            if(player1WonPoints < player2WonPoints){
                result.put(2, player2);
                result.put(3, player1);
                setGameResult(result);
                setGameState(GameState.GAME_OVER);
                getLogData().add("[" + partyCount + "] Spiel beendet.");
            }else if(player2WonPoints < player1WonPoints){
                result.put(2, player1);
                result.put(3, player2);
                setGameResult(result);
                setGameState(GameState.GAME_OVER);
                getLogData().add("[" + partyCount + "] Spiel beendet.");
            }else if(player1WonPoints == player2WonPoints){
                if(player2.getSumOfTrickPoints() <= player1.getSumOfTrickPoints()){ // Deciding winner on the basis of trick points
                    result.put(2, player1);
                    result.put(3, player2);
                    setGameResult(result);
                    setGameState(GameState.GAME_OVER);
                    getLogData().add("[" + partyCount + "] Spiel beendet.");
                }else if(player1.getSumOfTrickPoints() < player2.getSumOfTrickPoints()){
                    result.put(2, player2);
                    result.put(3, player1);
                    setGameResult(result);
                    setGameState(GameState.GAME_OVER);
                    getLogData().add("[" + partyCount + "] Spiel beendet.");
                }
            }
        }else{
            dealCards(); // Spiel ist nicht fertig, aber Partei ist fertig
        }
    }


    /**
     * Prüfen, ob Hand des Spielers eine spezifische Farbkarte enthält
     * @param username Benutzername der Spieler
     * @param colour Kartenfarbe, der gesucht werden soll
     * @return true, falls Hand des Spielers spezifische Farbkarte enthält, sonst false
     * @see PlayerInterface
     * @see Card
     * @author Devashish Pisal
     */
    public synchronized boolean playerHandContainsSpecificColor(String username, Colour colour){
        PlayerInterface player = getPlayersWithUsernames().get(username);
        List<Card> hand = player.getHand();
        for(Card card : hand){
            if(card.getColour() == colour){
                return true;
            }
        }
        return false;
    }


    /**
     * Prüfen, ob Hand des Spielers nur eine Trumpfkarte enthält
     * @param username Benutzername des Spielers
     * @return true, falls Hand des Spielers nur ein Karte enthält
     * @see PlayerInterface
     * @author Devashish Pisal
     */
    public synchronized boolean handContainsOnlyOneTrumpCard(String username){
        PlayerInterface player = getPlayersWithUsernames().get(username);
        List<Card> hand = player.getHand();
        for(Card card : hand){
            if(getTrumpCard() != null && card.getColour() == getTrumpSuit() && card.getValue() != Value.BUBE){
                return false;
            }
        }
        return true;
    }

    /**
     * Gibt Rang der Karte zurück um den Gewinner des Stichs zu ermitteln.
     * @param card
     * @return 
     * @author O.T.
     */
    private synchronized int calculateCardRank(Card card){
        if(this.getGameType() == GameType.Normal){
            if(card.getColour() == this.getTrumpSuit()){
                switch(card.getValue()){
                    case BUBE: return 26;
                    case NEUN: return  25;
                    case ASS: return 24;
                    case KOENIG: return 23;
                    case DAME: return 22;
                    case ZEHN: return 21;
                    case ACHT: return 20;
                    case SIEBEN: return 19;
                    case SECHS: return 18;
                }
            } else if(card.getColour() == this.getLeadSuit()){
                switch(card.getValue()){
                    case ASS: return 17;
                    case KOENIG: return 16;
                    case DAME: return 15;
                    case BUBE: return 14;
                    case ZEHN: return 13;
                    case NEUN: return 12;
                    case ACHT: return 11;
                    case SIEBEN: return 10;
                    case SECHS: return 9;
                }
            } else {
                switch (card.getValue()) {
                    case ASS: return 8;
                    case KOENIG: return 7;
                    case DAME: return 6;
                    case BUBE: return 5;
                    case ZEHN: return 4;
                    case NEUN: return 3;
                    case ACHT: return 2;
                    case SIEBEN: return 1;
                    case SECHS: return 0;
                }
            }
        }else if(this.getGameType() == GameType.Obenabe){
            switch (card.getValue()) {
                case ASS: return 8;
                case KOENIG: return 7;
                case DAME: return 6;
                case BUBE: return 5;
                case ZEHN: return 4;
                case NEUN: return 3;
                case ACHT: return 2;
                case SIEBEN: return 1;
                case SECHS: return 0;
            }
        }else if(this.getGameType() == GameType.Undenuf){
            switch (card.getValue()) {
                case ASS: return 0;
                case KOENIG: return 1;
                case DAME: return 2;
                case BUBE: return 3;
                case ZEHN: return 4;
                case NEUN: return 5;
                case ACHT: return 6;
                case SIEBEN: return 7;
                case SECHS: return 8;
            }
        }
        return -1;
    }

    /** Je nach der Spielmodus Kartenpunkte berechnen
     * @param card Karte
     * @return int Punkten
     * @author Devashish Pisal
     */
    private synchronized int calculateCardPoints(Card card) {
        if(getGameType() == GameType.Obenabe) {
            switch (card.getValue()) {
                case ASS: return 11;
                case KOENIG: return 4;
                case DAME: return 3;
                case BUBE: return 2;
                case ZEHN: return 10;
                case NEUN: return  0;
                case ACHT: return 8;
                case SIEBEN: return 0;
                case SECHS: return 0;
            }
        }else if(getGameType() == GameType.Undenuf){
            switch (card.getValue()) {
                case ASS: return 0;
                case KOENIG: return 4;
                case DAME: return 3;
                case BUBE: return 2;
                case ZEHN: return 10;
                case NEUN: return  0;
                case ACHT: return 8;
                case SIEBEN: return 0;
                case SECHS: return 11;
            }
        }else if(getGameType() == GameType.Normal){
            switch (card.getValue()) {
                case BUBE: return (card.getColour() == getTrumpSuit()) ? 20 : 2;
                case NEUN: return (card.getColour() == getTrumpSuit()) ? 14 : 0;
                case ASS: return 11;
                case KOENIG: return 4;
                case DAME: return 3;
                case ZEHN: return 10;
                case ACHT: return 0;
                case SIEBEN: return 0;
                case SECHS: return 0;
            }
        }
        return -1; // default
    }


    /**
     * Gewonnene Stichen und Punkten im Player-Objekten speichern
     * @see PlayerInterface
     * @author Devashish Pisal
     */
    public synchronized void storeDataBeforeResetting(){
        PlayerInterface player0 = getPlayersWithUsernames().get(getUsernames().get(0));
        PlayerInterface player1 = getPlayersWithUsernames().get(getUsernames().get(1));
        PlayerInterface player2 = getPlayersWithUsernames().get(getUsernames().get(2));

        player0.setTotalWonTrickPointDuringEntireGame(player0.getTotalWonTrickPointDuringEntireGame() + player0.getSumOfTrickPoints());
        player1.setTotalWonTrickPointDuringEntireGame(player1.getTotalWonTrickPointDuringEntireGame() + player1.getSumOfTrickPoints());
        player2.setTotalWonTrickPointDuringEntireGame(player2.getTotalWonTrickPointDuringEntireGame() + player2.getSumOfTrickPoints());

        if(getWonTricksMapping().containsKey(player0.getUsername())) {
            player0.setNumberOfWonTricksDuringEntireGame(player0.getNumberOfWonTricksDuringEntireGame() + getWonTricksMapping().get(player0.getUsername()).size());
        }
        if(getWonTricksMapping().containsKey(player1.getUsername())){
            player1.setNumberOfWonTricksDuringEntireGame(player1.getNumberOfWonTricksDuringEntireGame() + getWonTricksMapping().get(player1.getUsername()).size());
        }
        if(getWonTricksMapping().containsKey(player2.getUsername())){
            player2.setNumberOfWonTricksDuringEntireGame(player2.getNumberOfWonTricksDuringEntireGame() + getWonTricksMapping().get(player2.getUsername()).size());
        }
    }


    /**
     * Je nach der Trumpfkarte Spielmodus feststellen
     */
    public synchronized void decideGameType(){
        if(trumpCard.getValue() == Value.ASS){
            setGameType(GameType.Obenabe);
            setTrumpSuit(null);
            setTrumpCard(null);
            setTrumpDecided(false);
        }else if(trumpCard.getValue() == Value.SECHS){
            setGameType(GameType.Undenuf);
            setTrumpSuit(null);
            setTrumpCard(null);
            setTrumpDecided(false);
        }else{
            setGameType(GameType.Normal);
        }
    }



    // Getter-Setter Methoden

    public synchronized boolean isTrumpDecided() {return trumpDecided;}

    public synchronized void setTrumpDecided(boolean trumpDecided) {this.trumpDecided = trumpDecided;}

    public synchronized Colour getTrumpSuit() {return trumpSuit;}

    public synchronized void setTrumpSuit(Colour trumpSuit) {this.trumpSuit = trumpSuit;}

    public synchronized Card getTrumpCard() {return trumpCard;}

    public synchronized void setTrumpCard(Card trumpCard) {this.trumpCard = trumpCard;}

    public synchronized GameState getGameState() {return gameState;}

    public synchronized void setGameState(GameState gameState) {this.gameState = gameState;}

    public synchronized ConcurrentHashMap<String, PlayerInterface> getPlayersWithUsernames() {return playersWithUsernames;}

    public synchronized GameType getGameType() {return gameType;}

    public synchronized void setGameType(GameType gameType) {this.gameType = gameType;}

    public synchronized String getUsernameOfPlayerOnTurn() {return usernameOfPlayerOnTurn;}

    public synchronized void setUsernameOfPlayerOnTurn(String usernameOfPlayerOnTurn) {this.usernameOfPlayerOnTurn = usernameOfPlayerOnTurn;}

    public synchronized ConcurrentHashMap<String, Card> getPressedCards() {return pressedCards;}

    public synchronized void setPressedCards(ConcurrentHashMap<String, Card> pressedCards) {this.pressedCards = pressedCards;}

    public synchronized ConcurrentHashMap<String, Card> getCurrentTrick() {return currentTrick;}

    public synchronized void setCurrentTrick(ConcurrentHashMap<String, Card> currentTrick) {this.currentTrick = currentTrick;}

    public synchronized int getIndexOfPlayerOnTurn() {return indexOfPlayerOnTurn;}

    public synchronized void setIndexOfPlayerOnTurn(int indexOfPlayerOnTurn) {this.indexOfPlayerOnTurn = indexOfPlayerOnTurn;}

    public synchronized Colour getLeadSuit() {return leadSuit;}

    public synchronized void setLeadSuit(Colour leadSuit) {this.leadSuit = leadSuit;}

    public synchronized List<String> getUsernames() {return usernames;}

    public synchronized void setUsernames(List<String> usernames) {this.usernames = usernames;}

    public synchronized ConcurrentHashMap<String, List<List<Card>>> getWonTricksMapping() {return wonTricksMapping;}

    public synchronized void setWonTricksMapping(ConcurrentHashMap<String, List<List<Card>>> wonTricksMapping) {this.wonTricksMapping = wonTricksMapping;}

    public synchronized HashMap<Integer, PlayerInterface> getGameResult() {return gameResult;}

    public synchronized void setGameResult(HashMap<Integer, PlayerInterface> gameResult) {this.gameResult = gameResult;}

    public synchronized ArrayList<String> getLogData() {return logData;}

    public synchronized void setLogData(ArrayList<String> logData) {this.logData = logData;}

    public synchronized int getPartyCount() {return partyCount;}

    public synchronized void setPartyCount(int partyCount) {this.partyCount = partyCount;}
}