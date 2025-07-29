package common;

import com.group06.cosmiceidex.common.RoomCredential;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class RoomCredentialTest {
    RoomCredential rc = null;

    @Test
    public void testConstructor(){
        assertNull(rc);
        rc = new RoomCredential("TEST-ROOM", "TEST-PASSWORD");
        assertNotNull(rc);
    }

    @Test
    public void testGetRoomName(){
        assertNull(rc);
        rc = new RoomCredential("TEST-ROOM", "TEST-PASSWORD");
        assertNotNull(rc);
        assertEquals("TEST-ROOM", rc.getRoomName());
    }

    @Test
    public void testGetPassword(){
        assertNull(rc);
        rc = new RoomCredential("TEST-ROOM", "TEST-PASSWORD");
        assertNotNull(rc);
        assertEquals("TEST-PASSWORD", rc.getPassword());
    }
}
