package processing;

import compound.network.Connect;

import java.io.IOException;

public class Reader extends Equipment {
    Reader(byte[] buff) {
        super(buff);
    }

    public static Talk readHelloMessage(Connect session) throws IOException {
        int read_bytes = session.getClientChannel().read(session.getReadBuff());
        if (read_bytes == -1) {
            session.close();
            return null;
        }

        if (Talk.isCorrectSizeOfMessage(session.getReadBuff())) {
            session.setReadBuff(session.getReadBuff().flip());
            return new Talk(session.getReadBuff());
        }

        return null;
    }

    public static Request readRequestMessage(Connect session) throws IOException {
        int read_bytes = session.getClientChannel().read(session.getReadBuff());
        if (read_bytes == -1) {
            session.close();
            return null;
        }

        if (Request.isCorrectSizeOfMessage(session.getReadBuff())) {
            session.setReadBuff(session.getReadBuff().flip());
            return new Request(session.getReadBuff());
        }

        return null;
    }

    public static byte[] getResponse() {
        byte[] data = new byte[2];
        data[0] = SOCKS_5;
        data[1] = NO_AUTHENTICATION;

        return data;
    }
}
