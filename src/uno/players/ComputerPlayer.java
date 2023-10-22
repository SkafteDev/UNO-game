package uno.players;

import uno.cards.Card;
import uno.cards.Color;
import uno.piles.DiscardPile;

import java.util.Random;

public class ComputerPlayer extends Player {

    public ComputerPlayer(String name) {
        super(name);
    }

    @Override
    public Card playCard(DiscardPile pile) {
        Card playableCard = getPlayableCard(pile.getTopCard());

        if (playableCard == null) {
            throw new RuntimeException("Player " + getName() + " has no playable card.");
        }

        if (pile.addCard(playableCard)) {
            this.getHand().remove(playableCard);
        }

        return playableCard;
    }

    private Card getPlayableCard(Card topCard) {
        for (Card potentialMove : getHand()) {
            boolean isMoveValid = potentialMove.matches(topCard);
            if (isMoveValid) {
                return potentialMove;
            }
        }

        return null;
    }

    @Override
    public Color announceCardColor() {
        Random r = new Random();
        Color chosenColor;

        do {
            int randomColorIndex = r.nextInt(0, Color.values().length);
            chosenColor = Color.values()[randomColorIndex];

        } while(chosenColor.equals(Color.WILD));

        return chosenColor;
    }
}
