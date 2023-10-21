package uno;

import uno.cards.Card;
import uno.cards.actioncards.ActionCard;
import uno.cards.actioncards.WildCard;
import uno.piles.DiscardPile;
import uno.piles.DrawPile;
import uno.players.Player;

import java.util.ArrayList;
import java.util.Random;

public class UnoGame {

    private DrawPile drawPile;
    private DiscardPile discardPile;
    private ArrayList<Player> players;
    private Player currentPlayer;
    private Player winner;

    public UnoGame() {
        this.players = new ArrayList<>();
        this.drawPile = new DrawPile();
        this.discardPile = new DiscardPile(initializeTopCard());
    }

    private Card initializeTopCard() {
        Card firstTopCard = drawPile.draw();
        while ((firstTopCard instanceof WildCard)) {
            firstTopCard = drawPile.draw();
        }
        return firstTopCard;
    }

    public void shuffleAndTurnAround() {
        if (drawPile.isEmpty()) {
            drawPile = discardPile.shuffleAndTurnAround();
        }
    }

    public void passTurn() {
        this.currentPlayer = getSubsequentPlayer();
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void play() {
        if ((this.players.size() < 2 || this.players.size() > 10)) {
            throw new RuntimeException("Minimum 2 (two) and maximum 10 (ten) players allowed.");
        }

        this.dealCards();
        this.currentPlayer = randomPlayer();

        do {
            printGameStatus();
            drawIfNoPlayableHand(currentPlayer);
            currentPlayer.displayHand();

            if (!currentPlayer.hasPlayableHand(discardPile.getTopCard())) {
                System.out.printf("\n%s has no playable hand. Passing turn. \n", currentPlayer.getName());
                passTurn();
                continue; // No playable hand. Next player's turn.
            }

            currentPlayer.choosePlayableCard(discardPile.getTopCard());
            System.out.println(currentPlayer.getName() + " plays: " + currentPlayer.getChosenCard());
            currentPlayer.playChosenCard(discardPile);

            // If the player lay down an action card:
            // Perform the action of that card.
            // E.g., skip next player, reverse player order, next player draws 2 cards, ...
            if (discardPile.getTopCard() instanceof ActionCard actionCard) {
                actionCard.action(this);
            }

            passTurn();

        } while(!isGameFinished());


        System.out.printf("Winner:       \t %s\n", this.winner.getName());
    }

    private void drawIfNoPlayableHand(Player currentPlayer) {
        if (!currentPlayer.hasPlayableHand(discardPile.getTopCard())) {
            System.out.printf("\n%s has no playable hand. Drawing.\n", currentPlayer.getName());
            if (drawPile.isEmpty()) {
                shuffleAndTurnAround();
            }
            currentPlayer.drawCardFrom(drawPile);
        }
    }

    private Player randomPlayer() {
        int randomPlayerIndex = new Random().nextInt(this.players.size());
        return this.players.get(randomPlayerIndex);
    }

    public void printGameStatus() {
        StringBuilder sb = new StringBuilder("\n");

        String pilesString = String.format("|Drawpile: %-9d | Discardpile: %-14s |\n", drawPile.getSize(), discardPile.getTopCard());

        sb.append("+--------------------+-----------------------------+\n");
        sb.append(pilesString);
        sb.append("+--------------------+-----------------------------+\n");

        for (Player p: players) {
            sb.append("|");
            String playerString = "";
            if (p.equals(currentPlayer)) {
                playerString = String.format(">Player: %-10s | Hand: %-21d |\n", p.getName(), p.getHand().size());
            } else {
                playerString = String.format("Player: %-11s | Hand: %-21d |\n", p.getName(), p.getHand().size());
            }
            sb.append(playerString);
        }
        sb.append("+--------------------+-----------------------------+\n");

        System.out.println(sb);

    }

    public boolean isGameFinished() {
        for (Player p : players) {
            if (p.getHand().isEmpty()) {
                this.winner = p;
                return true;
            }
        }

        return false;
    }

    public void addPlayer(Player p) {
        if (this.players.size() > 10) {
            throw new RuntimeException("Maximum 10 players allowed.");
        }

        if (this.players.contains(p)) {
            throw new RuntimeException("Cannot add the same player twice.");
        }

        this.players.add(p);
    }

    private void dealCards() {
        int cardsToEachPlayer = 7;

        for (Player p: players) {
            for (int i = 0; i < cardsToEachPlayer; i++) {
                p.receiveCard(drawPile.draw());
            }
        }
    }

    public DrawPile getDrawPile() {
        return drawPile;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public Player getSubsequentPlayer() {
        int currentPlayerIndex = this.players.indexOf(currentPlayer);
        int nextPlayerIndex = currentPlayerIndex + 1;

        if (nextPlayerIndex >= players.size()) {
            nextPlayerIndex = 0;
        }

        return this.players.get(nextPlayerIndex);
    }

    public Player getPreviousPlayer() {
        int currentPlayerIndex = this.players.indexOf(currentPlayer);
        int previousPlayerIndex = currentPlayerIndex - 1;

        if (previousPlayerIndex < 0) {
            previousPlayerIndex = this.players.size() - 1;
        }

        return this.players.get(previousPlayerIndex);
    }
}
