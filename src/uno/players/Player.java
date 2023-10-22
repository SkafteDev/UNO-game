package uno.players;

import uno.cards.Card;
import uno.cards.Color;
import uno.piles.DiscardPile;
import uno.piles.DrawPile;

import java.util.ArrayList;

public abstract class Player {

    private final String name;
    private final ArrayList<Card> hand;

    public Player(String name) {
        this.hand = new ArrayList<>();
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public ArrayList<Card> getHand() {
        return this.hand;
    }

    public void drawCardFrom(DrawPile pile) {
        this.hand.add(pile.draw());
    }

    public boolean hasPlayableHand(Card topCard) {
        boolean hasPlayableHand = false;

        for (Card c : hand) {
            hasPlayableHand = c.matches(topCard);
            if (hasPlayableHand) {
                return true;
            }
        }

        return hasPlayableHand;

    }

    public void receiveCard(Card card) {
        hand.add(card);
    }

    public void displayHand() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.name);
        sb.append(" hand: ");

        for (int i = 0; i < hand.size(); i++) {
            String card = String.format("%s", hand.get(i));
            sb.append(i).append(":").append(card).append("\t\t");
        }

        System.out.println(sb);
    }

    public abstract Card playCard(DiscardPile pile);

    public abstract Color announceCardColor();
}
