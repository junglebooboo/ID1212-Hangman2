package client.view;

import client.controller.Controller;

import java.util.Scanner;

/*
* User interface for startup and game.
* */

public class UserInterface implements Runnable {
    private final String PROMPT = "> ";
    private final Scanner console = new Scanner(System.in);
    private boolean receivingCommands = false;
    private Controller controller;


    // Start UI and receive commands from user.
    // The game is not stared before client connects to the server and starts the game.
    public void start() {
        System.out.println("Welcome to hangman. Type in 'Connect.' to connect to the sever. Type 'Disconnect.' to disconnect.");
        System.out.println("Enter 'ConnectAddr.' to connect to a specific IP.");
        System.out.println("Enter 'Start.' to start a new game after a connection has been established.");
        if (receivingCommands) {
            return;
        }
        receivingCommands = true;
        controller = new Controller();
        new Thread(this).start();
    }


    private String getUserInput() {
        String input = console.nextLine();
        return input;
    }

    // get user input and act accordingly
    @Override
    public void run() {
        while(true){
            String userMsg = getUserInput();

            switch (userMsg) {
                case "Connect.":
                    controller.connect();
                    break;
                case "ConnectAddr.":
                    System.out.print("Enter IP: ");
                    String ip = getUserInput();
                    if (!ip.equals("")) {
                        controller.connectAdr(ip);
                    } else {
                        // debugging addition
                        controller.connectAdr("192.168.0.11");
                    }
                    break;
                case "Disconnect.":
                    receivingCommands = false;
                    controller.disconnect();
                    break;
                default:
                    // if the command is not one of the three above, it's game related
                    controller.sendMsg(userMsg);
                    break;
            }
        }
    }


    // game state message from the the server transformed to user readable information
    private String messageSwitch(String msg) {
        String msgFromServer;
        switch (msg){
            case "w":
                msgFromServer = "You win. New game started";
                break;
            case "l":
                msgFromServer = "You lose. New game started";
                break;
            case "n":
                msgFromServer = "New Game Started";
                break;
            case "r":
                msgFromServer = "You restated the game.";
                break;
            case "d":
                msgFromServer = "Disconnected.";
                break;
            case "cg":
                msgFromServer = "Correct Guess";
                break;
            case "wg":
                msgFromServer = "Wrong Guess";
                break;
            case "nr":
                msgFromServer = "Input not recognized. Try again.";
                break;
            default:
                //msgFromServer = msg;
                msgFromServer = "Unknown Message";
        }
        return msgFromServer;
    }

    // Format and print the data received from the server
    public void showOutput(String fromServer){
        if (fromServer.equals("Disconnected.")) {
            System.out.println("Disconnected from server");
        } else {
            String[] dataToShow = fromServer.split("/");
            System.out.println("_______________________________________________");
            System.out.println(">>> " + messageSwitch(dataToShow[3]) + " <<<");
            System.out.println("Score: " + dataToShow[0] + "     Attempts: " + dataToShow[1]);
            System.out.println("Word:   " + dataToShow[2]);
            System.out.println("_______________________________________________");
            System.out.print(PROMPT);
        }
    }
}

