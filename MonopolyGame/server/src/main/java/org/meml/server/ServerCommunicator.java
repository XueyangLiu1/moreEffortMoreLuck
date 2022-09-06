package org.meml.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;

public class ServerCommunicator {
    private Socket socket;
    private BufferedReader br;
    private BufferedWriter bw;

    private static final Logger logger = LogManager.getLogger(ServerCommunicator.class);

    public ServerCommunicator(Socket socket) throws IOException {
        this.socket = socket;
        this.br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.bw = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
    }

    /**
     * send a msg string to the client
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
     * read string msg from the client
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

    /**
     * Try to receive a string from the server.
     * @return
     * @throws IOException
     */
    public String nonBlockReceive(){
        String msg = null;
        // This ensures that it will not block.
        try{
            if (br.ready()) {
                msg = br.readLine();
            }
        }catch(IOException e) {
            logger.error("Fail to non-blocking receive msg");
        }
        return msg;
    }
}
