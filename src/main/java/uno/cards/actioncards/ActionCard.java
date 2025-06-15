package uno.cards.actioncards;

import uno.UnoGame;
import uno.cards.Card;
import uno.cards.Color;

public abstract class ActionCard extends Card {
    public ActionCard(Color color) {
        super(color);
    }

    public abstract void action(UnoGame game);
}
