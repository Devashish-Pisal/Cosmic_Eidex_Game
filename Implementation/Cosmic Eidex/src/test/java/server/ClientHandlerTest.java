package server;

import com.group06.cosmiceidex.bot.EasyBot;
import com.group06.cosmiceidex.bot.HardBot;
import com.group06.cosmiceidex.common.LeaderboardEntry;
import com.group06.cosmiceidex.common.Message;
import com.group06.cosmiceidex.common.RoomCredential;
import com.group06.cosmiceidex.common.User;
import com.group06.cosmiceidex.exceptions.AuthException;
import com.group06.cosmiceidex.exceptions.RoomDoesNotExists;
import com.group06.cosmiceidex.game.GameSession;
import com.group06.cosmiceidex.game.Player;
import com.group06.cosmiceidex.game.PlayerInterface;
import com.group06.cosmiceidex.server.ClientHandler;
import com.group06.cosmiceidex.server.DatabaseService;
import com.group06.cosmiceidex.server.Server;
import com.group06.cosmiceidex.server.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.lang.reflect.Field;
import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;



@ExtendWith(MockitoExtension.class)
class ClientHandlerTest {

    @Mock private Socket mockSocket;
    @Mock private ObjectOutputStream mockOut;
    @Mock private ObjectInputStream mockIn;
    @Mock private Server mockServer;
    @Mock private Message mockMessage;
    @Mock private SessionManager mockSessionManager;
    @Mock private DatabaseService mockDatabaseService;
    @Mock private ClientHandler clientHandler1;
    @Mock private ClientHandler clientHandler2;
    @Mock private ScheduledExecutorService mockBotExecutor;
    @InjectMocks private ClientHandler clientHandler;

    private ArgumentCaptor<Message> messageCaptor;

    @BeforeEach
    void setUp() throws IOException, NoSuchFieldException, IllegalAccessException {
        messageCaptor = ArgumentCaptor.forClass(Message.class);
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ByteArrayInputStream byteIn = new ByteArrayInputStream(new byte[0]);
        when(mockSocket.getOutputStream()).thenReturn(byteOut);
        when(mockSocket.getInputStream()).thenReturn(byteIn);

        mockSessionManager = mock(SessionManager.class);
        Field sessionManagerInstanceField = SessionManager.class.getDeclaredField("sessionManagerInstance");
        sessionManagerInstanceField.setAccessible(true);
        sessionManagerInstanceField.set(null, mockSessionManager);

        mockDatabaseService = mock(DatabaseService.class);
        Field instanceField = DatabaseService.class.getDeclaredField("databaseService");
        instanceField.setAccessible(true);
        instanceField.set(null, mockDatabaseService);

        clientHandler = new ClientHandler(mockSocket, mockServer);
        clientHandler.setMockParameters(mockOut, mockIn);
        clientHandler.setUsername("testUser");
    }

    @Test
    void testHandleClientMessage_LOGIN_REQUEST() {
        when(mockMessage.getType()).thenReturn(Message.MessageType.LOGIN_REQUEST);
        User user = new User("testUser", "testPassword");
        when(mockMessage.getPayload()).thenReturn(user);

        ClientHandler spyClientHandler = spy(clientHandler);
        spyClientHandler.handleClientMessage(mockMessage);

        verify(spyClientHandler, times(1)).handleAuthenticationMessage(mockMessage);
        verify(spyClientHandler, never()).handleLobbyManagementMessage(any());
        verify(spyClientHandler, never()).handleRoomManagementMessage(any());
        verify(spyClientHandler, never()).handleGameLogicMessage(any());
    }

    @Test
    void testHandleClientMessage_LOBBY_CHAT_REQUEST() {
        when(mockMessage.getType()).thenReturn(Message.MessageType.LOBBY_CHAT_REQUEST);
        when(mockMessage.getSender()).thenReturn("testUser");
        when(mockMessage.getPayload()).thenReturn("testMessage");

        ClientHandler spyClientHandler = spy(clientHandler);
        spyClientHandler.handleClientMessage(mockMessage);

        verify(spyClientHandler, never()).handleAuthenticationMessage(any());
        verify(spyClientHandler, times(1)).handleLobbyManagementMessage(mockMessage);
        verify(spyClientHandler, never()).handleRoomManagementMessage(any());
        verify(spyClientHandler, never()).handleGameLogicMessage(any());
    }

