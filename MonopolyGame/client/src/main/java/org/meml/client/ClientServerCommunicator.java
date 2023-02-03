package org.meml.client;

import java.io.*;
import java.net.Socket;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.meml.shared.protocol.ClientServer;
import org.meml.shared.protocol.ServerClient;

public class ClientServerCommunicator {
    private final InputStream input;
    private final OutputStream output;

    private static final Logger logger = LogManager.getLogger(ClientServerCommunicator.class);

    /**
     * The constructor of the ClientSocket
     */
    public ClientServerCommunicator(Socket socket) throws IOException {
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
     * send a csMsg to the server
     *
     * @param csMsg the string to be sent
     */
    public void send(ClientServer.CSmsg csMsg){
        try{
            csMsg.writeDelimitedTo(output);
        }catch (IOException e) {
            logger.error("Fail to send CSmsg: " + csMsg);
        }
    }

    /**
     * read scMsg from the server
     *
     * @return the scMsg
     */
    public ServerClient.SCmsg receive(){
        try{
            ServerClient.SCmsg scMsg =  ServerClient.SCmsg.parseDelimitedFrom(this.input);
            while (scMsg == null) {
                scMsg =  ServerClient.SCmsg.parseDelimitedFrom(this.input);
            }
            return scMsg;
        }catch(IOException e) {
            logger.error("Fail to receive scMsg");
        }
        return null;
    }
}
