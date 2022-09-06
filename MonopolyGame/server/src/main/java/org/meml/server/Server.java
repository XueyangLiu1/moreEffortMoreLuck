package org.meml.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Hello world!
 *
 */
public class Server
{
    private int clientIndex = 0;
    private ServerSocket serverSocket;
    private final ExecutorService threadPool;

    private static final Logger logger = LogManager.getLogger(Server.class);
    public Server(int port){
        this.serverSocket = null;
        try{
            this.serverSocket = new ServerSocket(port);
        }catch(IOException e){
            logger.error("Fail to start server, fail to initiate a serverSocket");
        }
        this.threadPool = Executors.newCachedThreadPool();
        logger.info("Server successfully initiated");
    }

    public void run(){
        while (true) {
            logger.info(String.format("Listening through port:%d",this.serverSocket.getLocalPort()));
            try{
                Socket socket = this.serverSocket.accept();
                ServerCommunicator serverCommunicator = new ServerCommunicator(socket);
                // Create a new task to be handled by threadPool.
                clientIndex += 1;
                threadPool.execute(new RequestHandleTask(serverCommunicator, clientIndex));
                logger.info(String.format("Connected with client No.%d",clientIndex));
            }catch(IOException e){
                logger.error(String.format("Fail to receive connection from client No.%d",clientIndex+1));
            }
        }
    }

    class RequestHandleTask implements Runnable {
        ServerCommunicator serverCommunicator;
        final int clientIndex;
        boolean running;

        @Override
        public void run() {
            while (this.running) {
                String msg = serverCommunicator.nonBlockReceive();
                if (msg != null) {
                    switch(msg){
                        case "ping":
                            serverCommunicator.send(String.format("Hi, this is server, I can hear you. Your Index is %d",clientIndex));
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        RequestHandleTask(ServerCommunicator serverCommunicator, int clientIndex) {
            this.serverCommunicator = serverCommunicator;
            this.clientIndex = clientIndex;
            this.running = true;
        }
    }
}
