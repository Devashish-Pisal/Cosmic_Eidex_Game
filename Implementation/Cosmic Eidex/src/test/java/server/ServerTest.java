package server;

import com.group06.cosmiceidex.server.ClientHandler;
import com.group06.cosmiceidex.server.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServerTest {

    @Mock
    private Socket mockClientSocket;
    @Mock
    private NetworkInterface mockNetworkInterface;
    @Mock
    private InetAddress mockInetAddress;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        resetServerInstance();
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
        resetServerInstance();
    }

    private void resetServerInstance() {
        try {
            java.lang.reflect.Field field = Server.class.getDeclaredField("serverInstance");
            field.setAccessible(true);
            field.set(null, null);
        } catch (Exception e) {}
    }

    @Test
    void testGetServerInstance_CreatesSingletonInstance() {
        try (MockedConstruction<ServerSocket> serverSocketMock = mockConstruction(ServerSocket.class,
                (mock, context) -> {
                    // Mock the accept method to throw IOException immediately to exit the loop
                    when(mock.accept()).thenThrow(new IOException("Test exception"));
                });
             MockedStatic<NetworkInterface> networkInterfaceMock = mockStatic(NetworkInterface.class)) {
            setupNetworkInterfaceMock(networkInterfaceMock);
            Server instance1 = Server.getServerInstance();
            Server instance2 = Server.getServerInstance();
            assertNotNull(instance1);
            assertSame(instance1, instance2);
            assertEquals(1, serverSocketMock.constructed().size());
        }
    }

    @Test
    void testServerStartsOnCorrectPort() throws IOException {
        try (MockedConstruction<ServerSocket> serverSocketMock = mockConstruction(ServerSocket.class,
                (mock, context) -> {
                    assertEquals(1234, context.arguments().get(0));
                    when(mock.accept()).thenThrow(new IOException("Test exception"));
                });
             MockedStatic<NetworkInterface> networkInterfaceMock = mockStatic(NetworkInterface.class)) {
            setupNetworkInterfaceMock(networkInterfaceMock);
            Server.getServerInstance();
            assertEquals(1, serverSocketMock.constructed().size());
        }
    }

    @Test
    void testServerAcceptsClientConnections() throws IOException {
        try (MockedConstruction<ServerSocket> serverSocketMock = mockConstruction(ServerSocket.class,
                (mock, context) -> {
                    when(mock.accept())
                            .thenReturn(mockClientSocket)
                            .thenThrow(new IOException("Test exception to exit loop"));
                });
             MockedConstruction<ClientHandler> clientHandlerMock = mockConstruction(ClientHandler.class);
             MockedConstruction<Thread> threadMock = mockConstruction(Thread.class);
             MockedStatic<NetworkInterface> networkInterfaceMock = mockStatic(NetworkInterface.class)) {

            setupNetworkInterfaceMock(networkInterfaceMock);
            Server.getServerInstance();
            assertEquals(1, clientHandlerMock.constructed().size());
            assertEquals(1, threadMock.constructed().size());
            Thread constructedThread = threadMock.constructed().get(0);
            verify(constructedThread).start();
        }
    }

    @Test
    void testSetServerPrivateIpAddress_FindsCorrectIpAddress() throws SocketException {
        try (MockedConstruction<ServerSocket> serverSocketMock = mockConstruction(ServerSocket.class,
                (mock, context) -> {
                    when(mock.accept()).thenThrow(new IOException("Test exception"));
                });
             MockedStatic<NetworkInterface> networkInterfaceMock = mockStatic(NetworkInterface.class)) {
            InetAddress loopbackAddress = mock(InetAddress.class);
            InetAddress publicAddress = mock(InetAddress.class);
            InetAddress privateAddress = mock(InetAddress.class);

            when(loopbackAddress.isLoopbackAddress()).thenReturn(true);
            when(loopbackAddress.isSiteLocalAddress()).thenReturn(false);
            when(publicAddress.isLoopbackAddress()).thenReturn(false);
            when(publicAddress.isSiteLocalAddress()).thenReturn(false);
            when(privateAddress.isLoopbackAddress()).thenReturn(false);
            when(privateAddress.isSiteLocalAddress()).thenReturn(true);
            when(privateAddress.getHostAddress()).thenReturn("192.168.1.100");

            Vector<InetAddress> addresses = new Vector<>();
            addresses.add(loopbackAddress);
            addresses.add(publicAddress);
            addresses.add(privateAddress);

            when(mockNetworkInterface.getInetAddresses())
                    .thenReturn(addresses.elements());

            networkInterfaceMock.when(NetworkInterface::getNetworkInterfaces)
                    .thenReturn(Collections.enumeration(Arrays.asList(mockNetworkInterface)));
            Server.getServerInstance();

            verify(privateAddress).getHostAddress();
        }
    }

    @Test
    void testServerHandlesIoExceptionDuringStartup() throws IOException {
        try (MockedConstruction<ServerSocket> serverSocketMock = mockConstruction(ServerSocket.class,
                (mock, context) -> {
                    when(mock.accept()).thenThrow(new IOException("Port already in use"));
                });
             MockedStatic<NetworkInterface> networkInterfaceMock = mockStatic(NetworkInterface.class)) {

            setupNetworkInterfaceMock(networkInterfaceMock);
            assertDoesNotThrow(() -> Server.getServerInstance());
        }
    }

    @Test
    void testServerHandlesSocketExceptionDuringIpAddressResolution() throws SocketException {
        try (MockedConstruction<ServerSocket> serverSocketMock = mockConstruction(ServerSocket.class,
                (mock, context) -> {
                    when(mock.accept()).thenThrow(new IOException("Test exception"));
                });
             MockedStatic<NetworkInterface> networkInterfaceMock = mockStatic(NetworkInterface.class)) {
            networkInterfaceMock.when(NetworkInterface::getNetworkInterfaces)
                    .thenThrow(new SocketException("Network interface error"));
            assertDoesNotThrow(() -> Server.getServerInstance());
        }
    }

    @Test
    void testMainMethodStartsServer() {
        try (MockedStatic<Server> serverStaticMock = mockStatic(Server.class)) {
            Server mockServer = mock(Server.class);
            serverStaticMock.when(Server::getServerInstance).thenReturn(mockServer);
            serverStaticMock.when(() -> Server.main(any())).thenCallRealMethod();
            Server.main(new String[]{});

            serverStaticMock.verify(Server::getServerInstance);
        }
    }

    private void setupNetworkInterfaceMock(MockedStatic<NetworkInterface> networkInterfaceMock) {
        when(mockNetworkInterface.getInetAddresses())
                .thenReturn(Collections.enumeration(Arrays.asList(mockInetAddress)));
        when(mockInetAddress.isLoopbackAddress()).thenReturn(false);
        when(mockInetAddress.isSiteLocalAddress()).thenReturn(true);
        when(mockInetAddress.getHostAddress()).thenReturn("192.168.1.100");

        networkInterfaceMock.when(NetworkInterface::getNetworkInterfaces)
                .thenReturn(Collections.enumeration(Arrays.asList(mockNetworkInterface)));
    }
}
