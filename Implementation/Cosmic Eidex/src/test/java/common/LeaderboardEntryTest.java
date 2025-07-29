package common;

import org.junit.jupiter.api.Test;
import com.group06.cosmiceidex.common.LeaderboardEntry;

import static org.junit.jupiter.api.Assertions.*;

class LeaderboardEntryTest {

    @Test
    void testConstructorAndGetters() {
        LeaderboardEntry entry = new LeaderboardEntry("Player1", 5, 12, 300);

        assertEquals("Player1", entry.getUserName());
        assertEquals(5, entry.getWins());
        assertEquals(12, entry.getTricks());
        assertEquals(300, entry.getPoints());
    }

    @Test
    void testToString() {
        LeaderboardEntry entry = new LeaderboardEntry("Alice", 7, 21, 420);
        String expected = "Alice | Siege: 7 | Stiche: 21 | Punkte: 420";
        assertEquals(expected, entry.toString());
    }

    @Test
    void testFromStringValid() {
        String line = "Bob | Siege: 3 | Stiche: 9 | Punkte: 150";
        LeaderboardEntry entry = LeaderboardEntry.fromString(line);

        assertNotNull(entry);
        assertEquals("Bob", entry.getUserName());
        assertEquals(3, entry.getWins());
        assertEquals(9, entry.getTricks());
        assertEquals(150, entry.getPoints());
    }

    @Test
    void testFromStringWithWhitespace() {
        String line = "  Carol | Siege: 1 | Stiche: 5 | Punkte: 75  ";
        LeaderboardEntry entry = LeaderboardEntry.fromString(line);

        assertNotNull(entry);
        assertEquals("Carol", entry.getUserName());
        assertEquals(1, entry.getWins());
        assertEquals(5, entry.getTricks());
        assertEquals(75, entry.getPoints());
    }

    @Test
    void testFromStringInvalidFormat() {
        assertNull(LeaderboardEntry.fromString("invalid string"));
        assertNull(LeaderboardEntry.fromString("User | Wins: 3 | Points: 100"));
        assertNull(LeaderboardEntry.fromString(""));
        assertNull(LeaderboardEntry.fromString(null));
    }

    @Test
    void testEqualsAndHashCode() {
        LeaderboardEntry entry1 = new LeaderboardEntry("Dave", 2, 10, 200);
        LeaderboardEntry entry2 = new LeaderboardEntry("Dave", 5, 15, 300);
        LeaderboardEntry entry3 = new LeaderboardEntry("Eve", 2, 10, 200);

        assertEquals(entry1, entry2);
        assertNotEquals(entry1, entry3);
        assertEquals(entry1.hashCode(), entry2.hashCode());
        assertNotEquals(entry1.hashCode(), entry3.hashCode());
    }

    @Test
    void testEqualsWithSameObject() {
        LeaderboardEntry entry = new LeaderboardEntry("Same", 1, 1, 1);
        assertEquals(entry, entry);
    }

    @Test
    void testEqualsWithNullAndDifferentClass() {
        LeaderboardEntry entry = new LeaderboardEntry("Test", 1, 2, 3);
        assertNotEquals(null, entry);
        assertNotEquals("NotAnEntry", entry);
    }
}
