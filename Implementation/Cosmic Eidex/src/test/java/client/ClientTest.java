package client;

import com.group06.cosmiceidex.client.Client;
import com.group06.cosmiceidex.common.Message;
import com.group06.cosmiceidex.controllers.LoginController;
import com.group06.cosmiceidex.controllers.RegistrierenController;
import com.group06.cosmiceidex.game.GameSession;
import com.group06.cosmiceidex.game.Player;
import com.group06.cosmiceidex.game.PlayerInterface;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.Socket;
import java.util.*;



import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ClientTest {

    @Mock
    private Socket mockSocket;
    @Mock
    private ObjectOutputStream mockOut;
    @Mock
    private ObjectInputStream mockIn;
    @Mock
    private Stage mockStage;
    @Mock
    private  LoginController mockLoginController;
    @Mock
    private RegistrierenController mockRegistrierenController;


    @InjectMocks
    private  Client client;

    @BeforeEach
    public void setUp() throws IOException {
        Client.resetInstance();
        client = Client.getClientInstance();
        client.setPrimaryStage(mockStage);
        client.setMockParameters(mockSocket, mockOut, mockIn);
        client.setSession(null);
        client.setLastMatchResult(null);
        LoginController.setMockLoginControllerInstance(mockLoginController);
        RegistrierenController.setMockRegistrierenControllerInstance(mockRegistrierenController);
    }

    @BeforeAll
    public static void setupPlatform() {
        Platform.startup(() -> {});
    }

    @AfterAll
    public static void tearDownPlatform() {
        Platform.exit();
    }

    @AfterEach
    public void makeInstanceNull(){
        Client.resetInstance();
        client = null;
        LoginController.setMockLoginControllerInstance(null);
        RegistrierenController.setMockRegistrierenControllerInstance(null);
        Mockito.reset(mockLoginController, mockRegistrierenController,
                mockSocket, mockOut, mockIn, mockStage);
    }

    @Test
    public void testConnectToServer_WhenSocketIsNull() throws IOException {
        client.setMockParameters(null, mockOut, mockIn);
        assertThrows(ConnectException.class , ()->{
            client.connectToServer("localhost", 1234, "testUser");
        });
    }

    @Test
    public void testConnectToServer_WhenSocketIsNotNull() throws IOException {
        client.connectToServer("localhost", 1234, "testUser");
        assertTrue(client.connectionWithServerEstablished);
        assertNotNull(client.getUsername());
        assertEquals("testUser", client.getUsername());
    }

    @Test
    public void testSendMessage_Success() throws IOException{
        Message testMessage = new Message(Message.MessageType.LOGIN_REQUEST, "testUser", "payload");
        client.sendMessage(testMessage);
        verify(mockOut).writeObject(testMessage);
        verify(mockOut).flush();
    }

    @Test
    public void testSendMessage_FailureNullPointerException() throws IOException {
        Message testMessage = new Message(Message.MessageType.LOGIN_REQUEST, "testUser", "payload");
        client.setMockParameters(mockSocket, null, mockIn);
        assertThrows(NullPointerException.class, ()->{
            client.sendMessage(testMessage);
        });
    }

    @Test
    public void testSendMessage_FailureIOException() throws IOException {
        Message testMessage = new Message(Message.MessageType.LOGIN_REQUEST, "testUser", "payload");
        doThrow(IOException.class).when(mockOut).writeObject(any());
        client.sendMessage(testMessage);
        verify(mockOut).writeObject(testMessage);
    }

    @Test
    public void testListenForMessages_Success() throws IOException, ClassNotFoundException {
        client.connectToServer("localhost", 1234, "testUser");
        when(mockIn.readObject())
                .thenReturn(new Message(Message.MessageType.LOBBY_CHAT_RESPONSE, "user", "hello"));
        client.setStopFlag(true);
        assertTrue(client.getStopFlag());
    }

    @Test
    public void testListenForMessages_FailureIOException() throws IOException, ClassNotFoundException, NoSuchMethodException {
        client.connectToServer("localhost", 1234, "testUser");
        client.setStopFlag(false);
        when(mockIn.readObject()).thenThrow(IOException.class);
        Method method = Client.class.getDeclaredMethod("listenForMessages");
        method.setAccessible(true);
        try {
            method.invoke(client);
        } catch (Exception e) {}
        assertFalse(client.getStopFlag());
    }

    @Test
    public void testHandleServerMessage_forHandleAuthenticationMessage_LOGIN_RESPONSE(){
        Message loginResponse = new Message(Message.MessageType.LOGIN_RESPONSE,
                "SERVER","testUser", "LOGGED-IN-SUCCESSFULLY");
        Client spyClient = spy(client);
        spyClient.handleServerMessage(loginResponse);
        assertEquals("testUser", spyClient.getUsername());
        verify(spyClient, times(1)).handleAuthenticationMessage(loginResponse);
    }

    @Test
    public void testHandleServerMessage_forHandleAuthenticationMessage_LOGIN_ERROR(){
        Message msg = new Message(Message.MessageType.LOGIN_ERROR,
                "SERVER","testUser", "error");
        Client spyClient = spy(client);
        spyClient.handleServerMessage(msg);
        verify(spyClient, times(1)).handleAuthenticationMessage(msg);
    }

    @Test
    public void testHandleServerMessage_forHandleAuthenticationMessage_REGISTER_ERROR(){
        Message msg = new Message(Message.MessageType.REGISTER_ERROR,
                "SERVER","testUser", "error");
        Client spyClient = spy(client);
        spyClient.handleServerMessage(msg);
        verify(spyClient, times(1)).handleAuthenticationMessage(msg);
    }

    @Test
    public void testHandleServerMessage_forHandleAuthenticationMessage_REGISTER_RESPONSE(){
        Message msg = new Message(Message.MessageType.REGISTER_RESPONSE,
                "SERVER","testUser", "success");
        Client spyClient = spy(client);
        spyClient.handleServerMessage(msg);
        verify(spyClient, times(1)).handleAuthenticationMessage(msg);
    }

    @Test
    public void testHandleServerMessage_forLobbyManagementMessage_LOBBY_CHAT_RESPONSE(){
        Message response = new Message(Message.MessageType.LOBBY_CHAT_RESPONSE,
                "SERVER","testUser", "testMessage");
        Client spyClient = spy(client);
        spyClient.handleServerMessage(response);
        verify(spyClient, times(1)).handleLobbyManagementMessage(response);
    }

    @Test
    public void testHandleServerMessage_forLobbyManagementMessage_LEADERBOARD_DATA_RESPONSE(){
        Message response = new Message(Message.MessageType.LEADERBOARD_DATA_RESPONSE,
                "SERVER","testUser", "data");
        Client spyClient = spy(client);
        spyClient.handleServerMessage(response);
        verify(spyClient, times(1)).handleLobbyManagementMessage(response);
    }

    @Test
    public void testHandleServerMessage_forLobbyManagementMessage_COMPLETE_LEADERBOARD_RESPONSE(){
        Message msg = new Message(Message.MessageType.COMPLETE_LEADERBOARD_RESPONSE,
                "SERVER","testUser", "data");
        Client spyClient = spy(client);
        spyClient.handleServerMessage(msg);
        verify(spyClient, times(1)).handleLobbyManagementMessage(msg);
    }

    @Test
    public void testHandleServerMessage_forLobbyManagementMessage_ACTIVE_ROOMS_RESPONSE(){
        Message msg = new Message(Message.MessageType.ACTIVE_ROOMS_RESPONSE,
                "SERVER","testUser", new HashMap<String,List<String>>());
        Message spyMessage = spy(msg);
        Client spyClient = spy(client);
        spyClient.setCurrentRoom("LOBBY");
        spyClient.handleServerMessage(spyMessage);
        verify(spyMessage, times(1)).getPayload();
        verify(spyClient, times(1)).handleLobbyManagementMessage(spyMessage);
    }

    @Test
    public void testHandleServerMessage_forLobbyManagementMessage_CHANGE_PASSWORD_ERROR(){
        Message msg = new Message(Message.MessageType.CHANGE_PASSWORD_ERROR,
                "SERVER","testUser", "data");
        Client spyClient = spy(client);
        spyClient.handleServerMessage(msg);
        verify(spyClient, times(1)).handleLobbyManagementMessage(msg);
    }

    @Test
    public void testHandleServerMessage_forLobbyManagementMessage_CHANGE_PASSWORD_RESPONSE(){
        Message msg = new Message(Message.MessageType.CHANGE_PASSWORD_RESPONSE,
                "SERVER","testUser", "data");
        Client spyClient = spy(client);
        spyClient.handleServerMessage(msg);
        verify(spyClient, times(1)).handleLobbyManagementMessage(msg);
    }


    @Test
    public void testHandleServerMessage_forHandleRoomManagementMessage_CREATE_ROOM_ERROR(){
        Message msg = new Message(Message.MessageType.CREATE_ROOM_ERROR,
                "SERVER","testUser", "error");
        Client spyClient = spy(client);
        spyClient.handleServerMessage(msg);
        verify(spyClient, times(1)).handleRoomManagementMessage(msg);
    }

    @Test
    public void testHandleServerMessage_forHandleRoomManagementMessage_CREATE_ROOM_RESPONSE(){
        Message response = new Message(Message.MessageType.CREATE_ROOM_RESPONSE,
                "SERVER","testUser", "ROOM-CREATED-SUCCESSFULLY");
        Client spyClient = spy(client);
        spyClient.handleServerMessage(response);
        verify(spyClient, times(1)).handleRoomManagementMessage(response);
    }

    @Test
    public void testHandleServerMessage_forHandleRoomManagementMessage_JOIN_ROOM_ERROR(){
        Message msg = new Message(Message.MessageType.JOIN_ROOM_ERROR,
                "SERVER","testUser", "error");
        Client spyClient = spy(client);
        spyClient.handleServerMessage(msg);
        verify(spyClient, times(1)).handleRoomManagementMessage(msg);
    }

    @Test
    public void testHandleServerMessage_forHandleRoomManagementMessage_JOIN_ROOM_RESPONSE(){
        Message msg = new Message(Message.MessageType.JOIN_ROOM_RESPONSE,
                "SERVER","testUser", "data");
        Client spyClient = spy(client);
        spyClient.handleServerMessage(msg);
        verify(spyClient, times(1)).handleRoomManagementMessage(msg);
    }

    @Test
    public void testHandleServerMessage_forHandleRoomManagementMessage_WAITING_ROOM_CHAT_RESPONSE(){
        Message msg = new Message(Message.MessageType.WAITING_ROOM_CHAT_RESPONSE,
                "SERVER","testUser", "data");
        Client spyClient = spy(client);
        spyClient.handleServerMessage(msg);
        verify(spyClient, times(1)).handleRoomManagementMessage(msg);
    }

    @Test
    public void testHandleServerMessage_forHandleRoomManagementMessage_LEAVE_ROOM_RESPONSE(){
        Message msg = new Message(Message.MessageType.LEAVE_ROOM_RESPONSE,
                "SERVER","testUser", "data");
        Client spyClient = spy(client);
        spyClient.handleServerMessage(msg);
        verify(spyClient, times(1)).handleRoomManagementMessage(msg);
    }

    @Test
    public void testHandleServerMessage_forHandleRoomManagementMessage_ROOM_PARTICIPANTS_RESPONSE(){
        Message msg = new Message(Message.MessageType.ROOM_PARTICIPANTS_RESPONSE,
                "SERVER","testUser", "data");
        Client spyClient = spy(client);
        spyClient.handleServerMessage(msg);
        verify(spyClient, times(1)).handleRoomManagementMessage(msg);
    }


    @Test
    public void testHandleServerMessage_forHandleRoomManagementMessage_START_GAME_RESPONSE(){
        PlayerInterface player1 = new Player("player1");
        PlayerInterface player2 = new Player("player2");
        PlayerInterface player3 = new Player("player3");
        Message msg = new Message(Message.MessageType.START_GAME_RESPONSE,
                "SERVER","testUser", new GameSession(player1, player2, player3));
        Client spyClient = spy(client);
        spyClient.handleServerMessage(msg);
        verify(spyClient, times(1)).handleRoomManagementMessage(msg);
    }


    @Test
    public void testHandleServerMessage_forHandleRoomManagementMessage_GAME_ROOM_CHAT_RESPONSE(){
        Message msg = new Message(Message.MessageType.GAME_ROOM_CHAT_RESPONSE,
                "SERVER","testUser", "data");
        Client spyClient = spy(client);
        spyClient.handleServerMessage(msg);
        verify(spyClient, times(1)).handleRoomManagementMessage(msg);
    }


    @Test
    public void testHandleServerMessage_forHandleGameLogicMessage_UPDATE_RESPONSE(){
        PlayerInterface player1 = new Player("player1");
        PlayerInterface player2 = new Player("player2");
        PlayerInterface player3 = new Player("player3");
        GameSession session = new GameSession(player1, player2, player3);

        Message response = new Message(Message.MessageType.UPDATE_RESPONSE,
                "SERVER","testUser", session);
        Client spyClient = spy(client);
        spyClient.handleServerMessage(response);
        verify(spyClient, times(1)).handleGameLogicMessage(response);
    }


    @Test
    public void testHandleServerMessage_forHandleGameLogicMessage_INVALID_MOVE(){
        Message response = new Message(Message.MessageType.INVALID_MOVE,
                "SERVER","testUser", "Herz9");
        Client spyClient = spy(client);
        spyClient.handleServerMessage(response);
        verify(spyClient, times(1)).handleGameLogicMessage(response);
    }


    @Test
    public void testHandleAuthenticationMessageFor_LOGIN_RESPONSE(){
        Message loginResponse = new Message(Message.MessageType.LOGIN_RESPONSE,
                "SERVER","testUser", "LOGGED-IN-SUCCESSFULLY");
        client.handleAuthenticationMessage(loginResponse);
        assertEquals("testUser", client.getUsername());
    }

    /*
    // only one test passing
    /*@Test
    public void testHandleAuthenticationMessageFor_REGISTER_ERROR() throws InterruptedException {
        Message error = new Message(Message.MessageType.REGISTER_ERROR,
                "SERVER","testUser", "ERROR-MESSAGE");
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(mockRegistrierenController).showError("Registrierung Fehler", (String) error.getPayload());

        Platform.runLater(()->{
            client.handleAuthenticationMessage(error);
        });
        latch.await(1, TimeUnit.SECONDS);

        verify(mockRegistrierenController).showError("Registrierung Fehler", (String) error.getPayload());
    }*/


    @Test
    public void testHandleLobbyManagementMessage(){
        Message loginResponse = new Message(Message.MessageType.LOGOUT_RESPONSE,
                "SERVER","testUser", "LOG-OUT-SUCCESSFUL");
        client.handleLobbyManagementMessage(loginResponse);
        assertNull(client.getUsername());
        assertEquals("LOBBY", client.getCurrentRoom());
        assertFalse(client.connectionWithServerEstablished);
    }


    @Test
    public void testHandleRoomManagementMessageFor_CREATE_ROOM_RESPONSE(){
        Message msg = new Message(Message.MessageType.CREATE_ROOM_RESPONSE, "SERVER",
                "testUser", "testRoom");
        client.handleRoomManagementMessage(msg);
        assertEquals("testRoom", client.getCurrentRoom());
        assertTrue(client.getIsGameMaster());
    }

    @Test
    public void testHandleRoomManagementMessageFor_JOIN_ROOM_RESPONSE(){
        Message msg = new Message(Message.MessageType.JOIN_ROOM_RESPONSE, "SERVER",
                "testUser", "testRoom");
        client.handleRoomManagementMessage(msg);
        assertEquals("testRoom", client.getCurrentRoom());
        assertFalse(client.getIsGameMaster());
    }

    @Test
    public void testHandleRoomManagementMessageFor_LEAVE_ROOM_RESPONSE(){
        Message msg = new Message(Message.MessageType.LEAVE_ROOM_RESPONSE, "SERVER",
                "testUser", "testRoom");
        client.handleRoomManagementMessage(msg);
        assertEquals("LOBBY", client.getCurrentRoom());
        assertFalse(client.getIsGameMaster());
    }


    @Test
    public void testHandleGameLogicMessage_UPDATE_RESPONSE(){
        PlayerInterface player1 = new Player("player1");
        PlayerInterface player2 = new Player("player2");
        PlayerInterface player3 = new Player("player3");
        GameSession session = new GameSession(player1, player2, player3);
        session.setGameState(GameSession.GameState.GAME_OVER);

        Message response = new Message(Message.MessageType.UPDATE_RESPONSE,
                "SERVER","testUser", session);
        Client spyClient = spy(client);
        spyClient.handleGameLogicMessage(response);
        assertEquals(session, spyClient.getSession());
        assertEquals("LOBBY", spyClient.getCurrentRoom());
        assertFalse(spyClient.getIsGameMaster());
    }



    @Test
    public void testHandleDisconnect_Success(){
        client.handleDisconnect();
        assertTrue(client.getStopFlag());
    }

    @Test
    public void testHandleDisconnect_IOException() throws IOException {
        doThrow(IOException.class).when(mockSocket).close();
        client.handleDisconnect();
        verify(mockSocket).close();
    }

    @Test
    public void testSetPrimaryStage(){
        client.setPrimaryStage(mockStage);
        assertEquals(mockStage, client.getPrimaryStage());
        verify(mockStage, times(3)).setOnCloseRequest(any());
    }


    @Test
    public void testGetClientInstance() {
        assertNotNull(client);
        assertSame(client, Client.getClientInstance());
    }

    @Test
    public void testGetUsername() {
        client.setUsername("testUser");
        assertNotNull(client.getUsername());
        assertEquals("testUser", client.getUsername());
    }

    @Test
    public void testSetUsername() {
        client.setUsername("newUser");
        assertEquals("newUser", client.getUsername());
    }

    @Test
    public void testGetCurrentRoom() {
        assertEquals("LOBBY", client.getCurrentRoom());
    }

    @Test
    public void testSetCurrentRoom() {
        client.setCurrentRoom("testRoom");
        assertEquals("testRoom", client.getCurrentRoom());
    }

    @Test
    public void testIsGameMaster() {
        assertFalse(client.getIsGameMaster());
    }

    @Test
    public void testSetGameMaster() {
        client.setGameMaster(true);
        assertTrue(client.getIsGameMaster());
        client.setGameMaster(false);
    }

    @Test
    public void testGetServerIP() {
        String expectedIP = "192.168.1.100";
        client.setServerIP(expectedIP);
        String actualIP = client.getServerIP();
        assertEquals(expectedIP, actualIP);
    }

    @Test
    public void testSetServerIP() {
        String testIP = "localhost";
        client.setServerIP(testIP);
        assertEquals(testIP, client.getServerIP());
    }

    @Test
    public void testGetServerPort() {
        int expectedPort = 8080;
        client.setServerPort(expectedPort);
        int actualPort = client.getServerPort();
        assertEquals(expectedPort, actualPort);
    }

    @Test
    public void testSetServerPort() {
        int testPort = 12345;
        client.setServerPort(testPort);
        assertEquals(testPort, client.getServerPort());
    }

    @Test
    public void testGetSession(){
        PlayerInterface player1 = new Player("player1");
        PlayerInterface player2 = new Player("player2");
        PlayerInterface player3 = new Player("player3");
        GameSession session = new GameSession(player1, player2, player3);
        client.setSession(session);
        assertEquals(session, client.getSession());
    }

    @Test
    public void testSetSession(){
        assertNull(client.getSession());
        PlayerInterface player1 = new Player("player1");
        PlayerInterface player2 = new Player("player2");
        PlayerInterface player3 = new Player("player3");
        GameSession session = new GameSession(player1, player2, player3);
        client.setSession(session);
        assertNotNull(client.getSession());
    }

    @Test
    public void testGetLastMatchResult(){
        HashMap<Integer, PlayerInterface> result = new HashMap<>();
        assertNull(client.getLastMatchResult());
        client.setLastMatchResult(result);
        assertNotNull(client.getLastMatchResult());
        assertEquals(result, client.getLastMatchResult());
    }

    @Test
    public void testSetLastMatchResult(){
        assertNull(client.getLastMatchResult());
        HashMap<Integer, PlayerInterface> result = new HashMap<>();
        client.setLastMatchResult(result);
        assertNotNull(client.getLastMatchResult());
        assertEquals(result, client.getLastMatchResult());
    }
}
