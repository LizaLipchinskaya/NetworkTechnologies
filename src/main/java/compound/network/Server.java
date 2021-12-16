package compound.network;

import compound.interfaces.Handler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

public class Server implements Handler {
    private final ServerSocketChannel serverChannel = ServerSocketChannel.open();
    private final Dns dns;

    public Server(int port, Selector selector) throws IOException {
        dns = new Dns(port, selector);
        serverChannel.bind(new InetSocketAddress(port));
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT, this);
    }

    public void closeDNS() throws IOException {
        dns.close();
    }

    @Override
    public void close() throws IOException {
        serverChannel.close();
    }

    @Override
    public void handle(SelectionKey key) {
        try {
            if (!key.isValid()) {
                close();
                return;
            }

            new Connect(serverChannel.accept(), dns, key.selector());
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
}
