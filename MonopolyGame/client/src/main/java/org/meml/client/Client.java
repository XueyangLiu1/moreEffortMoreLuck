package org.meml.client;

import java.io.BufferedReader;
import java.io.PrintStream;

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

    public Client(PrintStream ps, BufferedReader br, ClientCommunicator clientSocket){
        this.out = ps;
        this.inputReader = br;
        this.Clientcommunicator = clientSocket;
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
