package com.group06.cosmiceidex.controllerlogic;

import com.group06.cosmiceidex.controllers.SpielraumController;
import com.group06.cosmiceidex.game.PlayerInterface;
import javafx.scene.image.ImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SpielraumControllerLogic {
    /**
     * Berechnet die Platzierungen der 2 Spieler, die noch im Spiel sind, nachdem der Dritte das Spiel verlassen hat
     * @param player1 Spieler 1
     * @param player2 Spieler 2
     * @return Map mit den Platzierungen
     */
    public static HashMap<Integer, PlayerInterface> computeResults(PlayerInterface player1, PlayerInterface player2){
        HashMap<Integer, PlayerInterface> resMap = new HashMap<>();
        if(player1.getWonPartyPoints() < player2.getWonPartyPoints()){
            resMap.put(1, player2);
            resMap.put(2, player1);
        }else if(player2.getWonPartyPoints() < player1.getWonPartyPoints()){
            resMap.put(1, player1);
            resMap.put(2, player2);
        }else if(player1.getWonPartyPoints() == player2.getWonPartyPoints()){
            if(player2.getSumOfTrickPoints() <= player1.getSumOfTrickPoints()){
                resMap.put(1, player1);
                resMap.put(2, player2);
            }else{
                resMap.put(1, player2);
                resMap.put(2, player1);
            }
        }
        return resMap;
    }
}