    @Test
    void testHandleClientMessage_JOIN_ROOM_REQUEST() {
        when(mockMessage.getType()).thenReturn(Message.MessageType.WAITING_ROOM_CHAT_REQUEST);
        when(mockMessage.getSender()).thenReturn("testUser");
        when(mockMessage.getTarget()).thenReturn("testRoom");
        when(mockMessage.getPayload()).thenReturn("testMessage");

        ClientHandler spyClientHandler = spy(clientHandler);
        spyClientHandler.handleClientMessage(mockMessage);

        verify(spyClientHandler, never()).handleAuthenticationMessage(any());
        verify(spyClientHandler, never()).handleLobbyManagementMessage(any());
        verify(spyClientHandler, times(1)).handleRoomManagementMessage(mockMessage);
        verify(spyClientHandler, never()).handleGameLogicMessage(any());
    }

    @Test
    void testHandleClientMessage_GAME_UPDATE() {
        when(mockMessage.getType()).thenReturn(Message.MessageType.PLAY_CARD);
        ClientHandler spyClientHandler = spy(clientHandler);
        spyClientHandler.handleClientMessage(mockMessage);
        verify(spyClientHandler, times(1)).handleGameLogicMessage(mockMessage);
    }




    @Test
    public void testHandleAuthenticationMessage_LOGIN_REQUEST_alreadyLoggedIn() throws SQLException {
        when(mockMessage.getType()).thenReturn(Message.MessageType.LOGIN_REQUEST);
        when(mockMessage.getPayload()).thenReturn(new User("testUser", "testPassword"));
        when(mockDatabaseService.checkPassword("testUser", "testPassword")).thenReturn(new User("testUser", "testPassword"));
        ConcurrentHashMap<String, ClientHandler> loggedIn = new ConcurrentHashMap<>();
        loggedIn.put("testUser", clientHandler);
        when(mockSessionManager.getLoggedInClients()).thenReturn(loggedIn);

        ClientHandler spyClientHandler = spy(clientHandler);
        spyClientHandler.handleAuthenticationMessage(mockMessage);

        verify(spyClientHandler, times(1)).sendMessageToClient(any());
        verify(spyClientHandler).sendMessageToClient(messageCaptor.capture());
        Message sentMessage = messageCaptor.getValue();
        assertEquals(Message.MessageType.LOGIN_ERROR, sentMessage.getType());
    }

    @Test
    public void testHandleAuthenticationMessage_LOGIN_REQUEST_SQLException() throws SQLException {
        when(mockMessage.getType()).thenReturn(Message.MessageType.LOGIN_REQUEST);
        when(mockMessage.getPayload()).thenReturn(new User("testUser", "testPassword"));
        when(mockDatabaseService.checkPassword("testUser", "testPassword")).thenThrow(new SQLException());

        ClientHandler spyClientHandler = spy(clientHandler);
        spyClientHandler.handleAuthenticationMessage(mockMessage);

        verify(spyClientHandler, times(1)).sendMessageToClient(any());
        verify(spyClientHandler).sendMessageToClient(messageCaptor.capture());
        Message sentMessage = messageCaptor.getValue();
        assertEquals(Message.MessageType.LOGIN_ERROR, sentMessage.getType());
    }

    @Test
    public void testHandleAuthenticationMessage_LOGIN_REQUEST_successfulLogin() throws SQLException {
        when(mockMessage.getType()).thenReturn(Message.MessageType.LOGIN_REQUEST);
        when(mockMessage.getPayload()).thenReturn(new User("testUser", "testPassword"));
        when(mockDatabaseService.checkPassword("testUser", "testPassword")).thenReturn(new User("testUser", "testPassword"));
        ConcurrentHashMap<String, ClientHandler> loggedIn = new ConcurrentHashMap<>();
        when(mockSessionManager.getLoggedInClients()).thenReturn(loggedIn);

        ClientHandler spyClientHandler = spy(clientHandler);
        spyClientHandler.handleAuthenticationMessage(mockMessage);

        verify(spyClientHandler, times(1)).sendMessageToClient(any());
        verify(spyClientHandler).sendMessageToClient(messageCaptor.capture());
        Message sentMessage = messageCaptor.getValue();
        assertEquals(Message.MessageType.LOGIN_RESPONSE, sentMessage.getType());
        assertEquals("testUser", spyClientHandler.getUsername());
        assertEquals("LOBBY", spyClientHandler.getCurrentRoom());
    }

    @Test
    public void testHandleAuthenticationMessage_LOGIN_REQUEST_wrongCredentials() throws SQLException {
        when(mockMessage.getType()).thenReturn(Message.MessageType.LOGIN_REQUEST);
        when(mockMessage.getPayload()).thenReturn(new User("testUser", "testPassword"));
        when(mockDatabaseService.checkPassword("testUser", "testPassword")).thenReturn(null);

        ClientHandler spyClientHandler = spy(clientHandler);
        spyClientHandler.handleAuthenticationMessage(mockMessage);

        verify(spyClientHandler, times(1)).sendMessageToClient(any());
        verify(spyClientHandler).sendMessageToClient(messageCaptor.capture());
        Message sentMessage = messageCaptor.getValue();
        assertEquals(Message.MessageType.LOGIN_ERROR, sentMessage.getType());
    }

