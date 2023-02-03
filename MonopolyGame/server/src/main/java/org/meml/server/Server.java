package org.meml.server;

import lombok.Data;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.meml.shared.protocol.ClientServer;
import org.meml.shared.protocol.ServerClient;

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
    public Server(int port){
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

    public void run(){
        while (true) {
            logger.info(String.format("Listening through port:%d",serverSocket.getLocalPort()));
            try{
                Socket socket = serverSocket.accept();
                ServerClientCommunicator serverClientCommunicator = new ServerClientCommunicator(socket);
                threadPool.execute(new ClientConnection(serverClientCommunicator, playerNames, creatorRoomMap, codeRoomMap));
                logger.info(String.format("Connected with client ip: %s",socket.getInetAddress().getHostName()));
            }catch(IOException e){
                logger.error("Fail to receive connection from client");
            }
        }
    }

    static class ClientConnection implements Runnable {
        ServerClientCommunicator serverClientCommunicator;
        final Set<String> playerNames;
        final Map<String, Room> creatorRoomMap;
        final Map<String, Room> codeRoomMap;
        boolean running;

        @Override
        public void run() {
            while (running) {
                ClientServer.CSmsg msg = serverClientCommunicator.receive();
                ServerClient.SCmsg.Builder replyBuilder = ServerClient.SCmsg.newBuilder();
                // Only IdCreateRequest, RoomCreateRequest, RoomJoinRequest, GameStartRequest
                // After all room members sent GameStartRequest, the game thread will be launched
                // TODO: DestroyRoomRequest to be added
                if (msg.getIdCreateRequestList().size() > 0) {
                    ClientServer.IdCreateRequest request = msg.getIdCreateRequest(0);
                    String playerName = request.getPlayerName();
                    if (playerNames.add(playerName)) {
                        serverClientCommunicator.send(replyBuilder.addIdCreateResponse(
                                ServerClient.IdCreateResponse.newBuilder()
                                        .setStatus(ServerClient.Status.SUCCESS)).build());
                    } else {
                        serverClientCommunicator.send(replyBuilder.addIdCreateResponse(
                                ServerClient.IdCreateResponse.newBuilder()
                                        .setStatus(ServerClient.Status.FAILURE)
                                        .setFailReason("Player name already exists")).build());
                    }
                } else if (msg.getRoomCreateRequestList().size() > 0) {
                    ClientServer.RoomCreateRequest request = msg.getRoomCreateRequest(0);
                    String playerName = request.getPlayerName();

                    // ensured by client procedure
                    assert(playerNames.contains(playerName));

                    if (creatorRoomMap.get(playerName) == null) {
                        String roomCode = RandomStringUtils.random(6, true, true);
                        Room room = new Room(playerName,serverClientCommunicator);
                        while (codeRoomMap.putIfAbsent(roomCode, room) != null) {
                            roomCode = RandomStringUtils.random(6, true, true);
                        }
                        room.setRoomCode(roomCode);
                        creatorRoomMap.put(playerName, room);
                        serverClientCommunicator.send(replyBuilder.addRoomCreateResponse(
                                ServerClient.RoomCreateResponse.newBuilder()
                                        .setStatus(ServerClient.Status.SUCCESS)
                                        .setRoomCode(roomCode)).build());
                    } else {
                        serverClientCommunicator.send(replyBuilder.addRoomCreateResponse(
                                ServerClient.RoomCreateResponse.newBuilder()
                                        .setStatus(ServerClient.Status.FAILURE)
                                        .setFailReason("Room already exists")).build());
                    }
                } else if (msg.getRoomJoinRequestList().size() > 0) {
                    ClientServer.RoomJoinRequest request = msg.getRoomJoinRequest(0);
                    String roomCode = request.getRoomCode();
                    String playerName = request.getPlayerName();

                    // ensured by client procedure
                    assert(playerNames.contains(playerName));

                    Room roomToJoin = codeRoomMap.get(roomCode);
                    if (roomToJoin == null) {
                        serverClientCommunicator.send(replyBuilder.addRoomJoinResponse(
                                ServerClient.RoomJoinResponse.newBuilder()
                                        .setStatus(ServerClient.Status.FAILURE)
                                        .setFailReason("Room does not exist")).build());
                    } else if (roomToJoin.getPlayerNameIndexMap().containsKey(playerName)) {
                        serverClientCommunicator.send(replyBuilder.addRoomJoinResponse(
                                ServerClient.RoomJoinResponse.newBuilder()
                                        .setStatus(ServerClient.Status.SUCCESS)).build());
                    } else {
                        roomToJoin.addPlayer(playerName, serverClientCommunicator);
                        serverClientCommunicator.send(replyBuilder.addRoomJoinResponse(
                                ServerClient.RoomJoinResponse.newBuilder()
                                        .setStatus(ServerClient.Status.SUCCESS)).build());
                    }
                } else if (msg.getGameStartRequestList().size() > 0) {
                    ClientServer.GameStartRequest request = msg.getGameStartRequest(0);
                    String roomCode = request.getRoomCode();

                }
            }
        }

        public ClientConnection(
                ServerClientCommunicator serverClientCommunicator,
                Set<String> playerNames,
                Map<String, Room> creatorRoomMap,
                Map<String, Room> codeRoomMap) {
            this.serverClientCommunicator = serverClientCommunicator;
            this.playerNames = playerNames;
            this.creatorRoomMap = creatorRoomMap;
            this.codeRoomMap = codeRoomMap;
            running = true;
        }
    }

    @Data
    static class Room {
        private int playerIndex;
        private final Map<String, Integer> playerNameIndexMap;
        private final Map<String, ServerClientCommunicator> playerNameCommunicatorMap;
        private final Map<Integer, String> playerIndexNameMap;
        private String roomCode;

        public Room(String creatorName, ServerClientCommunicator serverCreatorCommunicator) {
            playerIndex = 0;
            playerNameIndexMap = new ConcurrentHashMap<>();
            playerIndexNameMap = new ConcurrentHashMap<>();
            playerNameCommunicatorMap = new ConcurrentHashMap<>();
            roomCode = "temp";
            addPlayer(creatorName, serverCreatorCommunicator);
        }

        public synchronized void addPlayer(String playerName, ServerClientCommunicator serverClientCommunicator) {
            playerNameIndexMap.put(playerName, playerIndex);
            playerIndexNameMap.put(playerIndex, playerName);
            playerIndex++;
            playerNameCommunicatorMap.put(playerName, serverClientCommunicator);
        }
    }
}
