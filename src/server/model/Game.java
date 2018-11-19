package server.model;

/*
* The game logic for hangman game.
* */

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Game {
    private String word;
    private String maskedWord, message;
    private static final String CORRECT_GUESS = "cg";
    private static final String WRONG_GUESS = "wg";
    private static final String WIN = "w";
    private static final String LOSE = "l";
    private static final String RESTART = "r";
    private static final String NEW_GAME = "n";
    private static final String NOT_RECOGNIZED = "nr";
    private static final String hiddenChar = "-";
    private int numAttempts, score = 0;
    private boolean wonGame;

    // Starts and restarts the game
    public String startGame(String condition) {
        ExecutorService executor = Executors.newCachedThreadPool();
        Future<String> futureCall = executor.submit(new Word());
        try {
            word = futureCall.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        numAttempts = word.length();
        wonGame = false;

        switch (condition) {
            case "newGame":
                message = NEW_GAME;
                break;
            case "win":
                message = WIN;
                break;
            case "lose":
                message = LOSE;
                break;
            case "restart":
                message = RESTART;
                break;
            default:
                message = NOT_RECOGNIZED;
                break;
        }
        maskedWord = createGameWord();
        System.out.println("Chosen word: " + word);

        return createGameState();
    }


    // Masks the chosen word
    private String createGameWord() {
        StringBuilder sb = new StringBuilder();

        for (char c : word.toCharArray()) {
            sb.append(hiddenChar);
        }
        return  sb.toString();
    }

    // Handles user input and output based on user input
    public String gameEntry(String input) {
        boolean correctGuess = false;
        wonGame = false;

        if (numAttempts != 0) {
            if (input.length() > 1) {
                if (input.equals(word)) {
                    wonGame = true;
                    maskedWord = word;
                }
            } else if (input.length() == 1) {
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < word.length(); i++) {
                    char c = word.charAt(i);
                    if (c == input.charAt(0)) {
                        sb.append(c);
                        correctGuess = true;
                    } else {
                        sb.append(maskedWord.charAt(i));
                    }
                }

                maskedWord = sb.toString();
                if (!maskedWord.contains(hiddenChar))
                    wonGame = true;
            }

            if (correctGuess) {
                message = CORRECT_GUESS;
            } else {
                message = WRONG_GUESS;
                numAttempts--;
            }
        }

        if (wonGame) {
            return winGame();
        }
        if (numAttempts < 1) {
            return loseGame();
        }
        return createGameState();
    }

    // score manipulation based on what happened
    public String restart(String condition) {
        if (condition.equals(RESTART)) {
            score--;
        }
        return startGame(condition);
    }

    private String loseGame() {
        score--;
        return restart("lose");
    }

    private String winGame() {
        score++;
        return restart("win");
    }


    // Game state creation to be passed to the client
    private String createGameState() {
        try {
            wait(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();

        sb.append(score);
        sb.append("/");
        sb.append(numAttempts);
        sb.append("/");
        sb.append(maskedWord);
        sb.append("/");
        sb.append(message);
        sb.append("/");

        return sb.toString();
    }
}
