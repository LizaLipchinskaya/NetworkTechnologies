package client;

import java.io.IOException;

public class ClientMain {
    public static int PORT = 8080;
    public static String ipAddr = "localhost";

    public static void main(String[] args) throws IOException {
        ClientSendFile.sendFile(args[0], ipAddr, PORT);
    }
}
