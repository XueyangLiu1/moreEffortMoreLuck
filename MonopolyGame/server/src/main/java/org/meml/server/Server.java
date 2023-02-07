package org.meml.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Hello world!
 *
 */
public class Server
{
    private ServerSocket serverSocket;
    private final Set<String> playerNames;
    // waitingRooms: <OwnerName, Room>
    private final Map<String, Room> creatorRoomMap;
    // waitingRoomDetails: <RoomCode, Room>
    private final Map<String, Room> codeRoomMap;
    private final ExecutorService threadPool;

    private static final Logger logger = LogManager.getLogger(Server.class);
    public Server(int port) {
        serverSocket = null;
        try{
            serverSocket = new ServerSocket(port);
        }catch(IOException e){
            logger.error("Fail to start server, fail to initiate a serverSocket");
        }
        playerNames = Collections.synchronizedSet(new HashSet<>());
        creatorRoomMap = new ConcurrentHashMap<>();
        codeRoomMap = new ConcurrentHashMap<>();
        threadPool = Executors.newCachedThreadPool();
        logger.info("Server successfully initiated");
    }

    public void run() {
        while (true) {
            logger.info(String.format("Listening through port:%d",serverSocket.getLocalPort()));
            try{
                Socket socket = serverSocket.accept();
                ServerClientCommunicator serverClientCommunicator = new ServerClientCommunicator(socket);
                threadPool.execute(new PregameMessageHandler(serverClientCommunicator, playerNames, creatorRoomMap, codeRoomMap, threadPool));
                logger.info(String.format("Connected with client ip: %s",socket.getInetAddress().getHostName()));
            }catch(IOException e){
                logger.error("Fail to receive connection from client");
            }
        }
    }
}
