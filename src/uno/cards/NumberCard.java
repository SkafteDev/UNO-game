package uno.cards;

public class NumberCard extends Card {

    public static final int[] ALLOWED_NUMBERS = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

    private final int number;

    public NumberCard(Color color, int number) {
        super(color);
        if (!(0 <= number && number <= 9)) {
            throw new IllegalArgumentException("Unsupported number.");
        }
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    @Override
    public boolean matches(Card other) {
        if (this.getColor().equals(other.getColor())) {
            return true;
        }

        if (other instanceof NumberCard otherNumberCard) {
            return this.getNumber() == otherNumberCard.getNumber();
        }

        return false;
    }

    @Override
    public String toString() {
        return this.getColor() + "_" + this.getNumber();
    }
}
