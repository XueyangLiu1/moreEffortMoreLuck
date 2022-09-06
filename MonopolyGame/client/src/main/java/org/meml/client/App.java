package org.meml.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class App {

    private static final Logger logger = LogManager.getLogger(App.class);

    public static void main( String[] args ) {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        String serverHostname = null;
        Integer serverListeningPort = null;
        try (InputStream config = App.class.getClassLoader().getResourceAsStream("config.properties")) {
            Properties prop = new Properties();
            //load a properties file from class path, inside static method
            prop.load(config);

            serverHostname = prop.getProperty("ServerHostname");
            serverListeningPort = Integer.valueOf(prop.getProperty("ServerListeningPort"));
        } catch (IOException ex) {
            logger.error("Fail to fetch server hostname and listening port from config file");
            return;
        }
        if (serverHostname == null || serverListeningPort == null) {
            logger.error("Server hostname and listening port config not found in config file");
            return;
        }
        Client client = new Client(System.out, input, serverHostname, serverListeningPort);
        client.send("ping");
        client.receive();
        client.outputCurrentMsg();
    }
}
