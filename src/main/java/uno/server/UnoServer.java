package uno.server;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.corundumstudio.socketio.listener.DataListener;

import uno.UnoGame;
import uno.players.Player;
import uno.cards.Card;
import uno.cards.Color;
import uno.piles.DiscardPile;

import java.util.*;

public class UnoServer {

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
            client.sendEvent("yourTurn", getHand().toString(), pile.getTopCard().toString());
            synchronized (lock) {
                chosenIndex = null;
                client.sendEvent("requestPlay", "");
                try {
                    lock.wait(60000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            if (chosenIndex == null || chosenIndex < 0 || chosenIndex >= getHand().size()) {
                return null;
            }
            Card card = getHand().get(chosenIndex);
            if (pile.addCard(card)) {
                getHand().remove(card);
            }
            return card;
        }

        @Override
        public Color announceCardColor() {
            client.sendEvent("requestColor", "");
            synchronized (lock) {
                chosenColor = null;
                try {
                    lock.wait(60000);
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
                if (p != null) {
                    game.getPlayers().remove(p);
                    broadcast(p.getName() + " disconnected.");
                }
            }
        });

        server.addEventListener("join", String.class, new DataListener<String>() {
            @Override
            public void onData(SocketIOClient client, String name, AckRequest ack) {
                if (remotePlayers.size() >= 8) {
                    client.sendEvent("message", "Server full");
                    client.disconnect();
                    return;
                }
                RemotePlayer rp = new RemotePlayer(name, client);
                remotePlayers.put(client.getSessionId(), rp);
                game.addPlayer(rp);
                broadcast(name + " joined the game.");

                if (!started && game.getPlayers().size() >= 2) {
                    started = true;
                    broadcast("Game starting...");
                    new Thread(UnoServer.this::playGame).start();
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
    }

    private void broadcast(String msg) {
        System.out.println(msg);
        for (RemotePlayer p : remotePlayers.values()) {
            p.getClient().sendEvent("message", msg);
        }
    }

    private void playGame() {
        game.play();
    }

    public void start() {
        server.start();
        System.out.println("Server started");
    }

    public static void main(String[] args) throws Exception {
        UnoServer us = new UnoServer(9092);
        us.start();
        Thread.sleep(Integer.MAX_VALUE);
    }
}
