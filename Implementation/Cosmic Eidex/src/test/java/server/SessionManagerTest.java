package server;

import com.group06.cosmiceidex.common.Message;
import com.group06.cosmiceidex.common.RoomCredential;
import com.group06.cosmiceidex.exceptions.RoomDoesNotExists;
import com.group06.cosmiceidex.server.ClientHandler;
import com.group06.cosmiceidex.server.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class SessionManagerTest {

    @Mock
    private ClientHandler clientHandler;
    @Mock
    private ClientHandler clientHandler1;

    @InjectMocks
    private SessionManager sessionManager;

    private String roomName;

    @BeforeEach
    public void setUp(){
        sessionManager = SessionManager.getSessionManagerInstance();
        // Reset all state
        sessionManager.getActiveRooms().clear();
        sessionManager.getLoggedInClients().clear();
        sessionManager.getActiveRooms().put("LOBBY", ConcurrentHashMap.newKeySet());

        roomName = "TEST-ROOM";
    }


    @Test
    public void testGetSessionManagerInstance(){
        assertEquals(sessionManager,SessionManager.getSessionManagerInstance());
        assertNotNull(sessionManager);
    }

    @Test
    public void testAddClientHandlerToRoom(){
        when(clientHandler.getUsername()).thenReturn("TEST-USER");
        assertNotNull(sessionManager);
        sessionManager.addClientHandlerToRoom(roomName, clientHandler);
        assertEquals(2, sessionManager.getActiveRooms().size());
        assertTrue(sessionManager.getActiveRooms().containsKey(roomName));
        assertEquals(1, sessionManager.getActiveRooms().get(roomName).size());
        assertTrue(sessionManager.getActiveRooms().get(roomName).contains(clientHandler));
        assertEquals(clientHandler, sessionManager.getLoggedInClients().get("TEST-USER"));
    }


    @Test
    public void testGetClientHandlerListOfRoom(){
        when(clientHandler.getUsername()).thenReturn("TEST-USER");
        assertNotNull(sessionManager);
        sessionManager.addClientHandlerToRoom(roomName, clientHandler);
        Set<ClientHandler> handlers = sessionManager.getClientHandlerListOfRoom(roomName);
        assertEquals(1, handlers.size());
        assertTrue(handlers.contains(clientHandler));
        assertThrows(RoomDoesNotExists.class,() -> sessionManager.getClientHandlerListOfRoom("UNKNOWN-ROOM"));
    }

    @Test
    public void testRemoveClientHandlerFromRoom(){
        when(clientHandler.getUsername()).thenReturn("TEST-USER");
        sessionManager.addClientHandlerToRoom(roomName,clientHandler);
        assertEquals(2,sessionManager.getActiveRooms().size());
        ClientHandler ch = sessionManager.removeClientHandlerFromRoom(roomName, clientHandler.getUsername());
        assertEquals(1, sessionManager.getActiveRooms().size());
        assertEquals(clientHandler,ch);
        ClientHandler nullClientHandler = sessionManager.removeClientHandlerFromRoom(roomName,clientHandler.getUsername());
        assertNull(nullClientHandler);
    }

    @Test
    public void testBroadcastMessageInRoom_clientHandlersNotNull(){
        when(clientHandler.getUsername()).thenReturn("TEST-USER");
        when(clientHandler1.getUsername()).thenReturn("TEST-USER1");
        Message msg = new Message(null,null,null);
        sessionManager.addClientHandlerToRoom("LOBBY", clientHandler);
        sessionManager.addClientHandlerToRoom("LOBBY", clientHandler1);
        sessionManager.broadcastMessageInRoom("LOBBY", msg);
        Mockito.verify(clientHandler,times(1)).sendMessageToClient(msg);
        Mockito.verify(clientHandler1,times(1)).sendMessageToClient(msg);
    }

    @Test
    public void testBroadcastMessageInRoom_clientHandlersNull(){
        Message msg = new Message(null,null,null);
        SessionManager spySessionManager = spy(sessionManager);
        spySessionManager.getActiveRooms().remove("TEST-ROOM");
        spySessionManager.broadcastMessageInRoom("TEST-ROOM", msg);
    }


    @Test
    public void testLogOut(){
        when(clientHandler.getUsername()).thenReturn("TEST-USER");
        sessionManager.addClientHandlerToRoom("TEST-ROOM", clientHandler);
        assertEquals(1,sessionManager.getLoggedInClients().size());
        assertEquals(2, sessionManager.getActiveRooms().size());
        sessionManager.logOut(clientHandler.getUsername(),"TEST-ROOM");
        assertEquals(0,sessionManager.getLoggedInClients().size());
        assertEquals(1, sessionManager.getActiveRooms().size());
    }

    @Test
    public void testConstructMapForActiveRooms(){
        when(clientHandler.getUsername()).thenReturn("TEST-USER");
        when(clientHandler1.getUsername()).thenReturn("TEST-USER1");
        sessionManager.addClientHandlerToRoom("TEST-ROOM", clientHandler);
        sessionManager.addClientHandlerToRoom("TEST-ROOM", clientHandler1);
        List<String> list = new ArrayList<>();
        list.add("TEST-USER");
        list.add("TEST-USER1");
        HashMap<String, List<String>> output = sessionManager.constructMapForActiveRooms();
        assertTrue(output.containsKey("TEST-ROOM"));
        assertNotNull(output.get("TEST-ROOM"));
        assertTrue(list.containsAll(output.get("TEST-ROOM")));
        assertTrue(output.get("TEST-ROOM").containsAll(list));
    }

    @Test
    public void testValidateRoomCredentialsWhenRoomDoesNotExists(){
        RoomCredential cred = new RoomCredential("TEST-ROOM", "TEST-PASSWORD");
        assertThrows(RoomDoesNotExists.class, ()->{
            sessionManager.validateRoomCredentials(cred);
        });
    }

    @Test
    public void testValidateRoomCredentialsWhenRoomExistsAndPasswordIsCorrect() throws NoSuchFieldException, IllegalAccessException {
        Field roomCredentialsField = SessionManager.class.getDeclaredField("roomCredentials");
        roomCredentialsField.setAccessible(true);
        List<RoomCredential> roomCredentials = (List<RoomCredential>) roomCredentialsField.get(sessionManager);
        RoomCredential rc = new RoomCredential("TEST-ROOM", "TEST-PASSWORD");
        roomCredentials.add(rc);
        assertTrue(sessionManager.validateRoomCredentials(rc));
    }

    @Test
    public void testValidateRoomCredentialsWhenRoomExistsAndPasswordIsWrong() throws NoSuchFieldException, IllegalAccessException {
        Field roomCredentialsField = SessionManager.class.getDeclaredField("roomCredentials");
        roomCredentialsField.setAccessible(true);
        List<RoomCredential> roomCredentials = (List<RoomCredential>) roomCredentialsField.get(sessionManager);
        RoomCredential rc = new RoomCredential("TEST-ROOM", "TEST-PASSWORD");
        roomCredentials.add(rc);
        RoomCredential newRC = new RoomCredential("TEST-ROOM", "WRONG-PASSWORD");
        assertFalse(sessionManager.validateRoomCredentials(newRC));
    }

    @Test
    public void testCloseRoom_WithLobbyRoom_ShouldNotCloseRoom() {
        ConcurrentHashMap<String, Set<ClientHandler>> activeRooms = sessionManager.getActiveRooms();
        List<RoomCredential> roomCredentials = sessionManager.getRoomCredentials();
        ConcurrentHashMap<String, Set<ClientHandler>> spyActiveRoom = spy(activeRooms);
        List<RoomCredential> spyRoomCredential = spy(roomCredentials);
        sessionManager.closeRoom(roomName);
        verify(spyActiveRoom, never()).remove(anyString());
        verify(spyRoomCredential, never()).remove(any());
    }


    @Test
    public void testCloseRoom_RoomCredsNotFound()  {
        Set<ClientHandler> handlers = new HashSet<>();
        handlers.add(clientHandler);
        handlers.add(clientHandler1);
        when(clientHandler.getUsername()).thenReturn("TEST-USER-1");
        when(clientHandler1.getUsername()).thenReturn("TEST-USER-2");
        sessionManager.getActiveRooms().put("TEST-ROOM", handlers);
        SessionManager spySessionManager = spy(sessionManager);
        spySessionManager.closeRoom("TEST-ROOM");
        verify(spySessionManager).addClientHandlerToRoom("LOBBY", clientHandler);
        verify(spySessionManager).addClientHandlerToRoom("LOBBY", clientHandler1);
    }

    @Test
    public void testCloseRoom_RoomCredsFound()  {
        Set<ClientHandler> handlers = new HashSet<>();
        handlers.add(clientHandler);
        handlers.add(clientHandler1);
        List<RoomCredential> creds = new ArrayList<>();
        creds.add(new RoomCredential("TEST-ROOM", "TEST-PASSWORD"));
        sessionManager.setRoomCredentials(creds);
        when(clientHandler.getUsername()).thenReturn("TEST-USER-1");
        when(clientHandler1.getUsername()).thenReturn("TEST-USER-2");
        sessionManager.getActiveRooms().put("TEST-ROOM", handlers);
        SessionManager spySessionManager = spy(sessionManager);
        spySessionManager.closeRoom("TEST-ROOM");
        verify(spySessionManager).addClientHandlerToRoom("LOBBY", clientHandler);
        verify(spySessionManager).addClientHandlerToRoom("LOBBY", clientHandler1);
        assertTrue(spySessionManager.getRoomCredentials().isEmpty());
    }

    @Test
    public void testRefreshRoomParticipants() {
        Set<ClientHandler> handlers = new HashSet<>();
        handlers.add(clientHandler);
        handlers.add(clientHandler1);
        List<String> list = new ArrayList<>();
        list.add("TEST-USER-2");
        list.add("TEST-USER-1 (Admin)");
        when(clientHandler.getIsGameMaster()).thenReturn(true);
        when(clientHandler.getUsername()).thenReturn("TEST-USER-1");
        when(clientHandler1.getUsername()).thenReturn("TEST-USER-2");
        sessionManager.getActiveRooms().put("TEST-ROOM", handlers);
        assertTrue(list.containsAll(sessionManager.refreshRoomParticipants("TEST-ROOM")));
        assertTrue(sessionManager.refreshRoomParticipants("TEST-ROOM").containsAll(list));
    }

    @Test
    public void testCloseRoomWithoutSendingResponse_WithLobbyRoom_ShouldNotCloseRoom(){
        ConcurrentHashMap<String, Set<ClientHandler>> activeRooms = sessionManager.getActiveRooms();
        List<RoomCredential> roomCredentials = sessionManager.getRoomCredentials();
        ConcurrentHashMap<String, Set<ClientHandler>> spyActiveRoom = spy(activeRooms);
        List<RoomCredential> spyRoomCredential = spy(roomCredentials);
        sessionManager.closeRoomWithoutSendingResponse(roomName);
        verify(spyActiveRoom, never()).remove(anyString());
        verify(spyRoomCredential, never()).remove(any());
    }


    @Test
    public void testCloseRoomWithoutSendingResponse_RoomCredsNotFound(){
        Set<ClientHandler> handlers = new HashSet<>();
        handlers.add(clientHandler);
        handlers.add(clientHandler1);
        when(clientHandler.getUsername()).thenReturn("TEST-USER-1");
        when(clientHandler1.getUsername()).thenReturn("TEST-USER-2");
        sessionManager.getActiveRooms().put("TEST-ROOM", handlers);
        SessionManager spySessionManager = spy(sessionManager);
        spySessionManager.closeRoomWithoutSendingResponse("TEST-ROOM");
        verify(spySessionManager).addClientHandlerToRoom("LOBBY", clientHandler);
        verify(spySessionManager).addClientHandlerToRoom("LOBBY", clientHandler1);
    }


    @Test
    public void testCloseRoomWithoutSendingResponse_RoomCredsFound(){
        Set<ClientHandler> handlers = new HashSet<>();
        handlers.add(clientHandler);
        handlers.add(clientHandler1);
        List<RoomCredential> creds = new ArrayList<>();
        creds.add(new RoomCredential("TEST-ROOM", "TEST-PASSWORD"));
        sessionManager.setRoomCredentials(creds);
        when(clientHandler.getUsername()).thenReturn("TEST-USER-1");
        when(clientHandler1.getUsername()).thenReturn("TEST-USER-2");
        sessionManager.getActiveRooms().put("TEST-ROOM", handlers);
        SessionManager spySessionManager = spy(sessionManager);
        spySessionManager.closeRoomWithoutSendingResponse("TEST-ROOM");
        verify(spySessionManager).addClientHandlerToRoom("LOBBY", clientHandler);
        verify(spySessionManager).addClientHandlerToRoom("LOBBY", clientHandler1);
        assertTrue(spySessionManager.getRoomCredentials().isEmpty());
    }


    @Test
    public void testAddBotToRoom_forEasyBot(){
        Set<ClientHandler> handlers = new HashSet<>();
        handlers.add(clientHandler);

        SessionManager spySessionManager = spy(sessionManager);

        spySessionManager.getActiveRooms().put("TEST-ROOM", handlers);
        spySessionManager.getRoomBots().put("TEST-ROOM", new ArrayList<>());

        spySessionManager.addBotToRoom("TEST-ROOM", "Easy");
        verify(spySessionManager, times(1)).broadcastRoomUpdate("TEST-ROOM");
    }

    @Test
    public void testAddBotToRoom_forHardBot(){
        Set<ClientHandler> handlers = new HashSet<>();
        handlers.add(clientHandler);

        SessionManager spySessionManager = spy(sessionManager);

        spySessionManager.getActiveRooms().put("TEST-ROOM", handlers);
        spySessionManager.getRoomBots().put("TEST-ROOM", new ArrayList<>());

        spySessionManager.addBotToRoom("TEST-ROOM", "Hard");
        verify(spySessionManager, times(1)).broadcastRoomUpdate("TEST-ROOM");
    }


    @Test
    public void testCreateParticipantsListIncludingBot(){
        Set<ClientHandler> handlers = new HashSet<>();
        handlers.add(clientHandler);
        List<String> bots = new ArrayList<>();
        bots.add("EasyBot-1");
        bots.add("HardBot-1");

        when(clientHandler.getUsername()).thenReturn("TEST-USER");

        SessionManager spySessionManager = spy(sessionManager);
        spySessionManager.getActiveRooms().put("TEST-ROOM", handlers);
        spySessionManager.getRoomBots().put("TEST-ROOM", bots);

        List<String> expectedResult = new ArrayList<>();
        expectedResult.add("EasyBot-1");
        expectedResult.add("HardBot-1");
        expectedResult.add("TEST-USER");

        List<String> actualResult = spySessionManager.createParticipantsListIncludingBots("TEST-ROOM");

        assertTrue(expectedResult.containsAll(actualResult));
        assertTrue(actualResult.containsAll(expectedResult));
    }


    @Test
    public void testRemoveBotsFromRoom(){
        List<String> bots = new ArrayList<>();
        bots.add("EasyBot-1");
        bots.add("HardBot-1");

        SessionManager spySessionManager = spy(sessionManager);
        spySessionManager.getActiveRooms().put("TEST-ROOM", new HashSet<>());

        spySessionManager.getRoomBots().put("TEST-ROOM", bots);
        assertEquals(2, spySessionManager.getRoomBots().get("TEST-ROOM").size());

        spySessionManager.removeBotsFromRoom("TEST-ROOM");
        assertEquals(0, spySessionManager.getRoomBots().get("TEST-ROOM").size());
        verify(spySessionManager, times(1)).broadcastRoomUpdate("TEST-ROOM");
    }


    @Test
    public void testBroadcastRoomUpdate(){
        SessionManager spySessionManager = spy(sessionManager);
        spySessionManager.getActiveRooms().put("TEST-ROOM", new HashSet<>());
        List<String> bots = new ArrayList<>();
        bots.add("EasyBot-1");
        spySessionManager.getRoomBots().put("TEST-ROOM", bots);
        spySessionManager.broadcastRoomUpdate("TEST-ROOM");
        verify(spySessionManager, times(1)).broadcastMessageInRoom(any(String.class), any(Message.class));
    }


    @Test
    public void testGetLoggedInClients(){
        when(clientHandler.getUsername()).thenReturn("TEST-USER");
        assertNotNull(sessionManager.getLoggedInClients());
        assertEquals(0,sessionManager.getLoggedInClients().size());
        sessionManager.addClientHandlerToRoom(roomName,clientHandler);
        assertEquals(1,sessionManager.getLoggedInClients().size());
        ConcurrentHashMap<String,ClientHandler> map = sessionManager.getLoggedInClients();
        assertEquals(clientHandler,map.get(clientHandler.getUsername()));
    }

    @Test
    public void testGetActiveRooms(){
        when(clientHandler.getUsername()).thenReturn("TEST-USER");
        assertNotNull(sessionManager.getActiveRooms());
        assertEquals(1,sessionManager.getActiveRooms().size());
        sessionManager.addClientHandlerToRoom(roomName,clientHandler);
        assertEquals(2,sessionManager.getActiveRooms().size());
        ConcurrentHashMap<String,Set<ClientHandler>> map = sessionManager.getActiveRooms();
        assertEquals(ConcurrentHashMap.newKeySet(),map.get("LOBBY"));
        Set<ClientHandler> set = new HashSet<>();
        set.add(clientHandler);
        assertEquals(set,map.get(roomName));
    }
}