    @Test
    public void testHandleAuthenticationMessage_REGISTER_REQUEST_Success() {
        when(mockMessage.getType()).thenReturn(Message.MessageType.REGISTER_REQUEST);
        when(mockMessage.getPayload()).thenReturn(new User("testUser", "testPassword"));

        ClientHandler spyClientHandler = spy(clientHandler);
        spyClientHandler.handleAuthenticationMessage(mockMessage);

        verify(spyClientHandler, times(1)).sendMessageToClient(any());
        verify(spyClientHandler).sendMessageToClient(messageCaptor.capture());
        Message sentMessage = messageCaptor.getValue();
        assertEquals(Message.MessageType.REGISTER_RESPONSE, sentMessage.getType());
    }

    @Test
    public void testHandleAuthenticationMessage_REGISTER_REQUEST_AuthException() {
        when(mockMessage.getType()).thenReturn(Message.MessageType.REGISTER_REQUEST);
        User user = new User("testUser", "testPassword");
        User spyUser = spy(user);
        when(mockMessage.getPayload()).thenReturn(spyUser);
        when(spyUser.getUsername()).thenThrow(AuthException.class);

        ClientHandler spyClientHandler = spy(clientHandler);
        spyClientHandler.handleAuthenticationMessage(mockMessage);

        verify(spyClientHandler, times(1)).sendMessageToClient(any());
        verify(spyClientHandler).sendMessageToClient(messageCaptor.capture());
        Message sentMessage = messageCaptor.getValue();
        assertEquals(Message.MessageType.REGISTER_ERROR, sentMessage.getType());
    }

    @Test
    public void testLobbyManagementMessage_LOBBY_CHAT_REQUEST() {
        when(mockMessage.getType()).thenReturn(Message.MessageType.LOBBY_CHAT_REQUEST);
        when(mockMessage.getSender()).thenReturn("testUser");
        when(mockMessage.getPayload()).thenReturn("testMessage");

        ClientHandler spyClientHandler = spy(clientHandler);
        spyClientHandler.handleLobbyManagementMessage(mockMessage);

        verify(mockSessionManager, times(1)).broadcastMessageInRoom(any(String.class), any(Message.class));
        verify(mockSessionManager).broadcastMessageInRoom(any(),messageCaptor.capture());
        Message sentMessage = messageCaptor.getValue();
        assertEquals(Message.MessageType.LOBBY_CHAT_RESPONSE, sentMessage.getType());
    }


    @Test
    public void testLobbyManagementMessage_LOGOUT_REQUEST(){
        when(mockMessage.getType()).thenReturn(Message.MessageType.LOGOUT_REQUEST);
        when(mockMessage.getSender()).thenReturn("testUser");

        ClientHandler spyClientHandler = spy(clientHandler);
        spyClientHandler.handleLobbyManagementMessage(mockMessage);

        verify(spyClientHandler, times(1)).sendMessageToClient(any());
        verify(spyClientHandler).sendMessageToClient(messageCaptor.capture());
        Message sentMessage = messageCaptor.getValue();
        assertEquals(Message.MessageType.LOGOUT_RESPONSE, sentMessage.getType());
        assertEquals("LOBBY", spyClientHandler.getCurrentRoom());
        assertNull(spyClientHandler.getUsername());
    }

    @Test
    public void testLobbyManagementMessage_LEADERBOARD_DATA_REQUEST() throws NoSuchFieldException, IllegalAccessException {
        when(mockMessage.getType()).thenReturn(Message.MessageType.LEADERBOARD_DATA_REQUEST);
        when(mockMessage.getSender()).thenReturn("testUser");

        List<LeaderboardEntry> board = new ArrayList<>();
        LeaderboardEntry entry = new LeaderboardEntry("testUser", 1,2,3);
        board.add(entry);
        when(mockDatabaseService.getTopTen()).thenReturn(board);

        ClientHandler spyClientHandler = spy(clientHandler);
        spyClientHandler.handleLobbyManagementMessage(mockMessage);

        verify(spyClientHandler, times(1)).sendMessageToClient(any());
        verify(spyClientHandler).sendMessageToClient(messageCaptor.capture());
        Message sentMessage = messageCaptor.getValue();
        assertEquals(Message.MessageType.LEADERBOARD_DATA_RESPONSE, sentMessage.getType());
    }

