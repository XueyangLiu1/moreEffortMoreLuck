package org.meml.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.meml.shared.protocol.ClientServer;
import org.meml.shared.protocol.ServerClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

/**
 * Hello world!
 *
 */
public class Client
{
    private final PrintStream out;
    private final BufferedReader inputReader;
    String serverHostname;
    int serverListeningPort;
    private ClientServerCommunicator clientServerCommunicator;
    private String playerName;
    private String roomCode;
    private String roomOwnerName;
    private String currentInput;
    private ServerClient.SCmsg currentMsg;

    private static final Logger logger = LogManager.getLogger(Client.class);

    public Client(PrintStream ps, BufferedReader br, String serverHostname, int serverListeningPort){
        this.out = ps;
        this.inputReader = br;
        this.serverHostname = serverHostname;
        this.serverListeningPort = serverListeningPort;
        this.clientServerCommunicator = null;
        this.playerName = null;
        this.roomCode = null;
        this.currentInput = null;
        this.currentMsg = null;
    }

    public boolean isRoomOwner(){
        return playerName != null && playerName.equals(roomOwnerName);
    }

    public void run() {
        logger.info(String.format("Try connect with server:%s through port:%d",serverHostname,serverListeningPort));
        try {
            Socket socket = new Socket(serverHostname, serverListeningPort);
            this.clientServerCommunicator = new ClientServerCommunicator(socket);
        } catch (Exception e) {
            logger.error("Fail to initiate clientServerCommunicator");
            return;
        }
        boolean creatingId = true;
        do {
            try{
                out.println("Input your Id:");
                currentInput = inputReader.readLine();
            }catch (IOException e) {
                logger.error(e);
            }

            clientServerCommunicator.send(ClientServer.CSmsg.newBuilder().addIdCreateRequest(
                    ClientServer.IdCreateRequest.newBuilder()
                            .setPlayerName(currentInput)).build());

            currentMsg = clientServerCommunicator.receive();
            if (currentMsg.getIdCreateResponseCount() > 0) {
                ServerClient.IdCreateResponse response = currentMsg.getIdCreateResponse(0);
                if (response.getStatus() == ServerClient.Status.SUCCESS) {
                    creatingId = false;
                    playerName = currentInput;
                } else {
                    logger.error(String.format("Create Id failed, reason:%s", response.getFailReason()));
                }
            }
        } while (creatingId);

        boolean notInRoom = true;
        do {
            try{
                out.println("Create Room or Join?");
                currentInput = inputReader.readLine();
            }catch (IOException e) {
                logger.error(e);
            }
            switch (currentInput) {
                case "create":
                    clientServerCommunicator.send(ClientServer.CSmsg.newBuilder().addRoomCreateRequest(
                            ClientServer.RoomCreateRequest.newBuilder()
                                    .setPlayerName(playerName)).build());
                    currentMsg = clientServerCommunicator.receive();
                    if (currentMsg.getRoomCreateResponseCount() > 0) {
                        ServerClient.RoomCreateResponse response = currentMsg.getRoomCreateResponse(0);
                        if (response.getStatus() == ServerClient.Status.SUCCESS) {
                            notInRoom = false;
                            roomCode = response.getRoomCode();
                            roomOwnerName = response.getRoomOwnerName();
                            out.println("Your room code:" + response.getRoomCode());
                        } else {
                            logger.error(String.format("Create Room failed, reason:%s", response.getFailReason()));
                        }
                    }
                    break;
                case "join":
                    try{
                        out.println("Room Code?");
                        currentInput = inputReader.readLine();
                    }catch (IOException e) {
                        logger.error(e);
                    }
                    clientServerCommunicator.send(ClientServer.CSmsg.newBuilder().addRoomJoinRequest(
                            ClientServer.RoomJoinRequest.newBuilder()
                                    .setPlayerName(playerName)
                                    .setRoomCode(currentInput)).build());
                    currentMsg = clientServerCommunicator.receive();
                    if (currentMsg.getRoomJoinResponseCount() > 0) {
                        ServerClient.RoomJoinResponse response = currentMsg.getRoomJoinResponse(0);
                        if (response.getStatus() == ServerClient.Status.SUCCESS) {
                            notInRoom = false;
                            roomCode = currentInput;
                            out.println("You've joined the room");
                        } else {
                            logger.error(String.format("Join Room failed, reason:%s", response.getFailReason()));
                            out.println("Join Room failed, reason:" + response.getFailReason());
                        }
                    }
                    break;
                default:
                    break;
            }
        } while (notInRoom);

        boolean gameStartRequestSent = false;
        while (!gameStartRequestSent) {
            try{
                out.println("Get Ready?");
                currentInput = inputReader.readLine();
            }catch (IOException e) {
                logger.error(e);
            }
            switch(currentInput) {
                case "yes":
                    if (isRoomOwner()) {
                        clientServerCommunicator.send(ClientServer.CSmsg.newBuilder().addOwnerStartGameRequest(
                                ClientServer.OwnerStartGameRequest.newBuilder()
                                        .setPlayerName(playerName)
                                        .setRoomCode(roomCode)).build());
                        currentMsg = clientServerCommunicator.receive();
                        assert(currentMsg.getOwnerStartGameResponseCount() > 0);
                        if (currentMsg.getOwnerStartGameResponse(0).getStatus() == ServerClient.Status.SUCCESS) {
                            out.println("Game will start!");
                            gameStartRequestSent = true;
                        } else {
                            out.println(currentMsg.getOwnerStartGameResponse(0).getFailReason());
                        }
                    } else {
                        clientServerCommunicator.send(ClientServer.CSmsg.newBuilder().addJoinerReadyRequest(
                                ClientServer.JoinerReadyRequest.newBuilder()
                                        .setPlayerName(playerName)
                                        .setRoomCode(roomCode)).build());
                        currentMsg = clientServerCommunicator.receive();
                        assert(currentMsg.getJoinerReadyResponseCount() > 0);
                        assert(currentMsg.getJoinerReadyResponse(0).getStatus() == ServerClient.Status.SUCCESS);
                        out.println("You are ready!");
                        gameStartRequestSent = true;
                    }
                    break;
                case "status":
                    clientServerCommunicator.send(ClientServer.CSmsg.newBuilder().addRoomStatusRequest(
                            ClientServer.RoomStatusRequest.newBuilder()
                                    .setPlayerName(playerName)
                                    .setRoomCode(roomCode)).build());
                    currentMsg = clientServerCommunicator.receive();
                    assert(currentMsg.getRoomStatusResponseCount() > 0);
                    out.println(currentMsg.getRoomStatusResponse(0).getRoomStatus());
                default:
                    break;
            }
        }

        while (true) {
            currentMsg = clientServerCommunicator.receive();
            if (currentMsg.getGameStartInformCount() > 0) {
                ServerClient.GameStartInform inform = currentMsg.getGameStartInform(0);
                out.println("Game started!");
            }
        }



//        boolean running = true;
//        while (running) {
//            try{
//                currentInput = inputReader.readLine();
//            }catch (IOException e) {
//                logger.error(e);
//            }
//
//        }
    }

    static class RoomStatusInformer implements Runnable {

        @Override
        public void run() {

        }
        public RoomStatusInformer() {

        }
    }

}
