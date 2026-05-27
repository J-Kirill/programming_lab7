package Server;

import common.stored.Request;
import common.stored.Response;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.*;

public class TCPServer {
    private final ServerSocketChannel serverChannel;
    private final Selector selector;
    private final Scanner scanner;
    private boolean running = true;

    private final ExecutorService readPool   = Executors.newFixedThreadPool(4);
    private final ExecutorService handlePool = Executors.newFixedThreadPool(4);
    private final ForkJoinPool sendPool   = new ForkJoinPool(4);

    public TCPServer() throws IOException { this(11280); }

    public TCPServer(int port) throws IOException {
        this.scanner = new Scanner(System.in);
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(port));
        selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Server started on port " + port);
    }

    public void run() throws IOException {
        while (running) {
            checkNetwork();
            checkConsole();
        }
        readPool.shutdown();
        handlePool.shutdown();
        sendPool.shutdown();
    }

    private void checkNetwork() throws IOException {
        int ready = selector.select(100);
        if (ready == 0) return;

        Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
        while (iter.hasNext()) {
            SelectionKey key = iter.next();
            iter.remove();
            if (key.isAcceptable()) handleAccept();
            else if (key.isReadable()) handleRead(key);
        }
    }

    private void handleAccept() throws IOException {
        SocketChannel clientChannel = serverChannel.accept();
        if (clientChannel == null) return;
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
        System.out.println("Client connected: " + clientChannel.getRemoteAddress());
    }

    private void handleRead(SelectionKey key) {
        SocketChannel clientChannel = (SocketChannel) key.channel();

        final byte[] data;
        try {
            data = readBytes(clientChannel);
            if (data == null) {
                System.out.println("Client disconnected");
                key.cancel();
                clientChannel.close();
                return;
            }
        } catch (IOException e) {
            System.err.println("Read error: " + e.getMessage());
            key.cancel();
            try { clientChannel.close(); } catch (IOException ignored) {}
            return;
        }

        // 1. readPool — десериализация запроса
        readPool.submit(() -> {
            try {
                Request request = deserialize(data);

                // 2. handlePool — выполнение команды
                handlePool.submit(() -> {
                    try {
                        Response response = CommandManager.handle(request);

                        // 3. sendPool — отправка ответа
                        sendPool.submit(() -> {
                            try {
                                sendResponse(clientChannel, response);
                            } catch (IOException e) {
                                System.err.println("Send error: " + e.getMessage());
                            }
                        });

                    } catch (Exception e) {
                        System.err.println("Handle error: " + e.getMessage());
                    }
                });

            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Deserialize error: " + e.getMessage());
            }
        });
    }


    private byte[] readBytes(SocketChannel channel) throws IOException {
        ByteBuffer lenBuf = ByteBuffer.allocate(4);
        int bytesRead = channel.read(lenBuf);
        if (bytesRead == -1) return null;

        lenBuf.flip();
        int length = lenBuf.getInt();

        ByteBuffer dataBuf = ByteBuffer.allocate(length);
        while (dataBuf.hasRemaining()) {
            if (channel.read(dataBuf) == -1) return null;
        }

        return dataBuf.array();
    }

    private void sendResponse(SocketChannel channel, Response response) throws IOException {
        byte[] data = serialize(response);
        ByteBuffer buffer = ByteBuffer.allocate(4 + data.length);
        buffer.putInt(data.length);
        buffer.put(data);
        buffer.flip();
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }

    private void checkConsole() throws IOException {
        if (System.in.available() == 0) return;

        String line = scanner.nextLine().trim();
        handleServerCommand(line);
    }

    private void handleServerCommand(String line) {
        switch (line) {
            case "help" -> {
                System.out.println("Commands:\n" +
                        "save\n" +
                        "exit\n" +
                        "register");
            }
            case "save" -> {
                System.out.println("Collection saved.");
            }
            case "exit" -> {
                System.out.println("Shutting down...");
                running = false;
            }
            case "register" -> {
                try {
                    System.out.println("Write login");
                    String login = scanner.nextLine();
                    System.out.println("Write password");
                    String password = scanner.nextLine();
                    CommandManager.registerUser(login, password);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            default -> System.out.println("Unknown server command: " + line);
        }
    }

    private byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        return baos.toByteArray();
    }

    private Request deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
        return (Request) ois.readObject();
    }
}
