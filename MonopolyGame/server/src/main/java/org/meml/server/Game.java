package org.meml.server;

import org.meml.shared.protocol.ServerClient;

public class Game implements Runnable{
    private final Room room;
    private boolean running;

    @Override
    public void run() {
        room.broadcastSCMsg(ServerClient.SCmsg.newBuilder().addGameStartInform(
                ServerClient.GameStartInform.newBuilder()).build());
        while(running){

        }
    }

    public Game(Room room) {
        this.room = room;
    }

}
