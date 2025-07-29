package controllerlogic;

import com.group06.cosmiceidex.common.LeaderboardEntry;
import com.group06.cosmiceidex.controllerlogic.BestenlisteControllerLogic;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BestenlisteControllerLogicTest {
    @Test
    public void testEntryTrue(){
        LeaderboardEntry entry = new LeaderboardEntry("a", 14, 11, 03);
        assertEquals("1. : a | Siege: 14 | Stiche: 11 | Punkte: 3", BestenlisteControllerLogic.createEntry(entry, 0));
    }

    @Test
    public void testEntryFalseNumber(){
        LeaderboardEntry entry = new LeaderboardEntry("a", 14, 11, 03);
        assertNotEquals("2. : a | Siege: 14 | Stiche: 11 | Punkte: 3", BestenlisteControllerLogic.createEntry(entry, 0));
    }

    @Test
    public void testEntryFalseName(){
        LeaderboardEntry entry = new LeaderboardEntry("a", 14, 11, 03);
        assertNotEquals("1. : b | Siege: 14 | Stiche: 11 | Punkte: 3", BestenlisteControllerLogic.createEntry(entry, 0));
    }
}