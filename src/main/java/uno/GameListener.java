package uno;

import uno.players.Player;
import uno.cards.Card;

public interface GameListener {
    void onState(UnoGame game);
    void onWinner(Player winner);
    void onDraw(Player player, Card card);
}
