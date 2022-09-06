package org.meml.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private final ClientCommunicator Clientcommunicator;
    private String currentMsg = null;

    private static final Logger logger = LogManager.getLogger(Client.class);

    public Client(PrintStream ps, BufferedReader br, String serverHostname, int serverListeningPort){
        this.out = ps;
        this.inputReader = br;
        Socket connection = initSocket(serverHostname, serverListeningPort);
        this.Clientcommunicator = new ClientCommunicator(connection);
    }

    /**
     * Initiate the socket and connect to the server
     * @param hostname server's hostname
     * @param port server's port number
     */
    private Socket initSocket(String hostname, int port) {
        try {
            Socket socket = new Socket(hostname, port);
            logger.info(String.format("Successfully connected to %s:%d",hostname,port));
            return socket;
        } catch (IOException e) {
            logger.error(String.format("Fail to connect to %s:%d",hostname,port));
        }
        return null;
    }

    public void send(String msg) {
        Clientcommunicator.send(msg);
    }

    public void receive() {
        currentMsg = Clientcommunicator.receive();
    }

    public void outputCurrentMsg(){
        out.println(currentMsg);
    }
}
