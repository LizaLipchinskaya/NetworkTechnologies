package server;

import java.io.IOException;

public class ServerMain {
    public static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        Server.start(PORT);
    }
}
