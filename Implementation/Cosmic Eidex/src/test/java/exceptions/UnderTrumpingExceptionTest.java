package exceptions;

import com.group06.cosmiceidex.exceptions.RoomDoesNotExists;
import com.group06.cosmiceidex.exceptions.UnderTrumpingException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class UnderTrumpingExceptionTest {

    @Test
    void testAuthExceptionMessage() {
        String errorMessage = "Test authentication error";
        UnderTrumpingException exception = new UnderTrumpingException(errorMessage);
        assertEquals(errorMessage, exception.getMessage());
    }
}
