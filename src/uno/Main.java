package uno;

import uno.players.ComputerPlayer;
import uno.players.HumanPlayer;
import uno.players.Player;

public class Main {
    public static void main(String[] args) {
        UnoGame game = new UnoGame();
        Player p1 = new HumanPlayer("Alice");
        Player p2 = new ComputerPlayer("Bob");
        Player p3 = new ComputerPlayer("John");
        game.addPlayer(p1);
        game.addPlayer(p2);
        game.addPlayer(p3);

        game.play();
    }
}
