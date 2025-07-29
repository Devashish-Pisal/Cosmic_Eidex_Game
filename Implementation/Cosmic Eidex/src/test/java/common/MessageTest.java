package common;

import com.group06.cosmiceidex.common.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class MessageTest {
    Message msg;
    HashMap<String, String> userCredential;

    @BeforeEach
    public void setUp(){
        userCredential = new HashMap<>();
        userCredential.put("TEST-USER", "TEST-PASSWORD");
        msg = new Message(Message.MessageType.LOGIN_REQUEST, "TEST-USER", userCredential);
    }

    @Test
    public void testGetType() {
        assertEquals(Message.MessageType.LOGIN_REQUEST, msg.getType());
    }

    @Test
    public void testGetSender() {
        assertEquals("TEST-USER", msg.getSender());
    }

    @Test
    public void testGetPayload() {
        assertEquals(userCredential, msg.getPayload());
    }

    @Test
    public void testSetPayload() {
        String newPayload = "new payload";
        msg.setPayload(newPayload);
        assertEquals(newPayload, msg.getPayload());
    }

    @Test
    public void testGetTargetWhenNotSet() {
        assertNull(msg.getTarget());
    }

    @Test
    public void testGetTargetWhenSet() {
        Message msgWithTarget = new Message(Message.MessageType.LOGIN_REQUEST, "TEST-USER", "target", userCredential);
        assertEquals("target", msgWithTarget.getTarget());
    }

    @Test
    public void testSetType() {
        assertNotNull(msg);
        msg.setType(Message.MessageType.LOGOUT_REQUEST);
        assertEquals(Message.MessageType.LOGOUT_REQUEST, msg.getType());
    }

    @Test
    public void testConstructorWithThreeArgs() {
        Message msg = new Message(Message.MessageType.LOGIN_REQUEST, "TEST-USER", userCredential);
        assertEquals(Message.MessageType.LOGIN_REQUEST, msg.getType());
        assertEquals("TEST-USER", msg.getSender());
        assertEquals(userCredential, msg.getPayload());
        assertNull(msg.getTarget());
    }

    @Test
    public void testConstructorWithFourArgs() {
        Message msg = new Message(Message.MessageType.LOGIN_REQUEST, "TEST-USER", "target", userCredential);
        assertEquals(Message.MessageType.LOGIN_REQUEST, msg.getType());
        assertEquals("TEST-USER", msg.getSender());
        assertEquals(userCredential, msg.getPayload());
        assertEquals("target", msg.getTarget());
    }
}
