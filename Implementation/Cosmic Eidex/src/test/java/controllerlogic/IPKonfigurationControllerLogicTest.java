package controllerlogic;

import com.group06.cosmiceidex.controllerlogic.IPKonfigurationControllerLogic;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IPKonfigurationControllerLogicTest {

    @Test
    void isValidIPTrue() {
        String ip = "123.123.123.10";
        assertEquals(true, IPKonfigurationControllerLogic.isValidIP(ip));
    }

    @Test
    void isValidIPFalse() {
        String ip = "256.256.256.256";
        assertEquals(false, IPKonfigurationControllerLogic.isValidIP(ip));
    }

    @Test
    void isValidPortTrue() {
        int port = 1234;
        assertEquals(true, IPKonfigurationControllerLogic.isValidPort(port));
    }

    @Test
    void isValidPortFalse() {
        int port = 123456789;
        assertEquals(false, IPKonfigurationControllerLogic.isValidPort(port));
    }
}