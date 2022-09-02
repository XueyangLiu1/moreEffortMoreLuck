package org.meml.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class App {
    static final String serverHostname = "localhost";
    static final int serverPort = 12345;

    public static void main( String[] args ) {
        ClientCommunicator socketClient = new ClientCommunicator(serverHostname, serverPort);
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        Client client = new Client(System.out, input, socketClient);
        client.send("ping");
        client.receive();
        client.outputCurrentMsg();
    }
}
