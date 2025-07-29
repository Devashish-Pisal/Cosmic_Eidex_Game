package com.group06.cosmiceidex.server;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;

/**
 * Klasse ist zuständig für auf fest gegebene Port Server zu starten.
 * @author Devashish Pisal
 */
public class Server {
    private static Server serverInstance;
    private static final int PORT = 1234;
    private ServerSocket serverSocket;
    private String serverIpAddress;

    /**
     * Klassen Konstruktor
     * startServer() Methode wird aufgerufen.
     * @author Devashish Pisal
     */
    private Server(){
        startServer();
    }

    /**
     * Initialisiert serverSocket mit fest gegebene PORT Nummer.
     * Akzeptiert ständig verbindungen vom Client und startet ClientHandler für jeden Client auf separate Thread,
     * um einzelne client Nachrichten zu verarbeiten.
     * @see ClientHandler
     * @author Devashish Pisal
     */
    public void startServer(){
        try{
            serverSocket = new ServerSocket(PORT);
            setServerPrivateIpAddress();
            System.out.println("[INFO] Server started on IP Address: " + serverIpAddress + " and PORT: " + PORT) ;

            while(true){
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.out.println("[ERROR] Unable to start server on PORT: " + PORT);
            e.printStackTrace();
        }
    }

    /**
     * Gibt einzige Instanz der Server-Klasse zurück.
     * Falls bisher noch keine Instanz existiert, wird eine neue Instanz erstellt.
     * @return Einzige Server-Instanz
     * @author Devashish Pisal
     */
    public static synchronized Server getServerInstance(){
        if(serverInstance == null){
            serverInstance = new Server();
        }
        return serverInstance;
    }

    /**
     * Sucht nach private IP-Adresse vom Server.
     * Falls gefunden, dann wird es zu serverIpAddress Attribute zugewiesen.
     * @throws SocketException Ausnahme wird geworfen, falls Problem mit dem Zugriff auf Socket Verbindung gab.
     * @author Devashish Pisal
     */
    private void setServerPrivateIpAddress() throws SocketException {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                InetAddress inetAddress = inetAddresses.nextElement();
                if (!inetAddress.isLoopbackAddress() && inetAddress.isSiteLocalAddress()) {
                    this.serverIpAddress = inetAddress.getHostAddress();
                }
            }
        }
    }

    /**
     * Eintrittspunkt, um Server zu starten.
     * @param args
     * @author Devashish Pisal
     */
    public static void main(String[] args){
        Server.getServerInstance().startServer();
    }
}
