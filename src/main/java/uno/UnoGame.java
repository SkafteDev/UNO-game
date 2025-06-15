package uno;

import uno.cards.Card;
import uno.cards.actioncards.ActionCard;
import uno.piles.DiscardPile;
import uno.piles.DrawPile;
import uno.players.Player;
import uno.GameListener;

import java.util.ArrayList;
import java.util.Random;

public class UnoGame {

    private DrawPile drawPile;
    private final DiscardPile discardPile;
    private final ArrayList<Player> players;
    private Player currentPlayer;
    private Player winner;
    private GameListener listener;

    public UnoGame() {
        this.players = new ArrayList<>();
        this.drawPile = new DrawPile();
        this.discardPile = new DiscardPile(initializeTopCard());
    }

    public void setListener(GameListener listener) {
        this.listener = listener;
    }

    public DrawPile getDrawPile() {
        return drawPile;
    }

    public DiscardPile getDiscardPile() {
        return discardPile;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
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

    public void addPlayer(Player p) {
        if (this.players.size() >= 8) {
            throw new RuntimeException("Maximum 8 players allowed.");
        }

        if (this.players.contains(p)) {
            throw new RuntimeException("Cannot add the same player twice.");
        }

        this.players.add(p);
    }

    public void passTurn() {
        this.currentPlayer = getSubsequentPlayer();
    }

    public void shuffleDiscardPile() {
        if (drawPile.isEmpty()) {
            drawPile = discardPile.shuffleAndTurnAround();
        }
    }

    public void play() {
        if ((this.players.size() < 2 || this.players.size() > 8)) {
            throw new RuntimeException("Minimum 2 (two) and maximum 8 (eight) players allowed.");
        }

        this.dealCards();
        this.currentPlayer = randomPlayer();
        if (listener != null) listener.onState(this);

        do {
            printGameStatus();
            if (listener != null) listener.onState(this);
            drawIfNoPlayableHand(currentPlayer);
            if (listener != null) listener.onState(this);
            currentPlayer.displayHand();

            boolean hasPlayable = currentPlayer.hasPlayableHand(discardPile.getTopCard());
            if (!hasPlayable) {
                System.out.printf("\n%s has no playable hand. Waiting for skip.\n", currentPlayer.getName());
            }

            Card playedCard = currentPlayer.playCard(discardPile);
            if (playedCard == null) {
                System.out.println(currentPlayer.getName() + " skipped.");
            } else {
                System.out.println(currentPlayer.getName() + " played: " + playedCard);
                if (playedCard instanceof ActionCard actionCard) {
                    actionCard.action(this);
                }
            }

            passTurn();

        } while(!isGameFinished());


        System.out.printf("Winner:       \t %s\n", this.winner.getName());
        if (listener != null) listener.onWinner(this.winner);
    }

    private Card drawIfNoPlayableHand(Player currentPlayer) {
        if (!currentPlayer.hasPlayableHand(discardPile.getTopCard())) {
            System.out.printf("\n%s has no playable hand. Drawing.\n", currentPlayer.getName());
            if (drawPile.isEmpty()) {
                shuffleDiscardPile();
            }
            Card drawn = drawPile.draw();
            currentPlayer.receiveCard(drawn);
            if (listener != null) listener.onDraw(currentPlayer, drawn);
            return drawn;
        }
        return null;
    }

    private Player randomPlayer() {
        int randomPlayerIndex = new Random().nextInt(this.players.size());
        return this.players.get(randomPlayerIndex);
    }

    private Card initializeTopCard() {
        Card firstTopCard;
        do {
            firstTopCard = drawPile.draw();

            if(firstTopCard instanceof ActionCard) {
                drawPile.placeCardRandomly(firstTopCard);
            }
        } while ((firstTopCard instanceof ActionCard));

        return firstTopCard;
    }

    private void printGameStatus() {
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

    private boolean isGameFinished() {
        for (Player p : players) {
            if (p.getHand().isEmpty()) {
                this.winner = p;
                return true;
            }
        }

        return false;
    }

    private void dealCards() {
        int cardsToEachPlayer = 7;

        for (Player p: players) {
            for (int i = 0; i < cardsToEachPlayer; i++) {
                p.receiveCard(drawPile.draw());
            }
        }
    }
}
