package compound.interfaces;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public interface Socket extends Handler {
    void read(SelectionKey key) throws IOException;

    void write(SelectionKey key) throws IOException;

}
