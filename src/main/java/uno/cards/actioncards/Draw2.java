package uno.cards.actioncards;

import uno.UnoGame;
import uno.cards.Card;
import uno.cards.Color;
import uno.players.Player;

public class Draw2 extends ActionCard {
    public Draw2(Color color) {
        super(color);
    }

    @Override
    public boolean matches(Card other) {
        if (this.getColor().equals(other.getColor())) {
            return true;
        }

        return other instanceof Draw2;
    }

    @Override
    public void action(UnoGame game) {
        Player subsequentPlayer = game.getSubsequentPlayer();

        // Draw 2 cards.
        java.util.List<uno.cards.Card> drawn = new java.util.ArrayList<>();
        for (int i = 0; i < 2; i++) {
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
        return this.getColor() + "_DRAW2";
    }
}
