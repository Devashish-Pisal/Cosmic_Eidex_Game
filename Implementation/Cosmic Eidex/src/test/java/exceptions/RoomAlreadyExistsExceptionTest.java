package exceptions;

import com.group06.cosmiceidex.exceptions.RoomAlreadyExistsException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;



public class RoomAlreadyExistsExceptionTest {

    @Test
    void testAuthExceptionMessage() {
        String errorMessage = "Test authentication error";
        RoomAlreadyExistsException exception = new RoomAlreadyExistsException(errorMessage);
        assertEquals(errorMessage, exception.getMessage());
    }
}
