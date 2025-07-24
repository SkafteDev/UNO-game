package uno.server;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.corundumstudio.socketio.listener.DataListener;

import uno.UnoGame;
import uno.GameListener;
import uno.players.Player;
import uno.cards.Card;
import uno.cards.Color;
import uno.piles.DiscardPile;

import java.util.*;

public class UnoServer implements GameListener {

    private static class RemotePlayer extends Player {
        private final SocketIOClient client;
        private final Object lock = new Object();
        private volatile Integer chosenIndex = null;
        private volatile Color chosenColor = null;

        RemotePlayer(String name, SocketIOClient client) {
            super(name);
            this.client = client;
        }

        public SocketIOClient getClient() {
            return client;
        }

        @Override
        public Card playCard(DiscardPile pile) {
            boolean hasPlayable = hasPlayableHand(pile.getTopCard());
            client.sendEvent("yourTurn", getHand().toString(), pile.getTopCard().toString());
            while (true) {
                synchronized (lock) {
                    chosenIndex = null;
                    client.sendEvent("requestPlay", hasPlayable);
                    if (!hasPlayable) {
                        client.sendEvent("noPlayable", "No playable hand. Click Skip.");
                    }
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                if (chosenIndex == null) {
                    continue;
                }
                if (chosenIndex == -1) {
                    return null;
                }
                if (chosenIndex < 0 || chosenIndex >= getHand().size()) {
                    client.sendEvent("message", "Invalid play. Choose another card.");
                    continue;
                }
                Card card = getHand().get(chosenIndex);
                if (!card.matches(pile.getTopCard())) {
                    client.sendEvent("message", "Invalid play. Choose another card.");
                    continue;
                }
                if (pile.addCard(card)) {
                    getHand().remove(card);
                }
                return card;
            }
        }

        @Override
        public Color announceCardColor() {
            client.sendEvent("requestColor", "");
            synchronized (lock) {
                chosenColor = null;
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return chosenColor == null ? Color.RED : chosenColor;
        }

        void receivePlay(int index) {
            synchronized (lock) {
                this.chosenIndex = index;
                lock.notify();
            }
        }

        void receiveColor(String color) {
            synchronized (lock) {
                try {
                    this.chosenColor = Color.valueOf(color);
                } catch (Exception e) {
                    this.chosenColor = Color.RED;
                }
                lock.notify();
            }
        }
    }

    private final SocketIOServer server;
    private final UnoGame game = new UnoGame();
    private final Map<UUID, RemotePlayer> remotePlayers = new LinkedHashMap<>();
    private final Set<UUID> readyPlayers = new HashSet<>();
    private boolean started = false;

    public UnoServer(int port) {
        Configuration config = new Configuration();
        config.setHostname("localhost");
        config.setPort(port);
        server = new SocketIOServer(config);

        server.addConnectListener(new ConnectListener() {
            @Override
            public void onConnect(SocketIOClient client) {
                System.out.println("Client connected: " + client.getSessionId());
            }
        });

        server.addDisconnectListener(new DisconnectListener() {
            @Override
            public void onDisconnect(SocketIOClient client) {
                RemotePlayer p = remotePlayers.remove(client.getSessionId());
                readyPlayers.remove(client.getSessionId());
                if (p != null) {
                    game.getPlayers().remove(p);
                    broadcast(p.getName() + " disconnected.");
                    if (!started) {
                        sendWaitingState();
                    }
                    checkStartGame();
                }
            }
        });

        server.addEventListener("join", String.class, new DataListener<String>() {
            @Override
            public void onData(SocketIOClient client, String name, AckRequest ack) {
                if (started) {
                    client.sendEvent("message", "Game already started");
                    client.disconnect();
                    return;
                }
                if (remotePlayers.size() >= 8) {
                    client.sendEvent("message", "Server full");
                    client.disconnect();
                    return;
                }
                RemotePlayer rp = new RemotePlayer(name, client);
                remotePlayers.put(client.getSessionId(), rp);
                game.addPlayer(rp);
                broadcast(name + " joined the game.");
                client.sendEvent("message", "Type 'ready' when you are ready to play.");
                sendWaitingState();
                checkStartGame();
            }
        });

        server.addEventListener("ready", String.class, new DataListener<String>() {
            @Override
            public void onData(SocketIOClient client, String data, AckRequest ackRequest) {
                RemotePlayer rp = remotePlayers.get(client.getSessionId());
                if (rp != null && !started) {
                    if (readyPlayers.add(client.getSessionId())) {
                        broadcast(rp.getName() + " is ready.");
                        sendWaitingState();
                        checkStartGame();
                    }
                }
            }
        });

        server.addEventListener("play", Integer.class, new DataListener<Integer>() {
            @Override
            public void onData(SocketIOClient client, Integer data, AckRequest ackRequest) {
                RemotePlayer rp = remotePlayers.get(client.getSessionId());
                if (rp != null) {
                    rp.receivePlay(data);
                }
            }
        });

        server.addEventListener("color", String.class, new DataListener<String>() {
            @Override
            public void onData(SocketIOClient client, String data, AckRequest ackRequest) {
                RemotePlayer rp = remotePlayers.get(client.getSessionId());
                if (rp != null) {
                    rp.receiveColor(data);
                }
            }
        });

        server.addEventListener("chat", String.class, new DataListener<String>() {
            @Override
            public void onData(SocketIOClient client, String data, AckRequest ackRequest) {
                RemotePlayer rp = remotePlayers.get(client.getSessionId());
                if (rp != null) {
                    broadcastChat(rp.getName() + ": " + data);
                }
            }
        });
    }

    private void broadcast(String msg) {
        System.out.println(msg);
        for (RemotePlayer p : remotePlayers.values()) {
            p.getClient().sendEvent("message", msg);
        }
    }

    private void broadcastChat(String msg) {
        System.out.println("CHAT: " + msg);
        for (RemotePlayer p : remotePlayers.values()) {
            p.getClient().sendEvent("chat", msg);
        }
    }

    private void sendWaitingState() {
        List<Map<String, Object>> info = new ArrayList<>();
        for (Map.Entry<UUID, RemotePlayer> e : remotePlayers.entrySet()) {
            Map<String, Object> m = new HashMap<>();
            m.put("name", e.getValue().getName());
            m.put("ready", readyPlayers.contains(e.getKey()));
            info.add(m);
        }
        for (RemotePlayer p : remotePlayers.values()) {
            p.getClient().sendEvent("waiting", info);
        }
    }

    private void sendGameState() {
        for (RemotePlayer p : remotePlayers.values()) {
            Map<String, Object> state = new HashMap<>();
            state.put("deck", game.getDrawPile().getSize());
            state.put("topCard", game.getDiscardPile().getTopCard().toString());
            state.put("current", game.getCurrentPlayer() != null ? game.getCurrentPlayer().getName() : "");
            List<Map<String, Object>> playersInfo = new ArrayList<>();
            for (Player pl : game.getPlayers()) {
                Map<String, Object> pi = new HashMap<>();
                pi.put("name", pl.getName());
                pi.put("cards", pl.getHand().size());
                playersInfo.add(pi);
            }
            state.put("players", playersInfo);
            List<String> hand = new ArrayList<>();
            for (Card c : p.getHand()) {
                hand.add(c.toString());
            }
            state.put("hand", hand);
            p.getClient().sendEvent("state", state);
        }
    }

    private void checkStartGame() {
        if (!started && readyPlayers.size() >= 2 && readyPlayers.size() == remotePlayers.size()) {
            started = true;
            broadcast("All players ready. Game starting...");
            for (RemotePlayer p : remotePlayers.values()) {
                p.getClient().sendEvent("start", "");
            }
            new Thread(this::playGame).start();
        }
    }

    private void playGame() {
        game.setListener(this);
        game.play();
    }

    public void start() {
        server.start();
        System.out.println("Server started");
    }

    @Override
    public void onState(UnoGame game) {
        sendGameState();
    }

    @Override
    public void onWinner(Player winner) {
        broadcast("Winner: " + winner.getName());
    }

    @Override
    public void onDraw(Player player, java.util.List<Card> cards) {
        for (RemotePlayer rp : remotePlayers.values()) {
            if (rp.equals(player)) {
                java.util.List<String> labels = new java.util.ArrayList<>();
                for (Card c : cards) {
                    labels.add(c.toString());
                }
                Map<String, Object> payload = new HashMap<>();
                payload.put("text", "Drew");
                payload.put("cards", labels);
                rp.getClient().sendEvent("drawnCards", payload);
                break;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        UnoServer us = new UnoServer(9092);
        us.start();
        Thread.sleep(Integer.MAX_VALUE);
    }
}
