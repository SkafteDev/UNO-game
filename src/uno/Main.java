package uno;

import uno.players.Player;

public class Main {
    public static void main(String[] args) {
        UnoGame game = new UnoGame();
        Player p1 = new Player("Alice");
        Player p2 = new Player("Bob");
        Player p3 = new Player("John");
        game.addPlayer(p1);
        game.addPlayer(p2);
        game.addPlayer(p3);

        game.play();
    }
}
