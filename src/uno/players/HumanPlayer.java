package uno.players;

import uno.cards.Card;
import uno.cards.Color;
import uno.piles.DiscardPile;

import java.util.Scanner;

public class HumanPlayer extends Player {
    public HumanPlayer(String name) {
        super(name);
    }

    @Override
    public Card playCard(DiscardPile pile) {
        Card playableCard = getPlayableCard(pile.getTopCard());

        if (pile.addCard(playableCard)) {
            this.getHand().remove(playableCard);
        }

        return playableCard;
    }

    private Card getPlayableCard(Card topCard) {
        Scanner scanner = new Scanner(System.in);
        Card selectedCard = null;

        do {
            System.out.println("Choose a playable card from the hand.");
            int index = Integer.parseInt(scanner.nextLine());
            if (index < 0 || index > getHand().size()) {
                System.out.println("Invalid index chosen.");
                continue;
            }
            selectedCard = getHand().get(index);

        } while(!selectedCard.matches(topCard));

        return selectedCard;
    }

    @Override
    public Color announceCardColor() {
        Scanner scanner = new Scanner(System.in);
        Color selectedColor = null;

        do {
            System.out.println("Choose a color: ");
            for (int i = 0; i < Color.values().length; i++) {
                if (Color.values()[i].equals(Color.WILD)) {
                    continue;
                }
                System.out.print(i + ":" + Color.values()[i] + " ");
            }
            System.out.println();

            int index = Integer.parseInt(scanner.nextLine());
            if (index < 1 || index > Color.values().length) {
                System.out.println("Invalid index chosen.");
                continue;
            }
            selectedColor = Color.values()[index];

        } while(selectedColor == null);

        return selectedColor;
    }
}
