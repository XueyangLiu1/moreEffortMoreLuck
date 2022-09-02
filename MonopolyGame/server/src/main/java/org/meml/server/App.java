package org.meml.server;

public class App {
    static final int serverPort = 12345;

    public static void main( String[] args ) {
        Server server = new Server(serverPort);
        server.run();
    }
}
