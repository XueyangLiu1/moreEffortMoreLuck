package org.meml.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.meml.shared.protocol.ServerClient;

public class Game implements Runnable{
    private final Room room;
    private boolean running;

    private static final Logger logger = LogManager.getLogger(Game.class);
    @Override
    public void run() {
        logger.info(String.format("Started a game for %s",room.allPlayerNames()));
        while(running){

        }
    }

    public Game(Room room) {
        this.room = room;
    }

}
