package client.controller;

import client.net.ServerConnector;

import java.util.concurrent.CompletableFuture;

/*
* Controller class for the client
* Connects the input from user with net Net-layer
* */

public class Controller {
    private final ServerConnector serverConnector = new ServerConnector();
    private String localhost = "localhost";
    private int port = 8080;

    // usd to connect to a specific IP address
    public void connectAdr(String host) {
        CompletableFuture.runAsync(() -> {
            serverConnector.connect(host, port);
        }).thenRun(() -> System.out.println("Connected to " + host + ":" + port));
    }

    // connect to localhost
    public void connect() {
        CompletableFuture.runAsync(() -> {
            serverConnector.connect(localhost, port);
        }).thenRun(() -> System.out.println("Connected to " + localhost + ":" + port));
    }

    public void disconnect() {
        serverConnector.disconnect();
    }

    // send message to the server
    public void sendMsg(String msg) {
        CompletableFuture.runAsync(() -> serverConnector.sendMsg(msg));
    }

}
