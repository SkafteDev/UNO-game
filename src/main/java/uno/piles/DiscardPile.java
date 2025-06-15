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
        Card topCard = this.cards.get(this.cards.size()-1);

        this.cards.remove(topCard);
        Collections.shuffle(cards);
        DrawPile drawPile = new DrawPile(new ArrayList<>(cards));
        this.cards.clear();

        this.cards.add(topCard);

        return drawPile;
    }

    public Card getTopCard() {
        int topCard = this.cards.size() - 1;
        return this.cards.get(topCard);
    }
}
