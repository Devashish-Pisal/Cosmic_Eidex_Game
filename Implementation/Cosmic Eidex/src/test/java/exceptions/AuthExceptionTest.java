package exceptions;

import com.group06.cosmiceidex.exceptions.AuthException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AuthExceptionTest {

    @Test
    void testAuthExceptionMessage() {
        String errorMessage = "Test authentication error";
        AuthException exception = new AuthException(errorMessage);
        assertEquals(errorMessage, exception.getMessage());
    }
}