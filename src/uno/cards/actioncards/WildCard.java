package uno.cards.actioncards;

import uno.UnoGame;
import uno.cards.Card;
import uno.cards.Color;
import uno.players.Player;

public class WildCard extends ActionCard {

    public WildCard() {
        super(Color.WILD);
    }

    public void setColor(Color color) {
        super.setColor(color);
    }

    @Override
    public boolean matches(Card other) {
        return true;
    }

    @Override
    public void action(UnoGame game) {
        Player player = game.getCurrentPlayer();
        Color color = player.announceCardColor();
        System.out.printf("%s announced color: %s\n\n", player.getName(), color);
        this.setColor(color);
    }

    @Override
    public String toString() {
        return this.getColor() + "_WILD";
    }
}
