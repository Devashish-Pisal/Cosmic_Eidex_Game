package common;

import com.group06.cosmiceidex.common.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testDefaultConstructor() {
        User user = new User();
        assertNull(user.getUsername());
        assertNull(user.getPassword());
    }

    @Test
    void testConstructorWithParameters() {
        User user = new User("testuser", "password123");
        assertEquals("testuser", user.getUsername());
        assertEquals("password123", user.getPassword());
    }

    @Test
    void testGetUsername() {
        User user = new User("testuser", "password");
        assertEquals("testuser", user.getUsername());
    }

    @Test
    void testSetUsername() {
        User user = new User();
        user.setUsername("newuser");
        assertEquals("newuser", user.getUsername());
    }

    @Test
    void testGetPassword() {
        User user = new User("user", "secret");
        assertEquals("secret", user.getPassword());
    }

    @Test
    void testSetPassword() {
        User user = new User();
        user.setPassword("newsecret");
        assertEquals("newsecret", user.getPassword());
    }

    @Test
    void testEquals() {
        User user1 = new User("testuser", "pass1");
        User user2 = new User("testuser", "pass2");
        User user3 = new User("anotheruser", "pass1");
        User user4 = new User("testuser", "pass1");

        assertEquals(user1, user2);
        assertEquals(user1, user4);
        assertTrue(user1.equals(user2));
        assertNotEquals(user1, user3);
        assertFalse(user1.equals(user3));
        assertNotEquals(null, user1);
        assertNotEquals(user1, new Object());
        assertEquals(user1, user1);
    }

    @Test
    void testHashCode() {
        User user1 = new User("testuser", "pass1");
        User user2 = new User("testuser", "pass2");
        User user3 = new User("anotheruser", "pass1");

        assertEquals(user1.hashCode(), user2.hashCode());
        assertNotEquals(user1.hashCode(), user3.hashCode());
    }

    @Test
    void testToString() {
        User user = new User("myuser", "mypass");
        String expectedString = "User{username='myuser'}";
        assertEquals(expectedString, user.toString());
    }
}
