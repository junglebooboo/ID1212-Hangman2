package client.net;

import client.view.UserInterface;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;
/*
* Connector handler to the server.
* */

public class ServerConnector implements Runnable {
    private static final String FATAL_COMMUNICATION_MSG = "Lost connection";

    private InetSocketAddress serverAddress;
    private static final int port = 8080;

    private final ByteBuffer msgFromServer = ByteBuffer.allocateDirect(8192);
    private final Queue<String> sendingQueue = new ArrayDeque<>();
    private SocketChannel socketChannel;
    private Selector selector;
    private boolean connected;
    private volatile boolean timeToSend = false;
    private UserInterface ui;



    @Override
    public void run() {
        try {
            initConnection();
            initSelector();

            while (connected || !sendingQueue.isEmpty()) {
                selector.select();

                if (timeToSend) {
                    socketChannel.keyFor(selector).interestOps(SelectionKey.OP_WRITE);
                    timeToSend = false;
                }

                for (SelectionKey key: selector.selectedKeys()) {
                    selector.selectedKeys().remove(key);
                    if (!key.isValid()) {
                        System.err.println("Key is invalid");
                        continue;
                    }
                    if (key.isConnectable()) {
                        completeConnection(key);
                    } else if (key.isReadable()) {
                        recvFromServer(key);
                    } else if (key.isWritable()) {
                        sendToServer(key);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println(FATAL_COMMUNICATION_MSG);
        }
    }


    public void connect(String host, int port) {
        serverAddress = new InetSocketAddress(host, port);
        new Thread(this).start();
    }

    private void initSelector() throws IOException {
        selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_CONNECT);
    }

    private void initConnection() throws IOException {
        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(serverAddress);
        ui = new UserInterface();
        connected = true;
    }

    // after connection - change interest to read
    private void completeConnection(SelectionKey key) throws IOException {
        socketChannel.finishConnect();
        key.interestOps(SelectionKey.OP_READ);
    }

    public void disconnect() {
        System.out.println("Client is disconnected.");
        connected = false;
        sendingQueue.clear();
        try {
            this.socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        socketChannel.keyFor(selector).cancel();
    }

    public void sendMsg(String msg) {
        synchronized (sendingQueue) {
            sendingQueue.add(msg);
        }
        timeToSend = true;
        selector.wakeup();
    }

    private void sendToServer(SelectionKey key) throws IOException {
        synchronized (sendingQueue) {
            while (!sendingQueue.isEmpty()) {
                ByteBuffer bb = ByteBuffer.wrap(sendingQueue.remove().getBytes());
                socketChannel.write(bb);
                if (bb.hasRemaining()) {
                    return;
                }
            }
            key.interestOps(SelectionKey.OP_READ);
        }
    }

    private void recvFromServer(SelectionKey key) throws IOException {
        msgFromServer.clear();

        int numOfReadBytes = socketChannel.read(msgFromServer);
        if (numOfReadBytes == -1) {
            throw new IOException(FATAL_COMMUNICATION_MSG);
        }
        String recvdString = extractMessageFromBuffer();
        ui.showOutput(recvdString);
        key.interestOps(SelectionKey.OP_WRITE);
    }


    private String extractMessageFromBuffer() {
        msgFromServer.flip();
        byte[] bytes = new byte[msgFromServer.remaining()];
        msgFromServer.get(bytes);
        return new String(bytes);
    }



}
