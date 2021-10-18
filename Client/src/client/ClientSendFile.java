package client;

import java.io.*;
import java.net.Socket;
import com.google.common.hash.*;

public class ClientSendFile {
    public static void sendFile(String path, String addr, int port) throws IOException {
        Socket socket = new Socket(addr, port);
        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        File file = new File(path);
        FileInputStream fileIn = new FileInputStream(file);

        out.writeUTF(file.getName());
        out.writeLong(file.length());

        byte[] buf = new byte[4096];
        int i;
        String sha256hex;

        while ((i = fileIn.read(buf)) != -1) {
            sha256hex = Hashing.sha256().hashBytes(buf).toString();
            out.write(buf, 0, i);
            out.flush();
        }

        String answer = in.readUTF();
        System.out.println(answer);

        socket.close();
        in.close();
        out.close();
        fileIn.close();
    }
}
