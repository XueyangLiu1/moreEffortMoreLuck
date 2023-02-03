package org.meml.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.meml.shared.protocol.ClientServer;
import org.meml.shared.protocol.ServerClient;

import java.io.*;
import java.net.Socket;

public class ServerClientCommunicator {
    private final InputStream input;
    private final OutputStream output;

    private static final Logger logger = LogManager.getLogger(ServerClientCommunicator.class);

    public ServerClientCommunicator(Socket socket) throws IOException {
        try{
            this.input = socket.getInputStream();
            this.output = socket.getOutputStream();
            logger.info("Succeed to get input and output from socket connection");
        }catch(IOException e) {
            logger.error("Failed to get input and output from socket connection");
            throw e;
        }
    }

    /**
     * send a scMsg to the client
     *
     * @param scMsg the SCmsg to be sent
     */
    public void send(ServerClient.SCmsg scMsg){
        try{
            scMsg.writeDelimitedTo(output);
        }catch (IOException e) {
            logger.error("Fail to send SCmsg: " + scMsg);
        }
    }

    /**
     * read csMsg from the client
     *
     * @return the CSmsg
     */
    public ClientServer.CSmsg receive(){
        try{
            ClientServer.CSmsg csMsg =  ClientServer.CSmsg.parseDelimitedFrom(this.input);
            while (csMsg == null) {
                csMsg =  ClientServer.CSmsg.parseDelimitedFrom(this.input);
            }
            return csMsg;
        }catch(IOException e) {
            logger.error("Fail to receive csMsg");
        }
        return null;
    }
    
}
