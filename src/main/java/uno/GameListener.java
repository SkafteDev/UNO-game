package uno;

import uno.players.Player;

public interface GameListener {
    void onState(UnoGame game);
    void onWinner(Player winner);
}
