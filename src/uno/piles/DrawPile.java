package uno.piles;

import uno.cards.Card;
import uno.cards.Color;
import uno.cards.NumberCard;
import uno.cards.actioncards.*;

import java.util.ArrayList;
import java.util.Collections;

public class DrawPile {
    private final ArrayList<Card> cards;

    public DrawPile() {
        this.cards = new ArrayList<>();
        this.cards.addAll(createNumberCards());
        this.cards.addAll(createWildCards());
        this.cards.addAll(createSkipCards());
        this.cards.addAll(createReverseCards());
        this.cards.addAll(createDraw2Cards());
        Collections.shuffle(this.cards);
    }

    public DrawPile(ArrayList<Card> cards) {
        this.cards = cards;
    }

    private ArrayList<Card> createNumberCards() {
        ArrayList<Card> cards = new ArrayList<>();

        Color[] allowedColors = Color.values();
        for (Color color : allowedColors) {
            if (color.equals(Color.WILD)) {
                continue;
            }
            for (int number : NumberCard.ALLOWED_NUMBERS) {
                if (number == 0) {
                    cards.add(new NumberCard(color, number));
                } else {
                    cards.add(new NumberCard(color, number));
                    cards.add(new NumberCard(color, number));
                }

            }
        }

        return cards;
    }

    private ArrayList<Card> createWildCards() {
        ArrayList<Card> cards = new ArrayList<>();

        int numberOfWildCards = 4;

        for (int i = 0; i < numberOfWildCards; i++) {
            cards.add(new WildCard());
            cards.add(new WildDraw4Card());
        }

        return cards;
    }

    private ArrayList<Card> createSkipCards() {
        ArrayList<Card> cards = new ArrayList<>();

        cards.add(new SkipCard(Color.BLUE));
        cards.add(new SkipCard(Color.BLUE));
        cards.add(new SkipCard(Color.GREEN));
        cards.add(new SkipCard(Color.GREEN));
        cards.add(new SkipCard(Color.RED));
        cards.add(new SkipCard(Color.RED));
        cards.add(new SkipCard(Color.YELLOW));
        cards.add(new SkipCard(Color.YELLOW));

        return cards;
    }

    private ArrayList<Card> createReverseCards() {
        ArrayList<Card> cards = new ArrayList<>();

        cards.add(new ReverseCard(Color.BLUE));
        cards.add(new ReverseCard(Color.BLUE));
        cards.add(new ReverseCard(Color.GREEN));
        cards.add(new ReverseCard(Color.GREEN));
        cards.add(new ReverseCard(Color.RED));
        cards.add(new ReverseCard(Color.RED));
        cards.add(new ReverseCard(Color.YELLOW));
        cards.add(new ReverseCard(Color.YELLOW));

        return cards;
    }

    private ArrayList<Card> createDraw2Cards() {
        ArrayList<Card> cards = new ArrayList<>();

        cards.add(new Draw2(Color.BLUE));
        cards.add(new Draw2(Color.BLUE));
        cards.add(new Draw2(Color.GREEN));
        cards.add(new Draw2(Color.GREEN));
        cards.add(new Draw2(Color.RED));
        cards.add(new Draw2(Color.RED));
        cards.add(new Draw2(Color.YELLOW));
        cards.add(new Draw2(Color.YELLOW));

        return cards;
    }

    public Card draw() {
        if (this.cards.isEmpty()) {
            throw new RuntimeException("DrawPile is empty.");
        }

        int cardToRemove = this.cards.size()-1;
        return this.cards.remove(cardToRemove);
    }

    public boolean isEmpty() {
        return this.cards.isEmpty();
    }

    public int getSize() {
        return this.cards.size();
    }
}
