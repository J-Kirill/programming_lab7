package Client;

import common.stored.Request;
import common.stored.Response;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class TCPClient {
    private static final int SERVER_PORT = 11280;
    private static final String SERVER_HOST = "localhost";
    private static final int TIMEOUT_MS = 5000;
    private static final int MAX_RETRIES = 3;

    private final SocketChannel channel;
    private final Selector selector;

    public TCPClient() throws IOException {
        channel = SocketChannel.open();
        channel.configureBlocking(false);
        channel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));

        selector = Selector.open();
        channel.register(selector, SelectionKey.OP_CONNECT);

        if (selector.select(TIMEOUT_MS) == 0) {
            throw new IOException("Connection timeout");
        }

        Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
        while (iter.hasNext()) {
            SelectionKey key = iter.next();
            iter.remove();
            if (key.isConnectable()) {
                channel.finishConnect();
            }
        }
        channel.register(selector, SelectionKey.OP_READ);
        System.out.println("Connected to server");
    }

    public Response sendRequest(Request request) throws IOException {
        byte[] data = serialize(request);

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                ByteBuffer buffer = ByteBuffer.allocate(4 + data.length);
                buffer.putInt(data.length);
                buffer.put(data);
                buffer.flip();
                while (buffer.hasRemaining()) {
                    channel.write(buffer);
                }

                Response response = receiveWithTimeout();
                if (response != null) {
                    return response;
                }

                System.out.printf("Attempt %d/%d: server not responding, retrying...%n",
                        attempt, MAX_RETRIES);

            } catch (IOException e) {
                System.err.println("Network error on attempt " + attempt + ": " + e.getMessage());
            }
        }

        throw new IOException("Server unavailable after " + MAX_RETRIES + " attempts");
    }

    private Response receiveWithTimeout() throws IOException {
        int ready = selector.select(TIMEOUT_MS);
        if (ready == 0) return null;

        Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
        while (iter.hasNext()) {
            SelectionKey key = iter.next();
            iter.remove();

            if (key.isReadable()) {
                ByteBuffer lenBuf = ByteBuffer.allocate(4);
                while (lenBuf.hasRemaining()) {
                    if (channel.read(lenBuf) == -1) throw new IOException("Server closed connection");
                }
                lenBuf.flip();
                int length = lenBuf.getInt();

                ByteBuffer dataBuf = ByteBuffer.allocate(length);
                while (dataBuf.hasRemaining()) {
                    if (channel.read(dataBuf) == -1) throw new IOException("Server closed connection");
                }

                try {
                    return deserialize(dataBuf.array());
                } catch (ClassNotFoundException e) {
                    throw new IOException("Failed to deserialize response", e);
                }
            }
        }

        return null;
    }

    public void close() throws IOException {
        selector.close();
        channel.close();
    }

    private byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        return baos.toByteArray();
    }

    private Response deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
        return (Response) ois.readObject();
    }
}