    @Test
    public void testLobbyManagementMessage_COMPLETE_LEADERBOARD_REQUEST() throws NoSuchFieldException, IllegalAccessException {
        when(mockMessage.getType()).thenReturn(Message.MessageType.COMPLETE_LEADERBOARD_REQUEST);
        when(mockMessage.getSender()).thenReturn("testUser");

        List<LeaderboardEntry> board = new ArrayList<>();
        LeaderboardEntry entry = new LeaderboardEntry("testUser", 1,2,3);
        board.add(entry);
        when(mockDatabaseService.getFullLeaderboard()).thenReturn(board);

        ClientHandler spyClientHandler = spy(clientHandler);
        spyClientHandler.handleLobbyManagementMessage(mockMessage);

        verify(spyClientHandler, times(1)).sendMessageToClient(any());
        verify(spyClientHandler).sendMessageToClient(messageCaptor.capture());
        Message sentMessage = messageCaptor.getValue();
        assertEquals(Message.MessageType.COMPLETE_LEADERBOARD_RESPONSE, sentMessage.getType());
    }

    @Test
    public void testLobbyManagementMessage_ACTIVE_ROOMS_REQUEST(){
        when(mockMessage.getType()).thenReturn(Message.MessageType.ACTIVE_ROOMS_REQUEST);
        when(mockSessionManager.constructMapForActiveRooms()).thenReturn(new HashMap<>());

        ClientHandler spyClientHandler = spy(clientHandler);
        spyClientHandler.handleLobbyManagementMessage(mockMessage);

        verify(spyClientHandler, times(1)).sendMessageToClient(any());
        verify(spyClientHandler).sendMessageToClient(messageCaptor.capture());
        Message sentMessage = messageCaptor.getValue();
        assertEquals(Message.MessageType.ACTIVE_ROOMS_RESPONSE, sentMessage.getType());
        assertEquals("LOBBY", spyClientHandler.getCurrentRoom());
        assertNotNull(spyClientHandler.getUsername());
    }

    @Test
    public void testLobbyManagementMessage_CHANGE_PASSWORD_REQUEST_UserIdNotFound(){
        when(mockMessage.getType()).thenReturn(Message.MessageType.CHANGE_PASSWORD_REQUEST);
        when(mockDatabaseService.findIdOfUser(any())).thenReturn(-1);

        ClientHandler spyClientHandler = spy(clientHandler);
        spyClientHandler.handleLobbyManagementMessage(mockMessage);

        verify(spyClientHandler, times(1)).sendMessageToClient(any());
        verify(spyClientHandler).sendMessageToClient(messageCaptor.capture());
        Message sentMessage = messageCaptor.getValue();
        assertEquals(Message.MessageType.CHANGE_PASSWORD_ERROR, sentMessage.getType());
        assertEquals("LOBBY", spyClientHandler.getCurrentRoom());
    }

    @Test
    public void testLobbyManagementMessage_CHANGE_PASSWORD_REQUEST_UserIdFound(){
        when(mockMessage.getType()).thenReturn(Message.MessageType.CHANGE_PASSWORD_REQUEST);
        when(mockDatabaseService.findIdOfUser(any())).thenReturn(5);

        ClientHandler spyClientHandler = spy(clientHandler);
        spyClientHandler.handleLobbyManagementMessage(mockMessage);

        verify(spyClientHandler, times(1)).sendMessageToClient(any());
        verify(spyClientHandler).sendMessageToClient(messageCaptor.capture());
        Message sentMessage = messageCaptor.getValue();
        assertEquals(Message.MessageType.CHANGE_PASSWORD_RESPONSE, sentMessage.getType());
        assertEquals("LOBBY", spyClientHandler.getCurrentRoom());
    }


    @Test
    public void testHandleRoomManagementMessage_CREATE_ROOM_REQUEST_roomAlreadyExists(){
        when(mockMessage.getType()).thenReturn(Message.MessageType.CREATE_ROOM_REQUEST);
        when(mockMessage.getPayload()).thenReturn(new RoomCredential("testRoom", "testPassword"));
        ConcurrentHashMap<String, Set<ClientHandler>> activeRooms = new ConcurrentHashMap<String, Set<ClientHandler>>();
        activeRooms.put("testRoom", new HashSet<ClientHandler>());
        when(mockSessionManager.getActiveRooms()).thenReturn(activeRooms);

        ClientHandler spyClientHandler = spy(clientHandler);
        spyClientHandler.handleRoomManagementMessage(mockMessage);

        verify(spyClientHandler, times(1)).sendMessageToClient(any());
        verify(spyClientHandler).sendMessageToClient(messageCaptor.capture());
        Message sentMessage = messageCaptor.getValue();
        assertEquals(Message.MessageType.CREATE_ROOM_ERROR, sentMessage.getType());
    }

