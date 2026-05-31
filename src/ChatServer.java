import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ChatServer {

    static final List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    // message ID counter
    static final AtomicInteger messageCounter = new AtomicInteger(0);

    // last 20 messages
    private final List<String> history = new CopyOnWriteArrayList<>();
    private static final int MAX_HISTORY = 20;

    // offline queue: username (lowercase) → queued messages
    final Map<String, Queue<String>> offlineQueue = new ConcurrentHashMap<>();

    // ── Broadcast to all except sender ──────────────────────────────
    public void broadcast(String msg, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(msg);
            }
        }
    }

    // ── Send to a specific user, returns false if not online ─────────
    public boolean sendPrivateTo(String name, String msg) {
        for (ClientHandler client : clients) {
            if (client.name != null && client.name.equalsIgnoreCase(name)) {
                client.sendMessage(msg);
                return true;
            }
        }
        return false;
    }

    // ── Queue message for offline user ───────────────────────────────
    public void queueOffline(String name, String msg) {
        offlineQueue
                .computeIfAbsent(name.toLowerCase(), k -> new LinkedList<>())
                .add(msg);
    }

    // ── Check if username exists among connected clients ─────────────
    public boolean searchname(String name) {
        for (ClientHandler cl : clients) {
            if (cl.name != null && cl.name.equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    // ── Check for duplicate on registration ──────────────────────────
    public boolean duplicates(String name) {
        for (ClientHandler cl : clients) {
            if (cl.name != null && cl.name.equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    // ── History ───────────────────────────────────────────────────────
    public void addToHistory(String msg) {
        history.add(msg);
        if (history.size() > MAX_HISTORY) {
            history.remove(0);
        }
    }

    public List<String> getHistory() {
        return Collections.unmodifiableList(history);
    }

    // ── Main ──────────────────────────────────────────────────────────
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(38297);
        ChatServer server = new ChatServer();

        System.out.println("\u001B[32m[SERVER] Started on port 38297\u001B[0m");
        System.out.println("\u001B[33m[SERVER] Waiting for clients...\u001B[0m");

        // one thread pool for all client handlers
        ExecutorService pool = Executors.newCachedThreadPool();

        while (true) {
            Socket socket = serverSocket.accept();
            ClientHandler handler = new ClientHandler(socket, server);
            clients.add(handler);
            pool.submit(handler);
        }
    }
}