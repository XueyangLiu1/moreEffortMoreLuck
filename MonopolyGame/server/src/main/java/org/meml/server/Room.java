package org.meml.server;

import lombok.Data;
import org.meml.shared.protocol.ServerClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class Room {
    private int playerIndex;
    private final Map<String, Integer> playerNameIndexMap;
    private final Map<String, ServerClientCommunicator> playerNameCommunicatorMap;
    private final Map<Integer, String> playerIndexNameMap;
    private final Map<String, Boolean> playerReadyMap;
    private String roomCode;
    private final String roomOwnerName;

    public Room(String creatorName, ServerClientCommunicator serverCreatorCommunicator) {
        playerIndex = 0;
        playerNameIndexMap = new ConcurrentHashMap<>();
        playerIndexNameMap = new ConcurrentHashMap<>();
        playerNameCommunicatorMap = new ConcurrentHashMap<>();
        playerReadyMap = new ConcurrentHashMap<>();
        roomCode = "temp";
        roomOwnerName = creatorName;
        addPlayer(creatorName, serverCreatorCommunicator);
    }

    public synchronized void addPlayer(String playerName, ServerClientCommunicator serverClientCommunicator) {
        playerNameIndexMap.put(playerName, playerIndex);
        playerIndexNameMap.put(playerIndex, playerName);
        playerIndex++;
        playerNameCommunicatorMap.put(playerName, serverClientCommunicator);
        playerReadyMap.put(playerName, false);
    }

    public synchronized void makePlayerReady(String playerName) {
        playerReadyMap.put(playerName, true);
    }

    public synchronized String getRoomStatus() {
        StringBuilder simpleRoomStatus = new StringBuilder("Room Status:\n");
        for (int i = 0; i < playerIndex; i++) {
            String currPlayerName = playerIndexNameMap.get(i);
            Boolean playerReady = playerReadyMap.get(currPlayerName);
            simpleRoomStatus.append(currPlayerName);
            simpleRoomStatus.append(playerReady ? "      Ready\n" : "  Not Ready\n");
        }

        return simpleRoomStatus.toString();
    }

    public synchronized boolean joinersAllReady() {
        for (String playerName : playerReadyMap.keySet()) {
            if (playerName.equals(roomOwnerName)) {
                continue;
            }
            if (playerReadyMap.get(playerName) == null || !playerReadyMap.get(playerName)) {
                return false;
            }
        }
        return true;
    }

    public synchronized void broadcastSCMsg(ServerClient.SCmsg sCmsg) {
        for (String playerName : playerNameCommunicatorMap.keySet()) {
            ServerClientCommunicator communicator = playerNameCommunicatorMap.get(playerName);
            communicator.send(sCmsg);
        }
    }
}
