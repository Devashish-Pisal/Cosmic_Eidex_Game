package controllerlogic;

import com.group06.cosmiceidex.game.Player;
import com.group06.cosmiceidex.game.PlayerInterface;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.group06.cosmiceidex.controllerlogic.SpielraumControllerLogic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.group06.cosmiceidex.controllerlogic.SpielraumControllerLogic.computeResults;
import static org.junit.jupiter.api.Assertions.*;

class SpielraumControllerLogicTest {
    @Test
    void testComputeResultsPlayer1WinnerByPartyPoints(){
        PlayerInterface player1 = new Player("test1");
        player1.setWonPartyPoints(3);
        PlayerInterface player2 = new Player("test2");
        player2.setWonPartyPoints(2);

        HashMap<Integer, PlayerInterface> correctMap = new HashMap<>();
        correctMap.put(1, player1);
        correctMap.put(2, player2);

        assertEquals(correctMap, computeResults(player1, player2));
    }

    @Test
    void testComputeResultsPlayer2WinnerByPartyPoints(){
        PlayerInterface player1 = new Player("test1");
        player1.setWonPartyPoints(3);
        PlayerInterface player2 = new Player("test2");
        player2.setWonPartyPoints(4);

        HashMap<Integer, PlayerInterface> correctMap = new HashMap<>();
        correctMap.put(1, player2);
        correctMap.put(2, player1);

        assertEquals(correctMap, computeResults(player1, player2));
    }

    @Test
    void testComputeResultsPlayer1WinnerByTrickPoints(){
        PlayerInterface player1 = new Player("test1");
        player1.setWonPartyPoints(3);
        player1.setSumOfTrickPoints(145);
        PlayerInterface player2 = new Player("test2");
        player2.setWonPartyPoints(3);
        player2.setSumOfTrickPoints(144);

        HashMap<Integer, PlayerInterface> correctMap = new HashMap<>();
        correctMap.put(1, player1);
        correctMap.put(2, player2);

        assertEquals(correctMap, computeResults(player1, player2));
    }

    @Test
    void testComputeResultsPlaye2WinnerByTrickPoints(){
        PlayerInterface player1 = new Player("test1");
        player1.setWonPartyPoints(3);
        player1.setSumOfTrickPoints(145);
        PlayerInterface player2 = new Player("test2");
        player2.setWonPartyPoints(3);
        player2.setSumOfTrickPoints(146);

        HashMap<Integer, PlayerInterface> correctMap = new HashMap<>();
        correctMap.put(1, player2);
        correctMap.put(2, player1);

        assertEquals(correctMap, computeResults(player1, player2));
    }
}