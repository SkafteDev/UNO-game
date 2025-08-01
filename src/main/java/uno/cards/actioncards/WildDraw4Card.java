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
        java.util.List<uno.cards.Card> drawn = new java.util.ArrayList<>();
        for (int i = 0; i < 4; i++) {
            if (game.getDrawPile().isEmpty()) {
                game.shuffleDiscardPile();
            }
            uno.cards.Card c = game.getDrawPile().draw();
            subsequentPlayer.receiveCard(c);
            drawn.add(c);
        }
        if (game.getListener() != null) {
            game.getListener().onDraw(subsequentPlayer, drawn);
        }

        System.out.println("Skipping next player: " + game.getSubsequentPlayer().getName());
        game.passTurn();
    }

    @Override
    public String toString() {
        return this.getColor() + "_WILD+4";
    }
}