    @Test
    public void testHandleRoomManagementMessage_CREATE_ROOM_REQUEST_roomDoesNotExists()  {
        ConcurrentHashMap<String, Set<ClientHandler>> activeRooms = new ConcurrentHashMap<String, Set<ClientHandler>>();
        activeRooms.put("LOBBY", new HashSet<ClientHandler>());
        List<RoomCredential> list = new ArrayList<>();

        when(mockSessionManager.getActiveRooms()).thenReturn(activeRooms);
        when(mockMessage.getType()).thenReturn(Message.MessageType.CREATE_ROOM_REQUEST);
        when(mockSessionManager.getRoomCredentials()).thenReturn(list);
        when(mockMessage.getPayload()).thenReturn(new RoomCredential("testRoom", "testPassword"));

        ClientHandler spyClientHandler = spy(clientHandler);
        spyClientHandler.handleRoomManagementMessage(mockMessage);

        assertEquals("testRoom", spyClientHandler.getCurrentRoom());
        assertTrue(spyClientHandler.getIsGameMaster());
        verify(spyClientHandler, times(2)).sendMessageToClient(any());
    }


    @Test
    public void testHandleRoomManagementMessage_JOIN_ROOM_REQUEST_roomIsFull(){
        when(mockMessage.getType()).thenReturn(Message.MessageType.JOIN_ROOM_REQUEST);
        when(mockMessage.getPayload()).thenReturn(new RoomCredential("testRoom", "testPassword"));
        ConcurrentHashMap<String, Set<ClientHandler>> activeRooms = new ConcurrentHashMap<String, Set<ClientHandler>>();
        HashSet<ClientHandler> set = new HashSet<>();
        set.add(new ClientHandler(null, null));
        set.add(new ClientHandler(null, null));
        set.add(new ClientHandler(null, null));
        activeRooms.put("testRoom", set);
        ConcurrentHashMap<String, List<String>> bots = new ConcurrentHashMap<>();
        bots.put("testRoom", new ArrayList<>());

        when(mockSessionManager.getActiveRooms()).thenReturn(activeRooms);
        when(mockSessionManager.getRoomBots()).thenReturn(bots);
        ClientHandler spyClientHandler = spy(clientHandler);
        spyClientHandler.handleRoomManagementMessage(mockMessage);

        verify(spyClientHandler, times(1)).sendMessageToClient(any());
        verify(spyClientHandler).sendMessageToClient(messageCaptor.capture());
        Message sentMessage = messageCaptor.getValue();
        assertEquals(Message.MessageType.JOIN_ROOM_ERROR, sentMessage.getType());
    }

    @Test
    public void testHandleRoomManagementMessage_JOIN_ROOM_REQUEST_roomIsNotFullAndCorrectPassword(){
        ConcurrentHashMap<String, Set<ClientHandler>> activeRooms = new ConcurrentHashMap<String, Set<ClientHandler>>();
        HashSet<ClientHandler> set = new HashSet<>();
        set.add(new ClientHandler(null, null));
        set.add(new ClientHandler(null, null));
        activeRooms.put("testRoom", set);
        ConcurrentHashMap<String, List<String>> bots = new ConcurrentHashMap<>();
        bots.put("testRoom", new ArrayList<>());

        when(mockSessionManager.getRoomBots()).thenReturn(bots);
        when(mockMessage.getType()).thenReturn(Message.MessageType.JOIN_ROOM_REQUEST);
        when(mockMessage.getPayload()).thenReturn(new RoomCredential("testRoom", "testPassword"));
        when(mockSessionManager.getActiveRooms()).thenReturn(activeRooms);
        when(mockSessionManager.validateRoomCredentials(any())).thenReturn(true);
        when(mockSessionManager.removeClientHandlerFromRoom(any(), any())).thenReturn(clientHandler);

        ClientHandler spyClientHandler = spy(clientHandler);
        spyClientHandler.handleRoomManagementMessage(mockMessage);

        assertEquals("testRoom", spyClientHandler.getCurrentRoom());
        verify(spyClientHandler, times(1)).sendMessageToClient(messageCaptor.capture());
        Message sentMessage1 = messageCaptor.getValue();
        assertEquals(Message.MessageType.JOIN_ROOM_RESPONSE, sentMessage1.getType());
        verify(mockSessionManager, times(1)).broadcastRoomUpdate(any());
    }

