package org.meml.server;

import org.apache.commons.lang3.RandomStringUtils;
import org.meml.shared.protocol.ClientServer;
import org.meml.shared.protocol.ServerClient;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class PregameMessageHandler implements Runnable{
    private final ServerClientCommunicator serverClientCommunicator;
    private final Set<String> playerNames;
    private final Map<String, Room> creatorRoomMap;
    private final Map<String, Room> codeRoomMap;
    private final ExecutorService threadPool;
    private boolean running;
    private Room currentRoom;

    @Override
    public void run() {
        while (running) {
            ClientServer.CSmsg msg = serverClientCommunicator.receive();
            // Only IdCreateRequest, RoomCreateRequest, RoomJoinRequest, GameStartRequest
            // After all room members sent GameStartRequest, the game thread will be launched
            // TODO: DestroyRoomRequest to be added
            if (msg.getIdCreateRequestCount() > 0) {
                ClientServer.IdCreateRequest request = msg.getIdCreateRequest(0);
                String playerName = request.getPlayerName();
                if (playerNames.add(playerName)) {
                    serverClientCommunicator.send(ServerClient.SCmsg.newBuilder().addIdCreateResponse(
                            ServerClient.IdCreateResponse.newBuilder()
                                    .setStatus(ServerClient.Status.SUCCESS)).build());
                } else {
                    serverClientCommunicator.send(ServerClient.SCmsg.newBuilder().addIdCreateResponse(
                            ServerClient.IdCreateResponse.newBuilder()
                                    .setStatus(ServerClient.Status.FAILURE)
                                    .setFailReason("Player name already exists")).build());
                }
            } else if (msg.getRoomCreateRequestCount() > 0) {
                ClientServer.RoomCreateRequest request = msg.getRoomCreateRequest(0);
                String playerName = request.getPlayerName();

                // ensured by client procedure
                assert (playerNames.contains(playerName));

                if (creatorRoomMap.get(playerName) == null) {
                    String roomCode = RandomStringUtils.random(6, true, true).toUpperCase();
                    Room room = new Room(playerName, serverClientCommunicator);
                    while (codeRoomMap.putIfAbsent(roomCode, room) != null) {
                        roomCode = RandomStringUtils.random(6, true, true).toUpperCase();
                    }
                    room.setRoomCode(roomCode);
                    creatorRoomMap.put(playerName, room);
                    currentRoom = room;
                    serverClientCommunicator.send(ServerClient.SCmsg.newBuilder().addRoomCreateResponse(
                            ServerClient.RoomCreateResponse.newBuilder()
                                    .setStatus(ServerClient.Status.SUCCESS)
                                    .setRoomCode(roomCode)
                                    .setRoomOwnerName(playerName)).build());
                } else {
                    serverClientCommunicator.send(ServerClient.SCmsg.newBuilder().addRoomCreateResponse(
                            ServerClient.RoomCreateResponse.newBuilder()
                                    .setStatus(ServerClient.Status.FAILURE)
                                    .setFailReason("Room already exists")).build());
                }
            } else if (msg.getRoomJoinRequestCount() > 0) {
                ClientServer.RoomJoinRequest request = msg.getRoomJoinRequest(0);
                String roomCode = request.getRoomCode();
                String playerName = request.getPlayerName();

                // ensured by client procedure
                assert (playerNames.contains(playerName));

                Room roomToJoin = codeRoomMap.get(roomCode);
                if (roomToJoin == null) {
                    serverClientCommunicator.send(ServerClient.SCmsg.newBuilder().addRoomJoinResponse(
                            ServerClient.RoomJoinResponse.newBuilder()
                                    .setStatus(ServerClient.Status.FAILURE)
                                    .setFailReason("Room does not exist")).build());
                } else if (roomToJoin.getPlayerNameIndexMap().containsKey(playerName)) {
                    serverClientCommunicator.send(ServerClient.SCmsg.newBuilder().addRoomJoinResponse(
                            ServerClient.RoomJoinResponse.newBuilder()
                                    .setStatus(ServerClient.Status.SUCCESS)
                                    .setRoomOwnerName(roomToJoin.getRoomOwnerName())).build());
                } else {
                    roomToJoin.addPlayer(playerName, serverClientCommunicator);
                    currentRoom = roomToJoin;
                    serverClientCommunicator.send(ServerClient.SCmsg.newBuilder().addRoomJoinResponse(
                            ServerClient.RoomJoinResponse.newBuilder()
                                    .setStatus(ServerClient.Status.SUCCESS)
                                    .setRoomOwnerName(roomToJoin.getRoomOwnerName())).build());
                }
            } else if (msg.getJoinerReadyRequestCount() > 0) {
                ClientServer.JoinerReadyRequest request = msg.getJoinerReadyRequest(0);
                String playerName = request.getPlayerName();
                Room room = currentRoom;

                // ensured by client procedure
                assert (room != null);
                assert (room.getPlayerNameIndexMap().containsKey(playerName));
                assert (!room.getRoomOwnerName().equals(playerName));

                room.makePlayerReady(playerName);
                serverClientCommunicator.send(ServerClient.SCmsg.newBuilder().addJoinerReadyResponse(
                        ServerClient.JoinerReadyResponse.newBuilder()
                                .setStatus(ServerClient.Status.SUCCESS)).build());
            } else if (msg.getOwnerStartGameRequestCount() > 0) {
                ClientServer.OwnerStartGameRequest request = msg.getOwnerStartGameRequest(0);
                String playerName = request.getPlayerName();
                Room room = currentRoom;

                // ensured by client procedure
                assert (room != null);
                assert (room.getPlayerNameIndexMap().containsKey(playerName));
                assert (room.getRoomOwnerName().equals(playerName));

                if (room.joinersAllReady()) {
                    room.makePlayerReady(playerName);
                    serverClientCommunicator.send(ServerClient.SCmsg.newBuilder().addOwnerStartGameResponse(
                            ServerClient.OwnerStartGameResponse.newBuilder()
                                    .setStatus(ServerClient.Status.SUCCESS)).build());
                    room.broadcastSCMsg(ServerClient.SCmsg.newBuilder().addGameStartInform(
                            ServerClient.GameStartInform.newBuilder()).build());
                } else {
                    serverClientCommunicator.send(ServerClient.SCmsg.newBuilder().addOwnerStartGameResponse(
                            ServerClient.OwnerStartGameResponse.newBuilder()
                                    .setStatus(ServerClient.Status.FAILURE)
                                    .setFailReason("Joiners are not all ready!")).build());
                }
            } else if (msg.getRoomStatusRequestCount() > 0) {
                ClientServer.RoomStatusRequest request = msg.getRoomStatusRequest(0);
                String playerName = request.getPlayerName();
                Room room = currentRoom;

                // ensured by client procedure
                assert (room != null);
                assert (room.getPlayerNameIndexMap().containsKey(playerName));

                serverClientCommunicator.send(ServerClient.SCmsg.newBuilder().addRoomStatusResponse(
                        ServerClient.RoomStatusResponse.newBuilder()
                                .setRoomStatus(room.getRoomStatus())).build());
            } else if (msg.getGameStartAckCount() > 0) {
                ClientServer.GameStartAck ack = msg.getGameStartAck(0);
                String playerName = ack.getPlayerName();
                if (currentRoom.isRoomOwner(playerName)) {
                    threadPool.execute(new Game(currentRoom));
                }
                running = false;
            }
        }
    }

    public PregameMessageHandler(
            ServerClientCommunicator serverClientCommunicator,
            Set<String> playerNames,
            Map<String, Room> creatorRoomMap,
            Map<String, Room> codeRoomMap,
            ExecutorService threadPool) {
        this.serverClientCommunicator = serverClientCommunicator;
        this.playerNames = playerNames;
        this.creatorRoomMap = creatorRoomMap;
        this.codeRoomMap = codeRoomMap;
        this.threadPool = threadPool;
        this.currentRoom = null;
        running = true;
    }
}