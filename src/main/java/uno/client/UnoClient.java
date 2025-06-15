package uno.client;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import java.net.URISyntaxException;
import java.util.Scanner;

public class UnoClient {
    private final Socket socket;

    public UnoClient(String uri, String name) throws URISyntaxException {
        socket = IO.socket(uri);

        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                socket.emit("join", name);
            }
        });

        socket.on("yourTurn", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("Your hand: " + args[0]);
                System.out.println("Top card: " + args[1]);
            }
        });

        socket.on("requestPlay", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Scanner sc = new Scanner(System.in);
                System.out.println("Enter card index to play (or -1 to pass): ");
                int index = Integer.parseInt(sc.nextLine());
                socket.emit("play", index);
            }
        });

        socket.on("requestColor", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Scanner sc = new Scanner(System.in);
                System.out.println("Choose color (RED, BLUE, GREEN, YELLOW): ");
                String color = sc.nextLine().trim().toUpperCase();
                socket.emit("color", color);
            }
        });

        socket.on("message", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println(args[0]);
            }
        });
    }

    public void start() {
        socket.connect();
    }

    public static void main(String[] args) throws Exception {
        if(args.length < 2) {
            System.out.println("Usage: UnoClient <serverUri> <name>");
            return;
        }
        UnoClient client = new UnoClient(args[0], args[1]);
        client.start();
        Thread.sleep(Integer.MAX_VALUE);
    }
}