    @Test
    public void testHandleRoomManagementMessage_JOIN_ROOM_REQUEST_roomIsNotFullAndWrongPassword(){
        ConcurrentHashMap<String, Set<ClientHandler>> activeRooms = new ConcurrentHashMap<String, Set<ClientHandler>>();
        HashSet<ClientHandler> set = new HashSet<>();
        set.add(new ClientHandler(null, null));
        set.add(new ClientHandler(null, null));
        activeRooms.put("testRoom", set);
        ConcurrentHashMap<String, List<String>> bots = new ConcurrentHashMap<>();
        bots.put("testRoom", new ArrayList<>());

        when(mockSessionManager.getRoomBots()).thenReturn(bots);
        when(mockMessage.getType()).thenReturn(Message.MessageType.JOIN_ROOM_REQUEST);
        when(mockMessage.getPayload()).thenReturn(new RoomCredential("testRoom", "testPassword"));
        when(mockSessionManager.getActiveRooms()).thenReturn(activeRooms);
        when(mockSessionManager.validateRoomCredentials(any())).thenReturn(false);

        ClientHandler spyClientHandler = spy(clientHandler);
        spyClientHandler.handleRoomManagementMessage(mockMessage);

        verify(spyClientHandler, times(1)).sendMessageToClient(messageCaptor.capture());
        Message sentMessage1 = messageCaptor.getValue();
        assertEquals(Message.MessageType.JOIN_ROOM_ERROR, sentMessage1.getType());
    }

    @Test
    public void testHandleRoomManagementMessage_JOIN_ROOM_REQUEST_RoomDoesNotExistsException(){
        ConcurrentHashMap<String, Set<ClientHandler>> activeRooms = new ConcurrentHashMap<String, Set<ClientHandler>>();
        HashSet<ClientHandler> set = new HashSet<>();
        set.add(new ClientHandler(null, null));
        set.add(new ClientHandler(null, null));
        activeRooms.put("testRoom", set);

        ConcurrentHashMap<String, List<String>> bots = new ConcurrentHashMap<>();
        bots.put("testRoom", new ArrayList<>());

        when(mockMessage.getType()).thenReturn(Message.MessageType.JOIN_ROOM_REQUEST);
        when(mockMessage.getPayload()).thenReturn(new RoomCredential("testRoom", "testPassword"));
        when(mockSessionManager.getActiveRooms()).thenReturn(activeRooms);
        when(mockSessionManager.getRoomBots()).thenReturn(bots);
        when(mockSessionManager.validateRoomCredentials(any())).thenThrow(new RoomDoesNotExists("error"));

        ClientHandler spyClientHandler = spy(clientHandler);
        spyClientHandler.handleRoomManagementMessage(mockMessage);

        verify(spyClientHandler, times(1)).sendMessageToClient(messageCaptor.capture());
        Message sentMessage1 = messageCaptor.getValue();
        assertEquals(Message.MessageType.JOIN_ROOM_ERROR, sentMessage1.getType());
    }


    @Test
    public void testHandlerRoomManagementMessage_WAITING_ROOM_CHAT_REQUEST(){
        when(mockMessage.getType()).thenReturn(Message.MessageType.WAITING_ROOM_CHAT_REQUEST);
        when(mockMessage.getPayload()).thenReturn("testMessage");
        when(mockMessage.getSender()).thenReturn("testUser");
        when(mockMessage.getTarget()).thenReturn("testRoom");

        ClientHandler spyClientHandler = spy(clientHandler);
        spyClientHandler.handleRoomManagementMessage(mockMessage);

        verify(mockSessionManager, times(1)).broadcastMessageInRoom(any(String.class), any(Message.class));
        verify(mockSessionManager).broadcastMessageInRoom(any(String.class), messageCaptor.capture());
        Message sentMessage = messageCaptor.getValue();
        assertEquals(Message.MessageType.WAITING_ROOM_CHAT_RESPONSE, sentMessage.getType());
    }


    @Test
    public void testHandlerRoomManagement_CLOSE_ROOM_REQUEST(){
        when(mockMessage.getType()).thenReturn(Message.MessageType.CLOSE_ROOM_REQUEST);
        when(mockMessage.getPayload()).thenReturn("testRoom");

        ClientHandler spyClientHandler = spy(clientHandler);
        spyClientHandler.setGameMaster(true);
        spyClientHandler.handleRoomManagementMessage(mockMessage);

        verify(mockSessionManager, times(1)).closeRoom(any(String.class));
    }

