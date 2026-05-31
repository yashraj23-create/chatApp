import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Client {

    static volatile boolean running = true;
    static volatile String currentPrompt = "";
    static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

    // ANSI Colors
    static final String RESET   = "\u001B[0m";
    static final String RED     = "\u001B[31m";
    static final String GREEN   = "\u001B[32m";
    static final String YELLOW  = "\u001B[33m";
    static final String CYAN    = "\u001B[36m";
    static final String BOLD    = "\u001B[1m";
    static final String MAGENTA = "\u001B[35m";

    static final String[] USER_COLORS = {
            "\u001B[32m", "\u001B[33m", "\u001B[34m",
            "\u001B[35m", "\u001B[36m", "\u001B[91m",
            "\u001B[92m", "\u001B[93m", "\u001B[94m"
    };

    public static String colorForUser(String name) {
        int idx = Math.abs(name.hashCode()) % USER_COLORS.length;
        return USER_COLORS[idx];
    }

    public static synchronized void safePrint(String msg) {
        System.out.print("\r\033[K"); // clear current line
        System.out.println(msg);
        if (!currentPrompt.isEmpty()) {
            System.out.print(currentPrompt);
        }
        System.out.flush();
    }

    static void printHelp() {
        safePrint(STR."\{YELLOW}─────────────────────────────────────\{RESET}");
        safePrint(STR."\{YELLOW}  Available Commands:\{RESET}");
        safePrint(STR."\{CYAN}  /dm <username> <message>\{RESET}  → private message");
        safePrint(STR."\{CYAN}  /bc <message>\{RESET}  → broadcast to all");
        safePrint(STR."\{CYAN}  /online\{RESET}  → list online users");
        safePrint(STR."\{CYAN}  /history\{RESET}  → last 20 messages");
        safePrint(STR."\{CYAN}  /help\{RESET}  → show this menu");
        safePrint(STR."\{CYAN}  /quit\{RESET}  → disconnect");
        safePrint(STR."\{YELLOW}─────────────────────────────────────\{RESET}");
    }

    public static void main(String[] args) throws IOException {

        BlockingQueue<String> serverResponses = new LinkedBlockingQueue<>();

        System.out.println(BOLD + CYAN);
        System.out.println("  ╔══════════════════════════════╗");
        System.out.println("  ║        TERMINAL CHAT         ║");
        System.out.println("  ╚══════════════════════════════╝");
        System.out.println(RESET);

        Socket socket = new Socket("localhost", 38297);

        BufferedReader in        = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out          = new PrintWriter(socket.getOutputStream(), true);

        // ── Name registration ──────────────────────────────────────
        String name = null;
        while (true) {
            System.out.print(STR."\{YELLOW}Enter your username: \{RESET}");
            String input = userInput.readLine();
            if (input == null || input.isBlank()) continue;

            out.println(input.trim());
            String response = in.readLine();

            if (response == null) {
                System.out.println(STR."\{RED}Server disconnected.\{RESET}");
                socket.close();
                return;
            }

            if (response.equalsIgnoreCase("ACCEPTED")) {
                name = input.trim();
                System.out.println(STR."""
\{GREEN}\{BOLD}
  ✓ Joined as [\{name}]\{RESET}""");
                printHelp();
                break;
            } else {
                System.out.println(STR."\{RED}  ✗ \{response}\{RESET}");
            }
        }

        final String myName = name;

        // ── Receiver thread ─────────────────────────────────────────
        new Thread(() -> {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    // internal protocol responses go to queue
                    if (msg.startsWith("---##")) {
                        serverResponses.put(msg.substring(5)); // strip prefix
                    } else {
                        safePrint(msg);
                    }
                }
            } catch (Exception e) {
                if (running) safePrint(STR."\{RED}[SERVER] Connection lost.\{RESET}");
            } finally {
                running = false;
                try { socket.close(); } catch (Exception ignored) {}
            }
        }, "receiver").start();

        new Thread(() -> {
            try {
                while (running) {
                    currentPrompt = STR."\{colorForUser(myName)}\{myName}\{RESET} > ";
                    System.out.print(currentPrompt);

                    String input = userInput.readLine();
                    if (input == null) break;
                    input = input.trim();
                    if (input.isEmpty()) continue;

                    String timestamp = LocalDateTime.now().format(formatter);

                    if (input.startsWith("/dm ")) {
                        String[] parts = input.substring(4).split(" ", 2);
                        if (parts.length < 2 || parts[1].isBlank()) {
                            safePrint(STR."\{RED}Usage: /dm <username> <message>\{RESET}");
                            continue;
                        }
                        String recipient = parts[0].trim();
                        String message   = parts[1].trim();

                        out.println(STR."CMD|check|\{recipient}");
                        String check = serverResponses.take();
                        if (check.equalsIgnoreCase("not exist")) {
                            safePrint(STR."\{RED}  ✗ User [\{recipient}] not found.\{RESET}");
                            continue;
                        }

                        out.println(STR."\{timestamp}|dm|\{myName}|\{recipient}|\{message}");

                    } else if (input.startsWith("/bc ")) {
                        String message = input.substring(4).trim();
                        if (message.isBlank()) {
                            safePrint(STR."\{RED}Usage: /bc <message>\{RESET}");
                            continue;
                        }
                        out.println(STR."\{timestamp}|public|\{myName}||\{message}");

                    } else if (input.equalsIgnoreCase("/online")) {
                        out.println("CMD|online");

                    } else if (input.equalsIgnoreCase("/history")) {
                        out.println("CMD|history");

                    } else if (input.equalsIgnoreCase("/help")) {
                        printHelp();

                    } else if (input.equalsIgnoreCase("/quit")) {
                        out.println("CMD|quit");
                        running = false;
                        socket.close();
                        safePrint(STR."\{YELLOW}  Goodbye, \{myName}!\{RESET}");
                        break;

                    } else {
                        safePrint(STR."\{RED}  Unknown command. Type /help for commands.\{RESET}");
                    }
                }
            } catch (Exception e) {
                if (running) e.printStackTrace();
            }
        }, "sender").start();
    }
}
