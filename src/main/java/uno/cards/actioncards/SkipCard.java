package uno.cards.actioncards;

import uno.UnoGame;
import uno.cards.Card;
import uno.cards.Color;
import uno.players.Player;

public class SkipCard extends ActionCard {
    public SkipCard(Color color) {
        super(color);
    }

    @Override
    public boolean matches(Card other) {
        if (this.getColor().equals(other.getColor())) {
            return true;
        }

        return other instanceof SkipCard;
    }

    @Override
    public void action(UnoGame game) {
        Player subsequentPlayer = game.getSubsequentPlayer();
        System.out.println("Skipping next player: " + subsequentPlayer.getName());
        game.passTurn();
    }

    @Override
    public String toString() {
        return this.getColor() + "_SKIP";
    }
}