    @Test
    public void testHandlerRoomManagement_LEAVE_ROOM_REQUEST(){
        when(mockMessage.getType()).thenReturn(Message.MessageType.LEAVE_ROOM_REQUEST);
        when(mockMessage.getPayload()).thenReturn("testRoom");
        when(mockMessage.getSender()).thenReturn("testUser");
        when(mockSessionManager.removeClientHandlerFromRoom(any(String.class), any(String.class))).thenReturn(clientHandler);

        ClientHandler spyClientHandler = spy(clientHandler);
        spyClientHandler.setGameMaster(false);
        spyClientHandler.handleRoomManagementMessage(mockMessage);

        verify(mockSessionManager, times(1)).removeClientHandlerFromRoom(any(String.class), any(String.class));
        verify(mockSessionManager, times(1)).addClientHandlerToRoom(any(String.class), any(ClientHandler.class));
        assertEquals("LOBBY",clientHandler.getCurrentRoom());
    }


    @Test
    public void testHandlerRoomManagement_START_GAME_REQUEST(){
        when(mockMessage.getType()).thenReturn(Message.MessageType.START_GAME_REQUEST);

        List<String> players = new ArrayList<>();
        players.add("player-1");
        players.add("player-2");
        players.add("player-3");


        ClientHandler spyClientHandler = spy(clientHandler);

        when(mockSessionManager.createParticipantsListIncludingBots(any())).thenReturn(players);
        when(spyClientHandler.getUsername()).thenReturn("player-1");

        spyClientHandler.handleRoomManagementMessage(mockMessage);
        verify(spyClientHandler, times(1)).setupAndStartGameSession(mockMessage);
        verify(mockSessionManager, times(1)).broadcastMessageInRoom(any(String.class), any(Message.class));
    }


    @Test
    public void testHandlerRoomManagement_ADD_BOT(){
        when(mockMessage.getType()).thenReturn(Message.MessageType.ADD_BOT);
        when(mockMessage.getPayload()).thenReturn("testRoom");

        ClientHandler spyClientHandler = spy(clientHandler);
        when(spyClientHandler.getIsGameMaster()).thenReturn(true);

        spyClientHandler.handleRoomManagementMessage(mockMessage);
        verify(mockSessionManager, times(1)).addBotToRoom(any(String.class), any(String.class));
    }


    @Test
    public void testHandlerRoomManagement_REMOVE_BOT(){
        when(mockMessage.getType()).thenReturn(Message.MessageType.REMOVE_BOT);

        ClientHandler spyClientHandler = spy(clientHandler);
        when(spyClientHandler.getIsGameMaster()).thenReturn(true);

        spyClientHandler.handleRoomManagementMessage(mockMessage);
        verify(mockSessionManager, times(1)).removeBotsFromRoom(any(String.class));
    }


    @Test
    public void testHandlerRoomManagement_GAME_ROOM_CHAT_REQUEST(){
        when(mockMessage.getType()).thenReturn(Message.MessageType.GAME_ROOM_CHAT_REQUEST);
        when(mockMessage.getPayload()).thenReturn("chat data");
        when(mockMessage.getTarget()).thenReturn("test room");

        clientHandler.handleRoomManagementMessage(mockMessage);
        verify(mockSessionManager, times(1)).broadcastMessageInRoom(any(String.class), any(Message.class));
    }


    @Test
    public void testHandleGameLogicMessage_UPDATE_REQUEST_DuringGame() throws SQLException {
        when(mockMessage.getType()).thenReturn(Message.MessageType.UPDATE_REQUEST);
        when(mockMessage.getTarget()).thenReturn("test room");

        Player player1 = new Player("player1");
        Player player2 = new Player("player2");
        Player player3 = new Player("player3");
        GameSession session = new GameSession(player1, player2, player3);
        session.setGameState(GameSession.GameState.PLAYING_CARDS);
        when(mockMessage.getPayload()).thenReturn(session);

        ClientHandler spyClientHandler = spy(clientHandler);
        spyClientHandler.handleGameLogicMessage(mockMessage);
        verify(mockSessionManager, times(1)).broadcastMessageInRoom(any(String.class), any(Message.class));
        verify(spyClientHandler, times(1)).sendBotMoveMessages(any(GameSession.class), any(Message.class));

    }
    /*
    @Test
    public void testHandleGameLogicMessage_UPDATE_REQUEST_whenGameOver() throws SQLException {
        when(mockMessage.getType()).thenReturn(Message.MessageType.UPDATE_REQUEST);
        when(mockMessage.getTarget()).thenReturn("test room");

        Player player1 = new Player("player1");
        Player player2 = new Player("player2");
        Player player3 = new Player("player3");
        GameSession session = new GameSession(player1, player2, player3);
        HashMap<Integer, PlayerInterface> result = new HashMap<>();
        PlayerInterface playerI = new Player("player1");
        playerI.setNumberOfWonTricksDuringEntireGame(3);
        playerI.setTotalWonTrickPointDuringEntireGame(6);
        result.put(1, playerI);
        when(mockDatabaseService.findIdOfUser(playerI.getUsername())).thenReturn(5);
        when(mockDatabaseService.findWinCount(5)).thenReturn(10);
        session.setGameResult(result);
        session.setGameState(GameSession.GameState.GAME_OVER);

        when(mockMessage.getPayload()).thenReturn(session);
        clientHandler.handleGameLogicMessage(mockMessage);
        verify(mockDatabaseService, times(1)).upsertLeaderboard(5, 11, 3, 6);
        verify(mockSessionManager, times(1)).broadcastMessageInRoom(any(String.class), any(Message.class));
        verify(mockSessionManager, times(1)).closeRoomWithoutSendingResponse(any(String.class));
    }*/


