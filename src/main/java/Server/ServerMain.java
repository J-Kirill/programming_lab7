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
        collection = DatabaseManager.loadAll();
        history = new ArrayDeque<>(List.of("Null", "Null", "Null", "Null", "Null"));
        if (args.length == 0) {
            TCPServer server = new TCPServer();
            server.run();
        } else {
            throw new Exception("Wrong number of arguments");
        }
    }
}
