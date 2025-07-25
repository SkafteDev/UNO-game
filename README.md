# UNO Game

This project contains a simple implementation of the classic UNO card game written in Java. It provides a basic command line version, as well as an experimental multiplayer server using Socket.IO with a small web interface.

## Features
- Play vs. the computer
- Online/offline Multi-player
- Play in Browser or in the CLI
- Chat
- Waiting room

## Gameplay example in browser

1. Connect as a new player  
![1_new_player.png](docs%2Fgameplay%2F1_new_player.png)
2. Await other players until they are ready to play.  
![2_waiting_room.png](docs%2Fgameplay%2F2_waiting_room.png)
3. Play vs. the other players  
![draw_2_example.png](docs%2Fgameplay%2Fdraw_2_example.png)
![actioncard_wild.png](docs%2Fgameplay%2Factioncard_wild.png)
![multiplayer_example.png](docs%2Fgameplay%2Fmultiplayer_example.png)

## Repository Structure

```
src/
  main/
    java/uno/
      Main.java          # entry point for simple local game
      UnoGame.java       # core game engine
      GameListener.java  # callback interface for state updates
      cards/             # card classes (number and action cards)
      piles/             # DrawPile and DiscardPile logic
      players/           # Player base class plus Human and Computer players
      server/            # Socket.IO based server for multiplayer
      client/            # small Java client for the server
    resources/
      public/            # web client (index.html & client.js)
```

`pom.xml` is a Maven build file that declares the required dependencies (Socket.IO server/client libraries) and configures compilation for Java 17.

## Building and Running

A Java 17 JDK and Maven are required to build the project. The simplest way to compile the sources is:

```bash
mvn package
```

This will produce compiled classes in `target/`. (When running in an offline environment Maven might fail to resolve plugins or dependencies.)

### Local CLI Game

To start a quick game between one human player and three computer players run the `uno.Main` class:

```bash
mvn exec:java -Dexec.mainClass="uno.Main"
```

### Multiplayer Server

`uno.server.UnoServer` hosts the game logic and communicates with clients through Socket.IO. Launch it with for example:

```bash
mvn exec:java -Dexec.mainClass="uno.server.UnoServer"
```

By default it listens on port `9092`. A simple browser based client is provided under `WebUI/` – open `index.html` in a browser and connect to the server. There is also a lightweight CLI client (`uno.client.UnoClient`) which can connect via:

```bash
mvn exec:java -Dexec.mainClass="uno.client.UnoClient" -Dexec.args="http://localhost:9092 playerName"
```

## Code Architecture

- **Cards** – `uno.cards.Card` is the abstract base class. `NumberCard` implements numeric cards, while the `actioncards` package defines `SkipCard`, `ReverseCard`, `Draw2`, `WildCard` and `WildDraw4Card`. Every card implements the `matches()` method to check if it can be placed on the current top card.
- **Piles** – `DrawPile` builds the deck and handles drawing cards. `DiscardPile` manages the pile where played cards are placed and exposes the top card.
- **Players** – All players derive from `uno.players.Player` which stores the hand and defines actions such as `playCard` and `announceCardColor`. `ComputerPlayer` implements a simple AI and `HumanPlayer` interacts through the console or a remote client.
- **UnoGame** – Coordinates the game state, turn order, card shuffling and dealing. A `GameListener` may be registered to receive notifications when the state or winner changes, used by the Socket.IO server to update connected clients.
- **Server/Client** – `UnoServer` wraps `UnoGame` and exposes events over Socket.IO. Clients (browser or `UnoClient`) send player actions back to the server.

## Extending the Game

New card types can be introduced by creating subclasses of `Card` (or `ActionCard` if the card performs an action). Implement the `matches()` logic and, if applicable, the `action(UnoGame)` method that manipulates the game state. Finally, include instances of the new card in `DrawPile` when building the deck.

Other features – such as additional rules, score keeping or network commands – can be built on top of `UnoGame` and hooked into the server via the `GameListener` callbacks.