    @Test
    public void testSetupAndStartGameSession_whenAllAreHumanPlayers(){
        List<String> players = new ArrayList<>();
        players.add("player-1");
        players.add("player-2");
        players.add("player-3");

        ClientHandler spyClientHandler = spy(clientHandler);

        when(mockSessionManager.createParticipantsListIncludingBots(any())).thenReturn(players);

        GameSession session = new GameSession(new Player("player-1"), new Player("player-2"), new Player("player-3"));
        GameSession session2 = spyClientHandler.setupAndStartGameSession(mockMessage);
        assertEquals(session.getUsernames(), session2.getUsernames());
    }

    @Test
    public void testSetupAndStartGameSession_IllegalStateException(){
        List<String> players = new ArrayList<>();
        players.add("player-1");
        players.add("player-2");

        ClientHandler spyClientHandler = spy(clientHandler);
        when(mockSessionManager.createParticipantsListIncludingBots(any())).thenReturn(players);

        assertThrows(IllegalStateException.class, ()->{
            spyClientHandler.setupAndStartGameSession(mockMessage);
        });
    }

    @Test
    public void testSendMessageToClient_Exception() throws IOException {
        clientHandler.setMockParameters(null, mockIn);
        Message msg = new Message(null, null, null);
        clientHandler.sendMessageToClient(msg);
        // passiert nichts, wenn Exception empfangen.
    }

    @Test
    public void testHandleClientHandlerDisconnect() throws IOException {
        Socket mockSocket = mock(Socket.class);
        ObjectOutputStream mockOut = mock(ObjectOutputStream.class);
        ObjectInputStream mockIn = mock(ObjectInputStream.class);

        ClientHandler handler = new ClientHandler(mockSocket, mock(Server.class));
        handler.setMockParameters(mockOut, mockIn);
        handler.handleClientHandlerDisconnect();

        verify(mockOut).close();
        verify(mockIn).close();
        verify(mockSocket).close();
    }

    @Test
    public void testHandlerClientHandlerDisconnect_Exception() throws IOException {
        clientHandler.setMockParameters(null, mockIn);
        clientHandler.handleClientHandlerDisconnect();
        // passiert nichts, wenn Exception empfangen.
    }

    @Test
    public void testGetUsername() {
        assertEquals("testUser", clientHandler.getUsername());
    }

    @Test
    public void testSetUsername() {
        clientHandler.setUsername("user");
        assertEquals("user", clientHandler.getUsername());
    }

    @Test
    public void testGetCurrentRoom() {
        assertEquals("LOBBY", clientHandler.getCurrentRoom());
    }

    @Test
    public void testSetCurrentRoom() {
        clientHandler.setCurrentRoom("TEST-ROOM");
        assertEquals("TEST-ROOM", clientHandler.getCurrentRoom());
    }

    @Test
    public void testIsGameMaster() {
        assertFalse(clientHandler.getIsGameMaster());
    }

    @Test
    public void testSetGameMaster() {
        clientHandler.setGameMaster(true);
        assertTrue(clientHandler.getIsGameMaster());
    }

    @Test
    public void testGetClientSocket(){
        assertNotNull(clientHandler.getClientSocket());
        assertEquals(mockSocket, clientHandler.getClientSocket());
    }

    @Test
    public void testGetIn(){
        assertNotNull(clientHandler.getIn());
        assertEquals(mockIn, clientHandler.getIn());
    }

    @Test
    public void testGetOut(){
        assertNotNull(clientHandler.getOut());
        assertEquals(mockOut, clientHandler.getOut());
    }

    @Test
    public void testSetMockParameter() throws IOException {
        assertNotNull(clientHandler.getOut());
        assertNotNull(clientHandler.getIn());
        clientHandler.setMockParameters(null, null);
        assertNull(clientHandler.getOut());
        assertNull(clientHandler.getIn());
    }
}