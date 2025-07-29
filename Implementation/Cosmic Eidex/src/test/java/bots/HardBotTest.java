package bots;

import com.group06.cosmiceidex.bots.HardBot;
import com.group06.cosmiceidex.game.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HardBotTest {

    private HardBot bot;

    @Mock
    private GameSession gameSession;

    @BeforeEach
    void setUp() {
        bot = new HardBot("TestBot");
        bot.setGameSession(gameSession);
    }

    private Card createCard(Colour colour, Value value) {
        return new Card(colour, value, colour.toString() + value.toString() + ".png");
    }

    @Test
    void testChooseCardToPressObenabeChoosesLowestPointCard() {
        when(gameSession.getGameType()).thenReturn(GameType.Obenabe);
        bot.setHand(new ArrayList<>(List.of(
                createCard(Colour.EIDEX, Value.ASS),
                createCard(Colour.RABE, Value.ZEHN),
                createCard(Colour.HERZ, Value.SIEBEN)
        )));

        Card pressedCard = bot.chooseCardToPress();

        assertEquals(Value.SIEBEN, pressedCard.getValue());
        assertEquals(Colour.HERZ, pressedCard.getColour());
    }

    @Test
    void testChooseCardToPressUndenufChoosesHighestNonSix() {
        when(gameSession.getGameType()).thenReturn(GameType.Undenuf);
        bot.setHand(new ArrayList<>(List.of(
                createCard(Colour.EIDEX, Value.SECHS),
                createCard(Colour.HERZ, Value.SIEBEN),
                createCard(Colour.EIDEX, Value.KOENIG)
        )));

        Card pressedCard = bot.chooseCardToPress();

        assertEquals(Value.KOENIG, pressedCard.getValue());
    }

    @Test
    void testChooseCardToPressUndenufOnlyNonSix() {
        when(gameSession.getGameType()).thenReturn(GameType.Undenuf);
        bot.setHand(new ArrayList<>(List.of(
                createCard(Colour.EIDEX, Value.SECHS),
                createCard(Colour.HERZ, Value.SECHS),
                createCard(Colour.HERZ, Value.SIEBEN)
        )));

        Card pressedCard = bot.chooseCardToPress();

        assertEquals(Value.SIEBEN, pressedCard.getValue());
    }

    @Test
    void testChooseCardToPressNormalChoosesWorthlessShortestSuitCard() {
        when(gameSession.getGameType()).thenReturn(GameType.Normal);
        when(gameSession.getTrumpSuit()).thenReturn(Colour.EIDEX);

        bot.setHand(new ArrayList<>(List.of(
                createCard(Colour.EIDEX, Value.BUBE),
                createCard(Colour.EIDEX, Value.NEUN),
                createCard(Colour.HERZ, Value.SIEBEN),
                createCard(Colour.HERZ, Value.SECHS),
                createCard(Colour.RABE, Value.SIEBEN)
        )));

        Card pressedCard = bot.chooseCardToPress();

        assertEquals(Colour.RABE, pressedCard.getColour());
        assertEquals(Value.SIEBEN, pressedCard.getValue());
    }

    @Test
    void testChooseCardToPressNormalFallbackIfNoZeroPointCard() {
        when(gameSession.getGameType()).thenReturn(GameType.Normal);
        when(gameSession.getTrumpSuit()).thenReturn(Colour.EIDEX);

        bot.setHand(new ArrayList<>(List.of(
                createCard(Colour.EIDEX, Value.ASS),
                createCard(Colour.HERZ, Value.ASS),
                createCard(Colour.RABE, Value.ZEHN)
        )));

        Card pressedCard = bot.chooseCardToPress();

        assertNotNull(pressedCard);
        assertEquals(Colour.EIDEX, pressedCard.getColour());
        assertEquals(Value.ASS, pressedCard.getValue());
    }

    @Test
    void testChooseCardToPlayLeadUndenufPlaysLowestCard() {
        bot.setHand(new ArrayList<>(List.of(
                createCard(Colour.HERZ, Value.SIEBEN),
                createCard(Colour.EIDEX, Value.KOENIG),
                createCard(Colour.STERN, Value.ASS)
        )));

        Card played = bot.chooseCardToPlay(null, new ConcurrentHashMap<>(), null, GameType.Undenuf);

        assertEquals(Value.SIEBEN, played.getValue());
    }

    @Test
    void testChooseCardToPlayFollowAndWinUndenuf() {
        ConcurrentHashMap<String, Card> trick = new ConcurrentHashMap<>();
        trick.put("Player1", createCard(Colour.EIDEX, Value.ASS));

        bot.setHand(new ArrayList<>(List.of(
                createCard(Colour.EIDEX, Value.KOENIG),
                createCard(Colour.EIDEX, Value.SIEBEN),
                createCard(Colour.EIDEX, Value.SECHS)
        )));

        Card played = bot.chooseCardToPlay(Colour.EIDEX, trick, null, GameType.Undenuf);

        assertEquals(Value.KOENIG, played.getValue());
    }

    @Test
    void testChooseCardToPlayFollowAndLoseUndenuf() {
        ConcurrentHashMap<String, Card> trick = new ConcurrentHashMap<>();
        trick.put("Player1", createCard(Colour.EIDEX, Value.SECHS));

        bot.setHand(new ArrayList<>(List.of(
                createCard(Colour.EIDEX, Value.KOENIG),
                createCard(Colour.EIDEX, Value.ASS),
                createCard(Colour.HERZ, Value.SIEBEN)
        )));

        Card played = bot.chooseCardToPlay(Colour.EIDEX, trick, null, GameType.Undenuf);

        assertEquals(Value.ASS, played.getValue());
    }

    @Test
    void testGetLegalMovesWhenLeadingReturnsAllCards() {
        List<Card> hand = List.of(
                createCard(Colour.HERZ, Value.ASS),
                createCard(Colour.EIDEX, Value.BUBE),
                createCard(Colour.RABE, Value.SIEBEN)
        );

        List<Card> trick = new ArrayList<>();
        List<Card> legal = bot.getLegalMoves(hand, trick, Colour.EIDEX, GameType.Normal);

        assertEquals(hand.size(), legal.size());
        assertTrue(legal.containsAll(hand));
    }


    @Test
    void testGetLegalMovesMustTrumpIfNoMatchingSuit() {
        List<Card> hand = List.of(
                createCard(Colour.STERN, Value.KOENIG),
                createCard(Colour.EIDEX, Value.BUBE),
                createCard(Colour.RABE, Value.SIEBEN)
        );

        List<Card> trick = new ArrayList<>();
        trick.add(createCard(Colour.HERZ, Value.ASS));

        List<Card> legal = bot.getLegalMoves(hand, trick, Colour.EIDEX, GameType.Normal);

        assertEquals(1, legal.size());
        assertEquals(Colour.EIDEX, legal.get(0).getColour());
    }

    @Test
    void testGetLegalMovesCanDiscardIfNoFollowOrTrump() {
        List<Card> hand = List.of(
                createCard(Colour.STERN, Value.KOENIG),
                createCard(Colour.RABE, Value.SIEBEN)
        );

        List<Card> trick = new ArrayList<>();
        trick.add(createCard(Colour.HERZ, Value.ASS));

        List<Card> legal = bot.getLegalMoves(hand, trick, Colour.EIDEX, GameType.Normal);

        assertEquals(hand.size(), legal.size());
        assertTrue(legal.containsAll(hand));
    }
}
