package uno.piles;

import uno.cards.Card;
import uno.cards.actioncards.WildCard;

import java.util.ArrayList;
import java.util.Collections;

public class DiscardPile {
    private final ArrayList<Card> cards;

    public DiscardPile(Card topCard) {
        this.cards = new ArrayList<>();
        this.cards.add(topCard);
    }

    public boolean addCard(Card playedCard) {
        boolean isMoveValid = playedCard.matches(getTopCard());

        if (isMoveValid) {
            this.cards.add(playedCard);
        }

        return isMoveValid;
    }

    public DrawPile shuffleAndTurnAround() {
        Collections.shuffle(cards);
        DrawPile drawPile = new DrawPile(new ArrayList<>(cards));
        this.cards.clear();

        Card potentialTopCard;

        do {
            potentialTopCard = drawPile.draw();
            this.cards.add(potentialTopCard);
        } while (potentialTopCard instanceof WildCard);

        return drawPile;
    }

    public Card getTopCard() {
        int topCard = this.cards.size() - 1;
        return this.cards.get(topCard);
    }
}
