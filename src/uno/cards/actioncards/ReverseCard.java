package uno.cards.actioncards;

import uno.UnoGame;
import uno.cards.Card;
import uno.cards.Color;

import java.util.Collections;

public class ReverseCard extends ActionCard {
    public ReverseCard(Color color) {
        super(color);
    }

    @Override
    public boolean matches(Card other) {
        if (this.getColor().equals(other.getColor())) {
            return true;
        }

        return other instanceof ReverseCard;
    }

    @Override
    public void action(UnoGame game) {
        if (game.getPlayers().size() == 2) {
            // Act as a skip card
            System.out.println("Reverse card played. Acting as skipping card.");
            game.passTurn();
        }

        System.out.println("Reverse card played.");
        Collections.reverse(game.getPlayers());
    }

    @Override
    public String toString() {
        return this.getColor() + "_REVERSE";
    }
}
