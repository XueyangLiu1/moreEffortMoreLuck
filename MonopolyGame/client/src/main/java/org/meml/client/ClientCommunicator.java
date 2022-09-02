package org.meml.client;

import java.io.*;
import java.net.Socket;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class ClientCommunicator {
    final String serverHostname;
    final int serverPort;
    private Socket socket;
    private BufferedReader br;
    private BufferedWriter bw;

    private static final Logger logger = LogManager.getLogger(ClientCommunicator.class);

    /**
     * The constructor of the ClientSocket
     */
    public ClientCommunicator(String hostname, int port){
        this.serverHostname = hostname;
        this.serverPort = port;
        initSocket(hostname, port);
    }

    /**
     * Initiate the socket and connect to the server
     * @param hostname server's hostname
     * @param port server's port number
     */
    private void initSocket(String hostname, int port) {
        try {
            this.socket = new Socket(hostname, port);
            this.bw = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
            this.br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            logger.info(String.format("Successfully connected to %s:%d",hostname,port));
        } catch (IOException e) {
            logger.error("Failed to connect to server.");
        }
    }

    /**
     * send a msg string to the server
     *
     * @param msg the string to be sent
     * @throws IOException
     */
    public void send(String msg){
        try{
            bw.write(msg + "\n");
            bw.flush();
        }catch (IOException e) {
            logger.error("Fail to send msg: " + msg);
        }

    }

    /**
     * read string msg from the server
     *
     * @return the msg
     * @throws IOException
     */
    public String receive(){
        try{
            String msg = br.readLine();
            while (msg == null) {
                msg = br.readLine();
            }
            return msg;
        }catch(IOException e) {
            logger.error("Fail to receive msg");
        }
        return null;
    }
}
