package server.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/*
* Net layer to connect server to client
* */

public class GameServer {
    private static final int LINGER_TIME = 5000;
    private int portNo = 8080;
    private Selector selector;
    private ServerSocketChannel listeningSocketChannel;

    public static void main(String[] args) {
        GameServer server = new GameServer();
        server.serve();
    }

    private void serve() {
        try {
            // start selector and connect to socket
            initSelector();
            initListeningSocketChannel();

            while (true) {
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isAcceptable()) {
                        // create a new client handler
                        startHandler(key);
                    } else if (key.isReadable()) {
                        recvFromClient(key);
                    } else if (key.isWritable()) {
                        sendToClient(key);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Server failure.");
        }
    }

    private void startHandler(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverSocketChannel.accept();
        clientChannel.configureBlocking(false);         // set non-blocking
        ClientHandler handler = new ClientHandler(clientChannel);
        clientChannel.register(selector, SelectionKey.OP_WRITE, handler);
        clientChannel.setOption(StandardSocketOptions.SO_LINGER, LINGER_TIME);
    }


    private void recvFromClient(SelectionKey key) throws IOException {
        ClientHandler handler = (ClientHandler) key.attachment();
        try {
            handler.receiveMsg();
            for (SelectionKey k : selector.keys()) {
                if (k.channel() instanceof SocketChannel && k.isValid()) {
                    k.interestOps(SelectionKey.OP_WRITE);
                }
            }
        } catch (IOException e) {
            removeClient(key);
        }
    }

    private void sendToClient(SelectionKey key) throws IOException {
        ClientHandler client = (ClientHandler) key.attachment();
        try {
            client.sendMsg();
            key.interestOps(SelectionKey.OP_READ);
        } catch (IOException e) {
            removeClient(key);
        }

    }

    private void removeClient(SelectionKey clientKey) throws IOException {
        ClientHandler client = (ClientHandler) clientKey.attachment();
        client.disconnectClient();
        clientKey.cancel();
    }

    private void initSelector() throws IOException {
        selector = Selector.open();

    }

    private void initListeningSocketChannel() throws IOException {
        listeningSocketChannel = ServerSocketChannel.open();
        listeningSocketChannel.configureBlocking(false);
        listeningSocketChannel.bind(new InetSocketAddress(portNo));
        listeningSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }


}
