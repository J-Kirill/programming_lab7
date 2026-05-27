package Server;

import common.MetaHashSet;
import common.stored.Route;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Scanner;

public class ServerMain {
    public static MetaHashSet<Route> collection;
    public static Deque<String> history;
    public static Scanner scanner = new Scanner(System.in);
    public static void main(String[] args) throws Exception {
        Class.forName("org.postgresql.Driver");
        collection = DatabaseManager.getInstance().loadAll();
        history = new ArrayDeque<>(List.of("Null", "Null", "Null", "Null", "Null"));
        TCPServer server = new TCPServer();
        server.run();
    }
}
