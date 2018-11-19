package server.controller;

import server.model.Game;

/*
* Controller for the server class.
* */

public class Controller {

    private Game game;

    public String startGame() {
        game = new Game();
        return game.startGame("newGame");
    }

    public String restart() {
        return game.restart("restart");
    }

    public String gameEntry(String input) {
        return game.gameEntry(input);
    }


}
