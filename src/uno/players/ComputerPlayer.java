package uno.players;

import uno.cards.Card;
import uno.cards.Color;
import uno.piles.DiscardPile;
import uno.piles.DrawPile;

import java.util.ArrayList;
import java.util.Random;

public class Player {
    private final String name;

    private final ArrayList<Card> hand;

    private Card chosenCard;

    public Player(String name) {
        this.hand = new ArrayList<>();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Card> getHand() {
        return this.hand;
    }

    public void drawCardFrom(DrawPile pile) {
        this.hand.add(pile.draw());
    }

    public void playChosenCard(DiscardPile pile) {
        if (chosenCard != null) {
            if (pile.addCard(chosenCard)) {
                this.hand.remove(chosenCard);
            } else {
                choosePlayableCard(pile.getTopCard());
                playChosenCard(pile);
            }
        }
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

    private Card getPlayableCard(Card topCard) {
        for (Card potentialMove : hand) {
            boolean isMoveValid = potentialMove.matches(topCard);
            if (isMoveValid) {
                return potentialMove;
            }
        }

        return null;
    }

    public void choosePlayableCard(Card topCard) {
        this.chosenCard = getPlayableCard(topCard);
    }

    public Card getChosenCard() {
        return chosenCard;
    }

    public Color announceCardColor() {
        Random r = new Random();
        Color chosenColor;

        do {
            int randomColorIndex = r.nextInt(0, Color.values().length);
            chosenColor = Color.values()[randomColorIndex];

        } while(chosenColor.equals(Color.WILD));

        return chosenColor;
    }

    public void receiveCard(Card c) {
        hand.add(c);
    }

    public void displayHand() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.name);
        sb.append(" hand: | ");

        for (int i = 0; i < hand.size(); i++) {
            String card = String.format("%-15s", hand.get(i));
            sb.append(i).append(":").append(card).append("| ");
        }

        System.out.println(sb);
    }
}
