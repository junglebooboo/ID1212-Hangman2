package server.net;

import server.controller.Controller;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;

/*
* Client handler of each client connected. Each client is started in a new thread.
* */

public class ClientHandler {
    private final SocketChannel clientChannel;
    private final ByteBuffer msgFromClient = ByteBuffer.allocateDirect(8192);
    private Controller controller = new Controller();
    private final Queue<String> sendingQueue = new ArrayDeque<>();
    private final Queue<String> receivingQueue = new ArrayDeque<>();


    ClientHandler(SocketChannel clientChannel) {
        this.clientChannel = clientChannel;
        System.out.println("Client connected: " + clientChannel);
    }

    void sendMsg() throws IOException {
        synchronized (sendingQueue) {
            while (!sendingQueue.isEmpty()) {
                ByteBuffer bb = ByteBuffer.wrap(sendingQueue.remove().getBytes());
                try {
                    clientChannel.write(bb);
                } catch (IOException e) {
                    throw new IOException("Could not send message.");
                }
            }
        }
    }

    void receiveMsg() throws IOException {
        msgFromClient.clear();
        int numOfReadBytes = clientChannel.read(msgFromClient);
        if (numOfReadBytes == -1) {
            System.err.println("Client has closed the connection");
            clientChannel.close();
        }
        receivingQueue.add(extractMessageFromBuffer());

        processMsg();
        //ForkJoinPool.commonPool().execute(this);
    }

    public void processMsg() {
        while (!receivingQueue.isEmpty()) {
            try {
                String msg = receivingQueue.remove();
                if (msg == null || msg.equals("")) {
                    System.err.println("No message received");
                } else {
                    switch (msg) {
                        case "Start.":
                            System.out.println("Received msg from client: " + msg);
                            addResponse(controller.startGame());
                            break;
                        case "Test.":
                            System.out.println("Received msg from client: " + msg);
                            addResponse(controller.startGame());
                            break;
                        case "Restart.":
                            System.out.println("Received msg from client: " + msg);
                            addResponse(controller.restart());
                            break;
                        case "Disconnect.":
                            System.out.println("Received msg from client: " + msg);
                            disconnectClient();
                            break;
                        default:
                            System.out.println("Received msg from client: " + msg);
                            addResponse(controller.gameEntry(msg));
                            break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addResponse(String msg) {
        synchronized (sendingQueue) {
            sendingQueue.add(msg);
        }
    }

    private String extractMessageFromBuffer() {
        msgFromClient.flip();
        byte[]bytes = new byte[msgFromClient.remaining()];
        msgFromClient.get(bytes);
        return new String(bytes);
    }

    public void disconnectClient() throws IOException {
        clientChannel.close();
    }
}
