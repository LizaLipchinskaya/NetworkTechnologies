package server;

import java.util.HashMap;
import java.util.Map;

public class InformationTransferFile {
    private static Map<Integer, Integer> IDAndBytes = new HashMap<>();

    public static void addIDAndBytes(int id) {
        IDAndBytes.put(id, 0);
    }

    public static int countBytes(int id) {
        return IDAndBytes.get(id);
    }

    public static void changeReadBytes(int id, int newBytes) {
        IDAndBytes.put(id, IDAndBytes.get(id) + newBytes);
    }

    public static void delete(int id) {
        IDAndBytes.remove(id);
    }
}
