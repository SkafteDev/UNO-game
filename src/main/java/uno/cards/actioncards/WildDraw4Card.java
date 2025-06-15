package uno.cards.actioncards;

import uno.UnoGame;
import uno.players.Player;

public class WildDraw4Card extends WildCard {
    public WildDraw4Card() {
        super();
    }

    @Override
    public void action(UnoGame game) {
        super.action(game);
        Player subsequentPlayer = game.getSubsequentPlayer();

        // Draw 4 cards.
        for (int i = 0; i < 4; i++) {
            if (game.getDrawPile().isEmpty()) {
                game.shuffleDiscardPile();
            }
            subsequentPlayer.drawCardFrom(game.getDrawPile());
        }

        System.out.println("Skipping next player: " + game.getSubsequentPlayer().getName());
        game.passTurn();
    }

    @Override
    public String toString() {
        return this.getColor() + "_WILD+4";
    }
}
