package org.meml.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class App {
    private static final Logger logger = LogManager.getLogger(App.class);

    public static void main( String[] args ) {
        Integer serverListeningPort;

        try (InputStream input = App.class.getClassLoader().getResourceAsStream("config.properties")) {
            Properties prop = new Properties();
            //load a properties file from class path, inside static method
            prop.load(input);

            serverListeningPort = Integer.valueOf(prop.getProperty("ServerListeningPort"));
        } catch (IOException ex) {
            logger.error("Fail to fetch server listening port from config file");
            return;
        }

        if (serverListeningPort == null) {
            logger.error("Server listening port config not found in config file");
            return;
        }

        Server server = new Server(serverListeningPort);
        server.run();
    }
}
