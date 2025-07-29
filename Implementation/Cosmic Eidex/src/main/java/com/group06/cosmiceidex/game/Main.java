package com.group06.cosmiceidex.game;

import com.group06.cosmiceidex.bot.EasyBot;
import com.group06.cosmiceidex.bot.HardBot;

/**
 * ---------------------------------------------------------------------------------------------------------------
 * ----------------------BOT-SIMULATIONSKLASSE FÜR EIGENTLICHE IMPLEMENTIERUNG IRRELEVANT-------------------------
 * ---------------------------------------------------------------------------------------------------------------
 */


/**
 * Startet eine Simulation für die Bots. Diese Klasse ist fürs eigentliche Projekt irrelevant und nur für die Bot
 * entwicklung nötig.
 */
public class Main {
    public static void main(String[] args) {
        PlayerInterface hardBot = new HardBot("HardBot");
        PlayerInterface easyBot1 = new EasyBot("RandomBot");
        PlayerInterface easyBot2 = new EasyBot("RadnomBot2");

        // Initialisiert den GameController mit den o.g. Bots
        GameController game = new GameController(easyBot1, hardBot, easyBot2);
        game.playRound();
    }
}