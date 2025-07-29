package bots;

import com.group06.cosmiceidex.bots.EasyBot;
import com.group06.cosmiceidex.game.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class EasyBotTest {

    private EasyBot bot;

    @BeforeEach
    void setUp() {
        bot = new EasyBot("EasyBot");
    }

    private Card createCard(Colour colour, Value value) {
        return new Card(colour, value, colour.toString() + value.toString() + ".png");
    }

    @Test
    void testGetUsername() {
        assertEquals("EasyBot", bot.getUsername());
    }

    @Test
    void testAddCardToHandAndGetHand() {
        Card card1 = createCard(Colour.HERZ, Value.ZEHN);
        Card card2 = createCard(Colour.EIDEX, Value.BUBE);

        bot.addCardToHand(card1);
        bot.addCardToHand(card2);

        List<Card> hand = bot.getHand();
        assertTrue(hand.contains(card1));
        assertTrue(hand.contains(card2));
        assertEquals(2, hand.size());
    }

    @Test
    void testSetAndReplaceHand() {
        Card card1 = createCard(Colour.STERN, Value.ZEHN);
        Card card2 = createCard(Colour.HERZ, Value.NEUN);
        List<Card> newHand = Arrays.asList(card1, card2);

        bot.setHand(newHand);
        assertEquals(2, bot.getHand().size());
        assertTrue(bot.getHand().contains(card1));
        assertTrue(bot.getHand().contains(card2));
    }

    @Test
    void testPlayCardRemovesSpecificCard() {
        Card card1 = createCard(Colour.EIDEX, Value.KOENIG);
        Card card2 = createCard(Colour.STERN, Value.ACHT);
        bot.addCardToHand(card1);
        bot.addCardToHand(card2);

        bot.playCard(card1);
        assertFalse(bot.getHand().contains(card1));
        assertTrue(bot.getHand().contains(card2));
    }

    @Test
    void testClearHandRemovesAllCards() {
        bot.addCardToHand(createCard(Colour.HERZ, Value.ASS));
        bot.addCardToHand(createCard(Colour.RABE, Value.BUBE));
        bot.clearHand();
        assertTrue(bot.getHand().isEmpty());
    }

    @Test
    void testChooseCardToPressChoosesAndRemovesCard() {
        Card card1 = createCard(Colour.HERZ, Value.DAME);
        Card card2 = createCard(Colour.RABE, Value.SIEBEN);
        bot.addCardToHand(card1);
        bot.addCardToHand(card2);

        Card chosen = bot.chooseCardToPress();
        assertNotNull(chosen);
        assertFalse(bot.getHand().contains(chosen));
        assertEquals(1, bot.getHand().size());
    }

    @Test
    void testChooseCardToPressReturnsNullIfHandIsEmpty() {
        Card chosen = bot.chooseCardToPress();
        assertNull(chosen);
    }

    @Test
    void testChooseCardToPlayWithNullLeadSuit() {
        Card card1 = createCard(Colour.EIDEX, Value.NEUN);
        Card card2 = createCard(Colour.RABE, Value.SIEBEN);
        bot.addCardToHand(card1);
        bot.addCardToHand(card2);

        Card played = bot.chooseCardToPlay(null, new ConcurrentHashMap<>(), null, GameType.Obenabe);
        assertNotNull(played);
        assertFalse(bot.getHand().contains(played));
        assertEquals(1, bot.getHand().size());
    }

    @Test
    void testChooseCardToPlayWithMatchingLeadSuit() {
        Card card1 = createCard(Colour.STERN, Value.BUBE);
        Card card2 = createCard(Colour.HERZ, Value.ACHT);
        bot.addCardToHand(card1);
        bot.addCardToHand(card2);

        Card played = bot.chooseCardToPlay(Colour.STERN, new ConcurrentHashMap<>(), null, GameType.Normal);
        assertEquals(Colour.STERN, played.getColour());
    }

    @Test
    void testChooseCardToPlayWithoutMatchingLeadSuitReturnsFirstCard() {
        Card card1 = createCard(Colour.RABE, Value.ZEHN);
        Card card2 = createCard(Colour.EIDEX, Value.SIEBEN);
        bot.addCardToHand(card1);
        bot.addCardToHand(card2);

        Card played = bot.chooseCardToPlay(Colour.HERZ, new ConcurrentHashMap<>(), null, GameType.Undenuf);
        assertNotNull(played);
        assertEquals(card1, played);
    }

    @Test
    void testChooseCardToPlayReturnsNullIfHandIsEmpty() {
        Card played = bot.chooseCardToPlay(Colour.STERN, new ConcurrentHashMap<>(), null, GameType.Normal);
        assertNull(played);
    }

    @Test
    void testTrickPointsHandling() {
        bot.setSumOfTrickPoints(5);
        assertEquals(5, bot.getSumOfTrickPoints());

        bot.incrementSumOfTrickPoints(4);
        assertEquals(9, bot.getSumOfTrickPoints());
    }

    @Test
    void testPartyPointsHandling() {
        bot.setWonPartyPoints(2);
        assertEquals(2, bot.getWonPartyPoints());

        bot.incrementWonPartyPoints(3);
        assertEquals(5, bot.getWonPartyPoints());
    }

    @Test
    void testIsBotAndGameMasterFlags() {
        assertTrue(bot.isBot());
        bot.setBot(false);
        assertFalse(bot.isBot());

        bot.setGameMaster(true);
        assertTrue(bot.isGameMaster());
    }

    @Test
    void testTurnHandling() {
        bot.setMyTurn(true);
        assertTrue(bot.isMyTurn());
        bot.setMyTurn(false);
        assertFalse(bot.isMyTurn());
    }

    @Test
    void testTotalTrickPointsAndTricks() {
        bot.setTotalWonTrickPointDuringEntireGame(50);
        assertEquals(50, bot.getTotalWonTrickPointDuringEntireGame());

        bot.setNumberOfWonTricksDuringEntireGame(6);
        assertEquals(6, bot.getNumberOfWonTricksDuringEntireGame());
    }
}
