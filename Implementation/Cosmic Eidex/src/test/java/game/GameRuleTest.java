package game;

import org.junit.jupiter.api.Test;
import java.util.List;
import com.group06.cosmiceidex.game.*;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

class GameRulesTest {

    @Test
    void testGetCardPointsTrumpMode() {
        Card trumpBube = new Card(Colour.HERZ, Value.BUBE, "HerzBube.png");
        int points = GameRules.getCardPoints(trumpBube, Colour.HERZ, GameMode.TRUMP);
        assertEquals(20, points);
    }

    @Test
    void testGetCardPointsObenabeMode() {
        Card ass = new Card(Colour.RABE, Value.ASS, "RabeAs.png");
        int points = GameRules.getCardPoints(ass, null, GameMode.OBENABE);
        assertEquals(11, points);
    }

    @Test
    void testGetCardPointsUndenufeMode() {
        Card sechs = new Card(Colour.STERN, Value.SECHS, "Stern6.png");
        int points = GameRules.getCardPoints(sechs, null, GameMode.UNDENUFE);

        assertEquals(11, points);
    }

    @Test
    void testGetTrickWinnerTrump() {
        Card card1 = new Card(Colour.RABE, Value.BUBE, "RabeBube.png");
        Card card2 = new Card(Colour.HERZ, Value.ASS, "HerzAs.png");
        Card card3 = new Card(Colour.HERZ, Value.KOENIG, "HerzKoenig.png");
        List<Card> trick = List.of(card1, card2, card3);
        Card winner = GameRules.getTrickWinner(trick, Colour.HERZ, GameMode.TRUMP);

        assertEquals(card2, winner);
    }

    @Test
    void testGetTrickWinnerObenabe() {
        Card card1 = new Card(Colour.HERZ, Value.ASS, "HerzAs.png");
        Card card2 = new Card(Colour.HERZ, Value.KOENIG, "HerzKoenig.png");
        List<Card> trick = List.of(card1, card2);
        Card winner = GameRules.getTrickWinner(trick, null, GameMode.OBENABE);

        assertEquals(card1, winner);
    }

    @Test
    void testGetTrickWinnerUndenufe() {
        Card card1 = new Card(Colour.EIDEX, Value.SECHS, "Eidex6.png");
        Card card2 = new Card(Colour.EIDEX, Value.ACHT, "Eidex8.png");
        List<Card> trick = List.of(card2, card1);
        Card winner = GameRules.getTrickWinner(trick, null, GameMode.UNDENUFE);

        assertEquals(card1, winner);
    }

    @Test
    void testGetLegalMovesEmptyTrick() {
        List<Card> hand = List.of(
                new Card(Colour.HERZ, Value.ASS, "HerzAs.png"),
                new Card(Colour.RABE, Value.ZEHN, "Rabe10.png")
        );
        List<Card> legal = GameRules.getLegalMoves(hand, new ArrayList<>(), Colour.HERZ, GameMode.OBENABE);

        assertEquals(hand.size(), legal.size());
        assertTrue(legal.containsAll(hand));
    }

    @Test
    void testGetLegalMovesMustFollowSuit() {
        List<Card> hand = List.of(
                new Card(Colour.HERZ, Value.BUBE, "HerzBube.png"),
                new Card(Colour.RABE, Value.ASS, "RabeAs.png")
        );

        List<Card> currentTrick = List.of(
                new Card(Colour.HERZ, Value.ACHT, "Herz8.png")
        );
        List<Card> legal = GameRules.getLegalMoves(hand, currentTrick, Colour.RABE, GameMode.OBENABE);

        assertEquals(1, legal.size());
        assertEquals(Colour.HERZ, legal.get(0).getColour());
    }

    @Test
    void testGetLegalMoves() {
        List<Card> hand = List.of(
                new Card(Colour.RABE, Value.BUBE, "RabeBube.png"),
                new Card(Colour.RABE, Value.ASS, "RabeAs.png")
        );

        List<Card> currentTrick = List.of(
                new Card(Colour.HERZ, Value.KOENIG, "HerzKoenig.png")
        );

        List<Card> legal = GameRules.getLegalMoves(hand, currentTrick, Colour.RABE, GameMode.OBENABE);

        assertEquals(2, legal.size());
    }

    @Test
    void testGetLegalMovesTrumpOvertrump() {
        List<Card> hand = List.of(
                new Card(Colour.RABE, Value.BUBE, "RabeBube.png"),
                new Card(Colour.RABE, Value.DAME, "RabeDame.png"),
                new Card(Colour.STERN, Value.ACHT, "Stern8.png")
        );

        List<Card> currentTrick = List.of(
                new Card(Colour.RABE, Value.DAME, "RabeDame.png")
        );

        List<Card> legal = GameRules.getLegalMoves(hand, currentTrick, Colour.RABE, GameMode.TRUMP);

        assertFalse(legal.contains(new Card(Colour.RABE, Value.BUBE, "RabeBube.png")));
        assertFalse(legal.contains(new Card(Colour.STERN, Value.ACHT, "Stern8.png")));
    }

}
