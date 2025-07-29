package exceptions;

import com.group06.cosmiceidex.exceptions.RoomDoesNotExists;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;



public class RoomDoesNotExistsTest {

    @Test
    void testAuthExceptionMessage() {
        String errorMessage = "Test authentication error";
        RoomDoesNotExists exception = new RoomDoesNotExists(errorMessage);
        assertEquals(errorMessage, exception.getMessage());
    }
}

