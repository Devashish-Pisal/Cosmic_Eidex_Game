package game;

import com.group06.cosmiceidex.bot.EasyBot;
import com.group06.cosmiceidex.bot.HardBot;
import com.group06.cosmiceidex.game.GameController;
import com.group06.cosmiceidex.game.PlayerInterface;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BotTest {

    private static final int NUM_ROUNDS = 10000;

    @Test
    public void testEasyVsEasyVsEasy() {
        int bot1Wins = 0;
        int bot2Wins = 0;
        int bot3Wins = 0;

        for (int i = 0; i < NUM_ROUNDS; i++) {
            PlayerInterface bot1 = new EasyBot("EasyBot1");
            PlayerInterface bot2 = new EasyBot("EasyBot2");
            PlayerInterface bot3 = new EasyBot("EasyBot3");

            GameController game = new GameController(bot1, bot2, bot3);
            game.playRound();

            PlayerInterface winner = getRoundWinner(bot1, bot2, bot3);

            switch (winner.getUsername()) {
                case "EasyBot1" -> bot1Wins++;
                case "EasyBot2" -> bot2Wins++;
                case "EasyBot3" -> bot3Wins++;
            }
        }

        System.out.println("\n===== EasyBots =====");
        System.out.println("EasyBot1 wins: " + bot1Wins);
        System.out.println("EasyBot2 wins: " + bot2Wins);
        System.out.println("EasyBot3 wins: " + bot3Wins);
        System.out.println("====================\n");

        double expected = NUM_ROUNDS / 3.0;
        double tolerance = NUM_ROUNDS * 0.15;

        assertTrue(Math.abs(bot1Wins - expected) < tolerance, "EasyBot1 sollte ähnlich oft gewinnen.");
        assertTrue(Math.abs(bot2Wins - expected) < tolerance, "EasyBot2 sollte ähnlich oft gewinnen.");
        assertTrue(Math.abs(bot3Wins - expected) < tolerance, "EasyBot3 sollte ähnlich oft gewinnen.");
    }

    @Test
    public void testHardVsHardVsHard() {
        int bot1Wins = 0;
        int bot2Wins = 0;
        int bot3Wins = 0;

        for (int i = 0; i < NUM_ROUNDS; i++) {
            PlayerInterface bot1 = new HardBot("HardBot1");
            PlayerInterface bot2 = new HardBot("HardBot2");
            PlayerInterface bot3 = new HardBot("HardBot3");

            GameController game = new GameController(bot1, bot2, bot3);
            game.playRound();

            PlayerInterface winner = getRoundWinner(bot1, bot2, bot3);

            switch (winner.getUsername()) {
                case "HardBot1" -> bot1Wins++;
                case "HardBot2" -> bot2Wins++;
                case "HardBot3" -> bot3Wins++;
            }
        }

        System.out.println("\n===== HardBots =====");
        System.out.println("HardBot1 wins: " + bot1Wins);
        System.out.println("HardBot2 wins: " + bot2Wins);
        System.out.println("HardBot3 wins: " + bot3Wins);
        System.out.println("====================\n");

        double expected = NUM_ROUNDS / 3.0;
        double tolerance = NUM_ROUNDS * 0.15;

        assertTrue(Math.abs(bot1Wins - expected) < tolerance, "HardBot1 sollte ähnlich oft gewinnen.");
        assertTrue(Math.abs(bot2Wins - expected) < tolerance, "HardBot2 sollte ähnlich oft gewinnen.");
        assertTrue(Math.abs(bot3Wins - expected) < tolerance, "HardBot3 sollte ähnlich oft gewinnen.");
    }

    @Test
    public void testEasyVsEasyVsHard() {
        int easy1Wins = 0;
        int easy2Wins = 0;
        int hardWins = 0;

        for (int i = 0; i < NUM_ROUNDS; i++) {
            PlayerInterface easy1 = new EasyBot("EasyBot1");
            PlayerInterface easy2 = new EasyBot("EasyBot2");
            PlayerInterface hard = new HardBot("HardBot");

            GameController game = new GameController(easy1, easy2, hard);
            game.playRound();

            PlayerInterface winner = getRoundWinner(easy1, easy2, hard);

            switch (winner.getUsername()) {
                case "EasyBot1" -> easy1Wins++;
                case "EasyBot2" -> easy2Wins++;
                case "HardBot" -> hardWins++;
            }
        }

        System.out.println("\n======= EasyBots vs HardBot =======");
        System.out.println("EasyBot1 wins: " + easy1Wins);
        System.out.println("EasyBot2 wins: " + easy2Wins);
        System.out.println("HardBot wins:  " + hardWins);
        System.out.println("===================================\n");

        int easyWins = easy1Wins + easy2Wins;

        assertTrue(hardWins > easy1Wins, "HardBot sollte öfter gewinnen als EasyBot1.");
        assertTrue(hardWins > easy2Wins, "HardBot sollte öfter gewinnen als EasyBot2.");
        assertTrue(hardWins > easyWins / 2, "HardBot sollte häufiger gewinnen als die beiden EasyBots einzeln betrachtet.");
    }

    private PlayerInterface getRoundWinner(PlayerInterface p1, PlayerInterface p2, PlayerInterface p3) {
        Map<PlayerInterface, Integer> scores = new HashMap<>();
        scores.put(p1, p1.getSumOfTrickPoints());
        scores.put(p2, p2.getSumOfTrickPoints());
        scores.put(p3, p3.getSumOfTrickPoints());

        return scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .get()
                .getKey();
    }
}
