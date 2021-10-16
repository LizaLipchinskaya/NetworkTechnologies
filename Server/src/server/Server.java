package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import static server.InformationTransferFile.*;

class Handler extends Thread {
    private Socket socket;
    private final byte[] buf;
    private final int ID;
    private DataInputStream in;
    private DataOutputStream out;
    private FileOutputStream fileOut;
    private int fileSize = 0;
    private Thread checkSpeed;

    Handler(Socket socket, int ID) {
        this.socket = socket;
        buf = new byte[4096];
        this.ID = ID;
    }

    @Override
    public void run() {
        Path path = Paths.get("uploads");

        if (!Files.exists(path)) {
            new File(path.toString()).mkdir();
        }

        Speed speed = new Speed(ID);

        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            String fileName = in.readUTF();
            fileSize = in.readInt();

            System.out.println("File " + fileName + " received");

            while (new File("uploads/" + fileName).exists()) {
                fileName = "(new)".concat(fileName);
            }

            fileOut = new FileOutputStream("uploads/" + fileName);
            addIDAndBytes(ID);
            int countByteRead;

            checkSpeed = new Thread(speed);
            checkSpeed.start();

            while (((countByteRead = in.read(buf)) != -1) && countBytes(ID) < fileSize) {
                changeReadBytes(ID, countByteRead);
                fileOut.write(buf, 0, countByteRead);
            }

            speed.finish();
            checkSpeed.join();

            if (countBytes(ID) == fileSize) {
                out.writeUTF("File transfer finish success");
            } else {
                out.writeUTF("File transfer finish with error");
            }

            delete(ID);

            socket.close();
            in.close();
            out.close();
            fileOut.close();
        } catch (IOException | InterruptedException e) {
            System.out.println("Error");
            e.printStackTrace();
        }

        System.out.println("Transfer with client " + ID + " finished");
    }
}

public class Server {
    public static void start(int port) throws IOException {
        int countProcessors = Runtime.getRuntime().availableProcessors();
        var threadPool = Executors.newFixedThreadPool(countProcessors);

        ServerSocket server = new ServerSocket(port, countProcessors);
        System.out.println("Server start to work");

        int ID = 0;

        try {
            while (true) {
                Socket socket = server.accept();
                threadPool.execute(new Handler(socket, ++ID));
            }
        } finally {
            threadPool.shutdown();
            server.close();
        }
    }
}
